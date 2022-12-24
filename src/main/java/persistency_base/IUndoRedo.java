package persistency_base;

public interface IUndoRedo<T> {
    T undo();
    T redo();
}
