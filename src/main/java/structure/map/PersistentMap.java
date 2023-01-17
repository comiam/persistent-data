package structure.map;

import persistency_base.*;
import structure.array.PersistentArray;
import structure.list.PersistentLinkedList;
import tree.BinaryTree;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class PersistentDictionary<TK, TV> extends BasePersistentCollection<BinaryTree<TK, PersistentNode<TV>>> implements Iterable<Map.Entry<TK, TV>>, IUndoRedo<PersistentDictionary<TK, TV>> {
    public PersistentDictionary() {
        nodes = new PersistentContent<>(new BinaryTree<>(), new ModificationCount(modificationCount));
    }

    private PersistentDictionary(PersistentContent<BinaryTree<TK, PersistentNode<TV>>> nodes, int count, int modificationCount) {
        super(nodes, count, modificationCount);
    }

    private PersistentDictionary(PersistentContent<BinaryTree<TK, PersistentNode<TV>>> nodes, int count, int modificationCount, int start) {
        super(nodes, count, modificationCount, start);
    }

    protected PersistentContent<BinaryTree<TK, PersistentNode<TV>>> reassembleNodes() {
        var newContent = new PersistentContent<>(
                new BinaryTree<TK, PersistentNode<TV>>(),
                new ModificationCount(modificationCount)
        );

        var allModifications = new ArrayList<Map.Entry<TK, Map.Entry<Integer, TV>>>();

        for (var entry : nodes.content.toList()) {
            var nodeKey = entry.getKey();
            var persistentNode = entry.getValue();

            var neededModifications = persistentNode.modifications.toList()
                    .stream()
                    .filter(m -> m.getKey() <= modificationCount)
                    .map(m -> Map.entry(nodeKey, m))
                    .sorted(Comparator.comparing(m -> m.getValue().getKey())).toList();

            allModifications.addAll(neededModifications);
        }

        for (var mod : allModifications) {
            var nodeKey = mod.getKey();
            var step = mod.getValue().getKey();
            var nodeVal = mod.getValue().getValue();

            newContent.update(c -> {
                var node = c.get(nodeKey);
                if (node == null) {
                    c.insert(nodeKey, new PersistentNode<>(step, nodeVal));
                } else {
                    node.update(step, nodeVal);
                }
            });
        }

        return newContent;
    }

    private void implAdd(PersistentContent<BinaryTree<TK, PersistentNode<TV>>> nodes, int modificationCount, TK key, TV value) {
        nodes.update(c -> c.insert(key, new PersistentNode<TV>(modificationCount + 1, value)));
    }

    private void implRemove(PersistentContent<BinaryTree<TK, PersistentNode<TV>>> nodes, int modificationCount, TK key) {
        nodes.update(c -> c.get(key).update(modificationCount + 1, null));
    }

    private void implClear(PersistentContent<BinaryTree<TK, PersistentNode<TV>>> nodes, int modificationCount) {
        nodes.update(c -> {
            for (var keyValuePair : c.toList()) {
                keyValuePair.getValue().update(modificationCount + 1, null);
            }
        });
    }

    private void implReplace(PersistentContent<BinaryTree<TK, PersistentNode<TV>>> nodes, int modificationCount, TK key, TV value) {
        nodes.update(c -> c.get(key).update(modificationCount + 1, value));
    }

    public PersistentDictionary<TK, TV> add(TK key, TV value) {
        var tryNode = nodes.content.get(key);
        if (tryNode != null && tryNode.modifications.toList().stream().anyMatch(m -> m.getKey() <= modificationCount)) {
            throw new IllegalArgumentException("Such a key is already presented in the dictionary");
        }

        if (nodes.maxModification.value > modificationCount) {
            var res = reassembleNodes();
            implAdd(res, modificationCount, key, value);

            return new PersistentDictionary<>(res, count + 1, modificationCount + 1);
        }

        implAdd(nodes, modificationCount, key, value);

        return new PersistentDictionary<>(nodes, count + 1, modificationCount + 1);
    }

    public PersistentDictionary<TK, TV> remove(TK key) {
        var tryNode = nodes.content.get(key);
        if (tryNode == null || tryNode.modifications.toList().stream().allMatch(m -> m.getKey() > modificationCount)) {
            return this;
        }

        if (nodes.maxModification.value > modificationCount) {
            var res = reassembleNodes();
            implRemove(res, modificationCount, key);

            return new PersistentDictionary<>(res, count - 1, modificationCount + 1);
        }

        implRemove(nodes, modificationCount, key);

        return new PersistentDictionary<TK, TV>(nodes, count - 1, modificationCount + 1);
    }

    public PersistentDictionary<TK, TV> clear() {
        if (nodes.maxModification.value > modificationCount) {
            var res = reassembleNodes();
            implClear(res, modificationCount);

            return new PersistentDictionary<>(res, 0, modificationCount + 1);
        }

        implClear(nodes, modificationCount);

        return new PersistentDictionary<>(nodes, 0, modificationCount + 1);
    }

    public PersistentDictionary<TK, TV> replace(TK key, TV value) {
        var tryNode = nodes.content.get(key);
        if (tryNode == null || tryNode.modifications.toList().stream().allMatch(m -> m.getKey() > modificationCount)) {
            throw new IllegalArgumentException("Such a key is not presented in the dictionary");
        }

        if (nodes.maxModification.value > modificationCount) {
            var res = reassembleNodes();
            implReplace(res, modificationCount, key, value);

            return new PersistentDictionary<>(res, count, modificationCount + 1);
        }

        implReplace(nodes, modificationCount, key, value);

        return new PersistentDictionary<>(nodes, count, modificationCount + 1);
    }

    public boolean containsKey(TK key) {
        return nodes.content.contains(key);
    }

    public TV get(TK key) {
        var node = nodes.content.get(key);

        return node == null
                ? null
                : node.modifications.findNearestLess(modificationCount);
    }

    public PersistentDictionary<TK, TV> undo() {
        return modificationCount == startModificationCount ? this : new PersistentDictionary<TK, TV>(nodes,
                recalculateCount(modificationCount - 1), modificationCount - 1);
    }

    public PersistentDictionary<TK, TV> redo() {
        return modificationCount == nodes.maxModification.value
                ? this
                : new PersistentDictionary<TK, TV>(
                nodes,
                recalculateCount(modificationCount + 1),
                modificationCount + 1
        );
    }

    protected int recalculateCount(int modificationStep) {
        return (int) nodes.content.toList()
                .stream()
                .filter(n -> n.getValue().modifications.toList()
                        .stream()
                        .anyMatch(m -> m.getKey() <= modificationStep))
                .count();
    }

    public PersistentArray<TV> toPersistentArray() {
        var content = new PersistentContent<List<PersistentNode<TV>>>(new ArrayList<>(), nodes.maxModification);

        var persistentNodes = nodes.content.toList().stream().map(Map.Entry::getValue).toList();
        content.content.addAll(persistentNodes);

        return new PersistentArray<>(content, count, modificationCount, modificationCount);
    }
}

