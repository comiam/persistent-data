package persistency_base;

public abstract class BasePersistentCollection<T> {
    protected PersistentContent<T> nodes;
    protected final int modificationCount, startModificationCount;

    protected BasePersistentCollection() {
        modificationCount = 0;
        startModificationCount = 0;
    }

    protected BasePersistentCollection(PersistentContent<T> nodes, int count, int modificationCount, int startModificationCount) {
        this.nodes = nodes;
        this.modificationCount = modificationCount;
        this.startModificationCount = startModificationCount;
        this.count = count;
    }

    public int count;

    protected abstract int RecalculateCount(int modificationStep);

    protected abstract PersistentContent<T> ReassembleNodes();
}


