package io.github.fishstiz.fidgetz.v0.gui.components;

import io.github.fishstiz.fidgetz.v0.inject.interfaces.WidgetOperator;
import io.github.fishstiz.fidgetz.v0.utils.CollectionUtils;
import net.minecraft.client.gui.screens.Screen;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class FZDialogManager {
    private static final Comparator<DialogEntry> DIALOG_COMPARATOR = Comparator.comparingInt(e -> e.dialog.fidgetz$popoverOrder());
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

    public void refreshDialogs() {
        dialogList.forEach(widgetRemover);
        dialogList = dialogsById.values().stream()
                .sorted(DIALOG_COMPARATOR)
                .map(DialogEntry::dialog)
                .toList();
        dialogList.reversed().forEach(widgetAdder);
    }

    @SuppressWarnings("unchecked")
    public <T extends FZDialog> void put(T dialog, Consumer<T> closer) {
        String id = Objects.requireNonNull(dialog.fidgetz$componentId(), "dialog id cannot be null");

        if (dialog.container != container) {
            throw new IllegalArgumentException("The dialog's (%s) container must be the same as the dialog manager's".formatted(id));
        }

        DialogEntry previous = dialogsById.get(id);
        if (previous != null && previous.dialog != dialog) {
            previous.close();
        }

        dialogsById.put(id, new DialogEntry(dialog, (Consumer<FZDialog>) closer));
        refreshDialogs();
    }

    public void put(FZDialog dialog) {
        put(dialog, d -> d.setOpen(false));
    }

    public <T extends FZDialog> void putIfClosed(String id, Supplier<T> fallback, Consumer<T> closer) {
        DialogEntry entry = dialogsById.get(id);
        if (entry == null || !entry.dialog.isOpen()) {
            put(fallback.get(), closer);
        }
    }

    public void putIfClosed(String id, Supplier<FZDialog> fallback) {
        putIfClosed(id, fallback, d -> d.setOpen(false));
    }

    public void remove(String id) {
        DialogEntry previous = dialogsById.remove(id);
        if (previous != null) previous.dialog.setOpen(false);
        refreshDialogs();
    }

    public void clear() {
        dialogsById.values().stream().sorted(DIALOG_COMPARATOR).forEachOrdered(DialogEntry::close);
        dialogsById.clear();
        refreshDialogs();
    }

    public Optional<FZDialog> get(String id) {
        return Optional.ofNullable(dialogsById.get(id)).map(DialogEntry::dialog);
    }

    public List<FZDialog> dialogs() {
        return dialogList;
    }

    private record DialogEntry(FZDialog dialog, Consumer<FZDialog> closer) {
        private void close() {
            closer.accept(dialog);
        }
    }
}
