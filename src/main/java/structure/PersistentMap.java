package structure;

import persistency_base.*;
import tree.BinaryTree;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class PersistentMap<TK, TV>
        extends BasePersistentCollection<BinaryTree<TK, PersistentNode<TV>>>
        implements IUndoRedo<PersistentMap<TK, TV>>, Collection<Map.Entry<TK, TV>> {

    public PersistentMap() {
        nodes = new PersistentContent<>(new BinaryTree<>(), new ModificationCount(modificationCount));
    }

    private PersistentMap(PersistentContent<BinaryTree<TK, PersistentNode<TV>>> nodes,
                         int count,
                         int modificationCount) {
        super(nodes, count, modificationCount);
    }

    private PersistentMap(PersistentContent<BinaryTree<TK, PersistentNode<TV>>> nodes,
                         int count,
                         int modificationCount,
                         int start) {
        super(nodes, count, modificationCount, start);
    }


    @Override
    protected int recalculateCount(int modificationStep) {
        return (int) nodes.content.toList()
                .stream()
                .filter(n -> n.getValue().modifications.toList()
                        .stream()
                        .anyMatch(m -> m.getKey() <= modificationStep))
                .count();
    }

    @Override
    protected PersistentContent<BinaryTree<TK, PersistentNode<TV>>> reassembleNodes() {
        return null;
    }

    @Override
    public PersistentMap<TK, TV> undo() {
        return modificationCount == startModificationCount ? this : new PersistentMap<>(nodes,
                recalculateCount(modificationCount - 1), modificationCount - 1);
    }

    @Override
    public PersistentMap<TK, TV> redo() {
        return modificationCount == nodes.maxModification.value ? this : new PersistentMap<>(nodes,
                recalculateCount(modificationCount + 1), modificationCount + 1);

    }

    @Override
    public int size() {
        return nodes.content.toList().size();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean contains(Object o) {
        return false;
    }

    @Override
    public Iterator<Map.Entry<TK, TV>> iterator() {
        return null;
    }

    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return null;
    }

    @Override
    public boolean add(Map.Entry<TK, TV> tktvEntry) {
        return false;
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends Map.Entry<TK, TV>> c) {
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return false;
    }

    @Override
    public void clear() {

    }
}
