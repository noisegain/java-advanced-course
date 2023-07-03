package info.kgeorgiy.ja.ponomarenko.arrayset;

import java.util.*;

public class ArraySet<T extends Comparable<? super T>> extends AbstractSet<T> implements SortedSet<T> {

    private final Comparator<T> comparator;
    private final List<T> data;

    public ArraySet() {
        comparator = Comparator.naturalOrder();
        data = Collections.emptyList();
    }

    private ArraySet(List<T> collection, Comparator<T> comparator) {
        this.comparator = comparator;
        data = List.copyOf(collection);
    }

    public ArraySet(Collection<T> collection) {
        this(buildFrom(collection, null), Comparator.naturalOrder());
    }

    public ArraySet(Collection<T> collection, Comparator<T> comparator) {
        this(buildFrom(collection, comparator), comparator);
    }
    private static <E> List<E> buildFrom(Collection<E> collection, Comparator<E> comparator) {
        Set<E> set = new TreeSet<>(comparator);
        set.addAll(collection);
        return List.copyOf(set);
    }

    @Override
    public Iterator<T> iterator() {
        return data.iterator();
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public Comparator<? super T> comparator() {
        return comparator.equals(Comparator.naturalOrder()) ? null : comparator;
    }

    @Override
    public SortedSet<T> subSet(T fromElement, T toElement) {
        if (comparator.compare(fromElement, toElement) > 0) {
            throw new IllegalArgumentException("fromElement > toElement");
        }
        int l = lowerBound(fromElement);
        int r = lowerBound(toElement);
        return new ArraySet<>(data.subList(l, r), comparator);
    }

    @Override
    public SortedSet<T> headSet(T toElement) {
        return new ArraySet<>(data.subList(0, lowerBound(toElement)), comparator);
    }

    @Override
    public SortedSet<T> tailSet(T fromElement) {
        return new ArraySet<>(data.subList(lowerBound(fromElement), data.size()), comparator);
    }

    @Override
    public T first() {
        checkIsNotEmpty();
        return data.get(0);
    }

    @Override
    public T last() {
        checkIsNotEmpty();
        return data.get(data.size() - 1);
    }

    private void checkIsNotEmpty() {
        if (data.isEmpty()) {
            throw new NoSuchElementException();
        }
    }
    private int lowerBound(T x) {
        int index = Collections.binarySearch(data, x, comparator);
        if (index < 0) {
            index = -index - 1;
        }
        return index;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(Object o) {
        if (data.isEmpty()) {
            return false;
        }
        int index = lowerBound((T) o);
        return index < data.size() && comparator.compare(data.get(index), (T) o) == 0;
    }
}
