package io.github.fishstiz.fidgetz.v0.gui.components;

import io.github.fishstiz.fidgetz.v0.inject.interfaces.WidgetOperator;
import io.github.fishstiz.fidgetz.v0.utils.CollectionUtils;
import net.minecraft.client.gui.screens.Screen;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class FZDialogManager {
    private static final Comparator<DialogEntry> DIALOG_COMPARATOR = Comparator.comparingInt(DialogEntry::order);
    private final FZDialogContainer container;
    private final Consumer<FZDialog> widgetAdder;
    private final Consumer<FZDialog> widgetRemover;
    private final Map<String, DialogEntry> dialogsById = new LinkedHashMap<>();
    private List<FZDialog> dialogList = Collections.emptyList();

    public <T extends Screen & FZDialogContainer> FZDialogManager(T container) {
        this.container = container;
        WidgetOperator widgetOperator = (WidgetOperator) container;
        this.widgetAdder = (dialog -> {
            widgetOperator.fidgetz$modifyWidgets(prev -> CollectionUtils.addFirst(prev, dialog));
            widgetOperator.fidgetz$modifyNarratables(prev -> CollectionUtils.addFirst(prev, dialog));
            widgetOperator.fidgetz$modifyRenderables(prev -> CollectionUtils.addLast(prev, dialog));
        });
        this.widgetRemover = (dialog -> {
            widgetOperator.fidgetz$modifyWidgets(prev -> CollectionUtils.remove(prev, dialog));
            widgetOperator.fidgetz$modifyNarratables(prev -> CollectionUtils.remove(prev, dialog));
            widgetOperator.fidgetz$modifyRenderables(prev -> CollectionUtils.remove(prev, dialog));
        });
    }

    public FZDialogManager(FZDialogContainer container, Consumer<FZDialog> widgetAdder, Consumer<FZDialog> widgetRemover) {
        this.container = container;
        this.widgetAdder = widgetAdder;
        this.widgetRemover = widgetRemover;
    }

    private void updateDialogList() {
        dialogList.forEach(widgetRemover);
        dialogList = dialogsById.values().stream()
                .sorted(DIALOG_COMPARATOR)
                .map(DialogEntry::dialog)
                .toList();
        dialogList.reversed().forEach(widgetAdder);
    }

    @SuppressWarnings("unchecked")
    public <T extends FZDialog> void addOrReplace(String id, int order, T dialog, Consumer<T> closer) {
        Objects.requireNonNull(id, "id cannot be null");
        Objects.requireNonNull(dialog, "dialog cannot be null");

        if (dialog.container != container) {
            throw new IllegalArgumentException("The dialog's (%s) container must be the same as the dialog manager's".formatted(id));
        }

        DialogEntry previous = dialogsById.get(id);
        if (previous != null && previous.dialog != dialog) {
            previous.close();
        }

        dialogsById.put(id, new DialogEntry(order, dialog, (Consumer<FZDialog>) closer));
        updateDialogList();
    }

    public void addOrReplace(String id, int order, FZDialog dialog) {
        addOrReplace(id, order, dialog, d -> d.setOpen(false));
    }

    public <T extends FZDialog> void addIfClosed(String id, int order, Supplier<T> fallback, Consumer<T> closer) {
        DialogEntry entry = dialogsById.get(id);
        if (entry == null || !entry.dialog.isOpen()) {
            addOrReplace(id, order, fallback.get(), closer);
        }
    }

    public void addIfClosed(String id, int order, Supplier<FZDialog> fallback) {
        addIfClosed(id, order, fallback, d -> d.setOpen(false));
    }

    public void remove(String id) {
        DialogEntry previous = dialogsById.remove(id);
        if (previous != null) previous.dialog.setOpen(false);
        updateDialogList();
    }

    public void clear() {
        dialogsById.values().stream().sorted(DIALOG_COMPARATOR).forEachOrdered(DialogEntry::close);
        dialogsById.clear();
        updateDialogList();
    }

    public Optional<FZDialog> get(String id) {
        return Optional.ofNullable(dialogsById.get(id)).map(DialogEntry::dialog);
    }

    public List<FZDialog> dialogs() {
        return dialogList;
    }

    private record DialogEntry(int order, FZDialog dialog, Consumer<FZDialog> closer) {
        private void close() {
            closer.accept(dialog);
        }
    }
}
