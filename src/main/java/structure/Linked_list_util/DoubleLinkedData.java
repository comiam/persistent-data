package structure.Linked_list_util;
import  java.util.UUID;
import persistency_base.PersistentNode;

public class DoubleLinkedData <T>
    {
        public PersistentNode<DoubleLinkedData<T>> previous;
        public PersistentNode<DoubleLinkedData<T>> next;
        public UUID id;
        public PersistentNode<T> value;

        public DoubleLinkedData(PersistentNode<DoubleLinkedData<T>> next, PersistentNode<DoubleLinkedData<T>> previous, PersistentNode<T> value)
        {
            this.next = next;
            this.previous = previous;
            this.value = value;
            id = UUID.randomUUID();
        }

        public DoubleLinkedData(PersistentNode<DoubleLinkedData<T>> next, PersistentNode<DoubleLinkedData<T>> previous, PersistentNode<T> value, UUID id)
        {
            this.next = next;
            this.previous = previous;
            this.value = value;
            this.id = id;
        }
    }
