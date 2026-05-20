package io.github.fishstiz.fidgetz.v0.utils;

import java.util.ArrayList;
import java.util.List;

public final class CollectionUtils {
    private CollectionUtils() {
    }

    public static <E> List<E> addFirst(List<E> list, E element) {
        List<E> newList = new ArrayList<>(list.size() + 1);
        newList.add(element);
        newList.addAll(list);
        return newList;
    }

    public static <E> List<E> addLast(List<E> list, E element) {
        List<E> newList = new ArrayList<>(list.size() + 1);
        newList.addAll(list);
        newList.add(element);
        return newList;
    }

    public static <E> List<E> remove(List<E> list, E element) {
        if (list.isEmpty()) {
            return new ArrayList<>();
        }

        List<E> newList = new ArrayList<>(list.size());
        for (E e : list) {
            if (!e.equals(element)) {
                newList.add(e);
            }
        }
        return newList;
    }
}
