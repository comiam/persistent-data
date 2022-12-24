package persistency_base;

public interface IUndoRedo<T> {
    public T Undo();

    public T Redo();
}
