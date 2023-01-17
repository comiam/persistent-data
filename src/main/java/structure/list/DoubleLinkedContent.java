package structure.list;

import persistency_base.PersistentNode;

public class DoubleLinkedContent<T> {
    public final PersistentNode<DoubleLinkedData<T>> pseudoHead, pseudoTail;

    public DoubleLinkedContent(PersistentNode<DoubleLinkedData<T>> pseudoHead, PersistentNode<DoubleLinkedData<T>> pseudoTail) {
        this.pseudoHead = pseudoHead;
        this.pseudoTail = pseudoTail;
    }
}
