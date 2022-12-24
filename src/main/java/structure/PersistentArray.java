package structure;

import persistency_base.*;

import java.util.*;

public class PersistentArray<T> extends BasePersistentCollection<List<PersistentNode<T>>> implements Collection<T>, IUndoRedo<PersistentArray<T>> {
    public PersistentArray() throws IndexOutOfBoundsException {
        nodes = new PersistentContent<>(new ArrayList<>(), new ModificationCount(modificationCount));
    }

    private PersistentArray(PersistentContent<List<PersistentNode<T>>> nodes, int count, int modificationCount) {
        super(nodes, count, modificationCount);
    }

    private PersistentArray(PersistentContent<List<PersistentNode<T>>> nodes, int count, int modificationCount, int start) {
        super(nodes, count, modificationCount, start);
    }

    @Override
    protected PersistentContent<List<PersistentNode<T>>> reassembleNodes() {
        var newContent = new PersistentContent<List<PersistentNode<T>>>(new ArrayList<>(),
                new ModificationCount(modificationCount));
        var allModifications = new ArrayList<Map.Entry<Integer, Map.Entry<Integer, T>>>();
        for (var i = 0; i < nodes.content.size(); i++) {
            var node = nodes.content.get(i);
            int finalI = i;
            var neededModifications = node
                    .modifications
                    .toList()
                    .stream()
                    .filter(
                            m -> m.getKey() <= modificationCount
                    ).sorted(
                            Comparator.comparingInt(Map.Entry::getKey)
                    ).map(val -> Map.entry(finalI, val)).toList();

            allModifications.addAll(neededModifications);
        }

        allModifications.forEach(m -> {
            if (m.getKey() >= newContent.content.size()) {
                newContent.update(c ->
                        c.add(new PersistentNode<>(m.getValue().getKey(), m.getValue().getValue())));
            } else {
                newContent.update(c -> c.get(m.getKey()).update(m.getValue().getKey(), m.getValue().getValue()));
            }
        });

        return newContent;
    }

    private void persistAddImpl(PersistentContent<List<PersistentNode<T>>> content, int modificationCount, T value) {
        content.update(c -> c.add(new PersistentNode<>(modificationCount + 1, value)));
    }

    private void persistInsertImpl(PersistentContent<List<PersistentNode<T>>> content, int modificationCount, int index, T value) {
        content.update(c ->
        {
            c.add(new PersistentNode<>(modificationCount + 1, c.get(c.size() - 1).value(modificationCount)));
            c.get(index).update(modificationCount + 1, value);
            for (var i = index + 1; i < c.size(); i++) {
                c.get(i).update(modificationCount + 1, c.get(i - 1).value(modificationCount));
            }
        });
    }

    private void persistReplaceImpl(PersistentContent<List<PersistentNode<T>>> content, int modificationCount, int index, T value) {
        content.update(c -> c.get(index).update(modificationCount + 1, value));
    }

    private void persistRemoveImpl(PersistentContent<List<PersistentNode<T>>> content, int modificationCount, int index) {
        content.update(c -> {
            for (var i = index; i < c.size() - 1; i++) {
                c.get(i).update(modificationCount + 1, c.get(i + 1).value(modificationCount));
            }
            c.get(c.size() - 1).update(modificationCount + 1, null);
        });
    }

    private void persistClear(PersistentContent<List<PersistentNode<T>>> content, int modificationCount) {
        content.update(c -> c.forEach(n -> n.update(modificationCount + 1, null)));
    }

    public PersistentArray<T> persistAdd(T value) {
        if (nodes.maxModification.value > modificationCount) {
            var res = reassembleNodes();
            persistAddImpl(res, modificationCount, value);

            return new PersistentArray<>(res, count + 1, modificationCount + 1);
        }

        persistAddImpl(nodes, modificationCount, value);
        return new PersistentArray<>(nodes, count + 1, modificationCount + 1);
    }

