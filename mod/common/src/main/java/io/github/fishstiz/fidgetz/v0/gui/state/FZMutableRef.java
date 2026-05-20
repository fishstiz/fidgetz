package io.github.fishstiz.fidgetz.v0.gui.state;

import org.apache.commons.lang3.mutable.MutableObject;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public class FZMutableRef<T extends @Nullable Object> implements FZRef<T> {
    private final Map<String, Runnable> subscribers = Collections.synchronizedMap(new LinkedHashMap<>());
    private T state;

    public FZMutableRef(T initialValue) {
        this.state = initialValue;
    }

    public void notifySubscribers() {
        synchronized (subscribers) {
            subscribers.values().forEach(Runnable::run);
        }
    }

    public void clearSubscribers() {
        synchronized (subscribers) {
            subscribers.clear();
        }
    }

    public void set(T newState) {
        T prevState = this.state;
        this.state = newState;
        if (!Objects.equals(prevState, newState)) {
            notifySubscribers();
        }
    }

    public void set(UnaryOperator<T> function) {
        set(function.apply(state));
    }

    @Override
    public T value() {
        return this.state;
    }

    public void unsubscribe(String id) {
        synchronized (subscribers) {
            subscribers.remove(id);
        }
    }

    @Override
    public <R extends @Nullable Object> Runnable subscribe(String key, Function<T, R> selector, Consumer<R> callback) {
        Objects.requireNonNull(callback, "callback cannot be null");
        MutableObject<R> last = new MutableObject<>(selector.apply(this.state));
        Runnable subscriber = () -> {
            R next = selector.apply(this.state);
            if (!Objects.equals(next, last.get())) {
                last.setValue(next);
                callback.accept(next);
            }
        };
        synchronized (subscribers) {
            subscribers.put(key, subscriber);
        }
        return () -> unsubscribe(key);
    }

    @Override
    public Runnable subscribe(String key, Consumer<T> callback) {
        Objects.requireNonNull(callback, "callback cannot be null");
        synchronized (subscribers) {
            subscribers.put(key, () -> callback.accept(this.state));
        }
        return () -> unsubscribe(key);
    }
}
