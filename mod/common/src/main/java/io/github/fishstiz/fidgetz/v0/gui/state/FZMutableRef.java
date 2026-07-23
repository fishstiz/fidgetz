package io.github.fishstiz.fidgetz.v0.gui.state;

import org.apache.commons.lang3.mutable.MutableObject;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class FZMutableRef<T> implements FZRef<T> {
    private volatile Map<String, Subscriber> subscribers = Collections.emptyMap();
    private volatile T state;

    public FZMutableRef(T initialValue) {
        this.state = initialValue;
    }

    public static <T> FZMutableRef<T> wrap(Consumer<T> setter, Supplier<T> getter) {
        FZMutableRef<T> ref = new FZMutableRef<>(getter.get());
        ref.subscribe("FZMutableRef#wrap@" + setter.hashCode(), setter);
        return ref;
    }

    public void notifySubscribers() {
        subscribers.values().forEach(Subscriber::run);
    }

    public void clearSubscribers() {
        subscribers.values().forEach(Subscriber::cancel);
        subscribers = Collections.emptyMap();
    }

    public void set(T newState) {
        T prevState = this.state;
        this.state = newState;
        if (!Objects.equals(prevState, newState)) {
            notifySubscribers();
        }
    }

    public void set(UnaryOperator<T> function) {
        set(function.apply(this.state));
    }

    @Override
    public T value() {
        return this.state;
    }

    public void unsubscribe(String id) {
        Map<String, Subscriber> newSubscribers = new LinkedHashMap<>(this.subscribers);
        Subscriber removed = newSubscribers.remove(id);
        if (removed != null) removed.cancel();
        this.subscribers = newSubscribers;
    }

    private void put(String key, Subscriber subscriber) {
        Map<String, Subscriber> newSubscribers = new LinkedHashMap<>(this.subscribers);
        Subscriber removed = newSubscribers.put(key, subscriber);
        if (removed != null) removed.cancel();
        this.subscribers = newSubscribers;
    }

    @Override
    public <R> Runnable subscribe(String key, Function<T, R> selector, Consumer<R> callback) {
        Objects.requireNonNull(callback, "callback cannot be null");
        MutableObject<R> last = new MutableObject<>(selector.apply(this.state));
        put(key, new Subscriber(() -> {
            R next = selector.apply(this.state);
            if (!Objects.equals(next, last.getValue())) {
                last.setValue(next);
                callback.accept(next);
            }
        }));
        return () -> unsubscribe(key);
    }

    @Override
    public Runnable subscribe(String key, Consumer<T> callback) {
        Objects.requireNonNull(callback, "callback cannot be null");
        put(key, new Subscriber(() -> callback.accept(this.state)));
        return () -> unsubscribe(key);
    }

    private static final class Subscriber implements Runnable {
        private final Runnable task;
        private volatile boolean active = true;

        Subscriber(Runnable task) {
            this.task = task;
        }

        @Override
        public void run() {
            if (active) {
                task.run();
            }
        }

        void cancel() {
            active = false;
        }
    }
}