    public PersistentArray<T> persistInsert(int index, T value) {
        if (index < 0 || index > count) {
            throw new IndexOutOfBoundsException(index);
        }

        if (index == count) {
            return persistAdd(value);
        }

        if (nodes.maxModification.value > modificationCount) {
            var res = reassembleNodes();
            persistInsertImpl(res, modificationCount, index, value);

            return new PersistentArray<>(res, count + 1, modificationCount + 1);
        }

        persistInsertImpl(nodes, modificationCount, index, value);
        return new PersistentArray<>(nodes, count + 1, modificationCount + 1);
    }

    public PersistentArray<T> persistReplace(int index, T value) {
        if (index < 0 || index > count) {
            throw new IndexOutOfBoundsException(index);
        }

        if (nodes.maxModification.value > modificationCount) {
            var res = reassembleNodes();
            persistReplaceImpl(res, modificationCount, index, value);

            return new PersistentArray<>(res, count, modificationCount + 1);
        }

        persistReplaceImpl(nodes, modificationCount, index, value);
        return new PersistentArray<>(nodes, count, modificationCount + 1);
    }

    public PersistentArray<T> persistRemove(int index) {
        if (index < 0 || index >= count) {
            throw new IndexOutOfBoundsException(index);
        }

        if (nodes.maxModification.value > modificationCount) {
            var res = reassembleNodes();
            persistRemoveImpl(res, modificationCount, index);

            return new PersistentArray<>(res, count - 1, modificationCount + 1);
        }

        persistRemoveImpl(nodes, modificationCount, index);
        return new PersistentArray<>(nodes, count - 1, modificationCount + 1);
    }

    public PersistentArray<T> persistClear() {
        if (nodes.maxModification.value > modificationCount) {
            var res = reassembleNodes();
            persistClear(res, modificationCount);

            return new PersistentArray<>(res, 0, modificationCount + 1);
        }

        persistClear(nodes, modificationCount);
        return new PersistentArray<>(nodes, 0, modificationCount + 1);
    }

    public T get(int index) {
        if (index < 0 || index >= count) {
            throw new IndexOutOfBoundsException(index);
        }
        return nodes.content.get(index).value(modificationCount);
    }

    @Override
    public int size() {
        return nodes.content.size();
    }

    @Override
    public boolean isEmpty() {
        return nodes.content.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return false;
    }

    public Iterator<T> iterator() {
        return nodes.content
                .stream()
                .filter(
                        n -> n.modifications.toList()
                                .stream()
                                .anyMatch(m -> m.getKey() <= modificationCount))
                .map(n -> n.value(modificationCount))
                .iterator();
    }

    @Override
    public Object[] toArray() {
        return nodes.content.stream().map(n -> n.value(modificationCount)).toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] t1s) {
        return (T1[]) nodes.content.stream().map(n -> n.value(modificationCount)).toArray();
    }

    @Override
    public boolean add(T t) {
        throw new RuntimeException("Do not use this method! Use persistAdd instead of!");
    }

    @Override
    public boolean remove(Object o) {
        throw new RuntimeException("Do not use this method! Use persistRemove instead of!");
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends T> collection) {
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        return false;
    }

    @Override
    public void clear() {
        throw new RuntimeException("Do not use this method! Use persistClear instead of!");
    }

    public PersistentArray<T> undo() {
        return modificationCount == startModificationCount ? this : new PersistentArray<>(nodes,
                recalculateCount(modificationCount - 1), modificationCount - 1);
    }

    public PersistentArray<T> redo() {
        return modificationCount == nodes.maxModification.value ? this : new PersistentArray<>(nodes,
                recalculateCount(modificationCount + 1), modificationCount + 1);
    }

    @Override
    protected int recalculateCount(int modificationStep) {
        return (int) nodes.content
                .stream()
                .filter(
                        n -> n.modifications.toList()
                                .stream()
                                .anyMatch(
                                        m -> m.getKey() <= modificationStep
                                )
                ).count();
    }
}
