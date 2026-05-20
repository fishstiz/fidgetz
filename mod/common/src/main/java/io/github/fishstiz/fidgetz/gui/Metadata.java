package io.github.fishstiz.fidgetz.gui;

@Deprecated
public interface Metadata<E> {
    E getMetadata();

    void setMetadata(E metadata);
}
