package de.upb.crc901.services.core;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.Function;

/**
 * A helper class to put a filter on an iterator.
 * 
 */
public final class FilteredIterator<E> implements Iterator<E> {
    private final Iterator<E> iterator;
    private final Function<E, Boolean> filter;

    private boolean hasNext = true;
    private E next;

    public FilteredIterator(final Iterator<E> iterator, final Function<E, Boolean> filter) {
        this.iterator = Objects.requireNonNull(iterator);

        if (filter == null) {
            this.filter = new AcceptAllFilter<E>();
        } else {
            this.filter = filter;
        }

        this.findNext();
    }

    @Override
    public boolean hasNext() {
        return this.next != null;
    }

    @Override
    public E next() {
        E returnValue = this.next;
        this.findNext();
        return returnValue;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    private void findNext() {
        while (this.iterator.hasNext()) {
            this.next = iterator.next();
            if (this.filter.apply(this.next)) {
                return;
            }
        }
        this.next = null;
        this.hasNext = false;
    }

    private static final class AcceptAllFilter<T> implements Function<T, Boolean> {
		@Override
		public Boolean apply(T t) {
			return true;
		}
    }
}