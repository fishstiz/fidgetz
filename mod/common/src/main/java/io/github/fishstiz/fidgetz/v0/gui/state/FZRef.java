package io.github.fishstiz.fidgetz.v0.gui.state;

import org.apache.commons.lang3.mutable.MutableObject;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public interface FZRef<T extends @Nullable Object> {
    T value();

    <R extends @Nullable Object> Runnable subscribe(String key, Function<T, R> selector, Consumer<R> callback);

    default Runnable subscribe(String key, Consumer<T> callback) {
        return subscribe(key, Function.identity(), callback);
    }

    default Runnable subscribe(String key, Runnable callback) {
        Objects.requireNonNull(callback, "callback cannot be null");
        return subscribe(key, _ -> callback.run());
    }

    default <R> R bind(String key, R object, BiConsumer<T, R> effect) {
        effect.accept(value(), object);
        subscribe(key, s -> effect.accept(s, object));
        return object;
    }

    default <R extends @Nullable Object> FZRef<R> map(Function<T, R> selector) {
        Objects.requireNonNull(selector, "selector cannot be null");
        FZRef<T> parent = this;
        return new FZRef<>() {
            @Override
            public R value() {
                return selector.apply(parent.value());
            }

            @Override
            public <S extends @Nullable Object> Runnable subscribe(String key, Function<R, S> innerSelector, Consumer<S> callback) {
                MutableObject<R> last = new MutableObject<>(selector.apply(parent.value()));
                return parent.subscribe(key, innerSelector.compose(selector), (s) -> {
                    R next = selector.apply(parent.value());
                    if (!Objects.equals(next, last.get())) {
                        last.setValue(next);
                        callback.accept(s);
                    }
                });
            }
        };
    }
}
