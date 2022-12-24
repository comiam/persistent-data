package persistency_base;

import java.util.function.Consumer;




public class PersistentContent<T> {
    public T content;
    public ModificationCount maxModification;

    public PersistentContent(T content, ModificationCount step) {
        this.content = content;
        maxModification = step;
    }

    public void Update(Consumer<T> contentUpdater) {
        contentUpdater.accept(content);
        maxModification.value++;
    }
}
