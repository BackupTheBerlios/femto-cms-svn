/*
 * Two Queue Cache.
 * Copyright(C) 2006 Klaus Rennecke, all rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package de.mobizcorp.lib;

import java.lang.ref.WeakReference;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Implementation of the Two Queue algorithm, also known as 2Q. See "2Q: A Low
 * Overhead High Performance Buffer Management Replacement Algorithm", Theodore
 * Johnson, Dennis Shasha, Proceedings of the 20th VLDB Conference Santiago,
 * Chile, 1994.
 * 
 * @author Copyright(C) 2006 Klaus Rennecke, all rights reserved.
 */
public class TwoQueueCache<K, V> implements Map<K, V> {

    private abstract class AbstractIterator<E> implements Iterator<E> {

        private final Iterator<Entry<K, V>> delegate;

        private Entry<K, V> next;

        protected AbstractIterator() {
            delegate = map.values().iterator();
        }

        public boolean hasNext() {
            while (next == null && delegate.hasNext()) {
                final Entry<K, V> entry = delegate.next();
                if (entry instanceof InMEntry) {
                    next = entry;
                }
            }
            return next != null;
        }

        protected Entry<K, V> nextEntry() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            final Entry<K, V> result = next;
            next = null;
            return result;
        }

        public void remove() {
            try {
                TwoQueueCache.this.remove(next.getKey());
            } catch (NullPointerException e) {
                throw new IllegalStateException();
            }
        }

    }

    /**
     * Entry set implementation for entrySet().
     */
    private class EntrySet extends AbstractSet<Entry<K, V>> {

        public EntrySet() {
        }

        @Override
        public Iterator<Entry<K, V>> iterator() {
            return new AbstractIterator<Entry<K, V>>() {
                public Entry<K, V> next() {
                    return nextEntry();
                }
            };
        }

        @Override
        public int size() {
            return TwoQueueCache.this.size();
        }

    }

    /**
     * An entry in Ain or Am.
     */
    private static class InMEntry<K, V> implements Entry<K, V>,
            Ring.Node<InMEntry<K, V>> {
        private boolean isM;

        private final K key;

        private Ring.Node<InMEntry<K, V>> next;

        private final V value;

        public InMEntry(final K key, final V value) {
            this.key = key;
            this.value = value;
            this.isM = false;
        }

        public InMEntry(final Entry<K, V> entry) {
            this(entry.getKey(), entry.getValue());
            this.isM = true;
        }

        public boolean equals(final Entry other) {
            return safeEquals(getKey(), other.getKey())
                    && safeEquals(getValue(), other.getValue());
        }

        public boolean equals(final Object other) {
            return this == other
                    || (other instanceof Entry && equals((Entry) other));
        }

        public final K getKey() {
            return key;
        }

        public final Ring.Node<InMEntry<K, V>> getNext() {
            return next;
        }

        public final InMEntry<K, V> getSelf() {
            return this;
        }

        public final V getValue() {
            return value;
        }

        public int hashCode() {
            return safeHashCode(getKey()) ^ safeHashCode(getValue());
        }

        public final void setNext(final Ring.Node<InMEntry<K, V>> next) {
            this.next = next;
        }

        public final V setValue(final V value) {
            throw new UnsupportedOperationException("setValue");
        }
    }

    /**
     * Key set implementation for keys().
     */
    private class KeySet extends AbstractSet<K> {

        @Override
        public Iterator<K> iterator() {
            return new AbstractIterator<K>() {
                public K next() {
                    return nextEntry().getKey();
                }
            };
        }

        @Override
        public int size() {
            return TwoQueueCache.this.size();
        }

    }

    /**
     * An entry in Aout.
     */
    private static class OutEntry<K, V> extends WeakReference<V> implements
            Entry<K, V>, Ring.Node<OutEntry<K, V>> {
        private final K key;

        private Ring.Node<OutEntry<K, V>> next;

        public OutEntry(final K key, final V value) {
            super(value);
            this.key = key;
        }

        public OutEntry(final Entry<K, V> entry) {
            this(entry.getKey(), entry.getValue());
        }

        public boolean equals(final Entry other) {
            return safeEquals(getKey(), other.getKey())
                    && safeEquals(getValue(), other.getValue());
        }

        public boolean equals(final Object other) {
            return this == other
                    || (other instanceof Entry && equals((Entry) other));
        }

        public K getKey() {
            return key;
        }

        public final Ring.Node<OutEntry<K, V>> getNext() {
            return next;
        }

        public final OutEntry<K, V> getSelf() {
            return this;
        }

        public V getValue() {
            return get();
        }

        public int hashCode() {
            return safeHashCode(getKey()) ^ safeHashCode(getValue());
        }

        public final void setNext(final Ring.Node<OutEntry<K, V>> next) {
            this.next = next;
        }

        public V setValue(final V value) {
            throw new UnsupportedOperationException("setValue");
        }
    }

    /**
     * Value collection implementation for values().
     */
    private class ValueCollection extends AbstractCollection<V> {
        @Override
        public void clear() {
            TwoQueueCache.this.clear();
        }

        @Override
        public boolean contains(final Object value) {
            return TwoQueueCache.this.containsValue(value);
        }

        @Override
        public Iterator<V> iterator() {
            return new AbstractIterator<V>() {
                public V next() {
                    return nextEntry().getValue();
                }
            };
        }

        @Override
        public int size() {
            return TwoQueueCache.this.size();
        }
    }

    private static boolean safeEquals(final Object a, final Object b) {
        return a == b || (a != null && b != null && a.equals(b));
    }

    private static int safeHashCode(final Object a) {
        return a == null ? 0 : a.hashCode();
    }

    private final int inEntryCapacity;

    private int inEntryCount;

    private final Ring<InMEntry<K, V>> inEntryRing;

    private final HashMap<K, Entry<K, V>> map = new HashMap<K, Entry<K, V>>();

    private final int mEntryCapacity;

    private int mEntryCount;

    private final Ring<InMEntry<K, V>> mEntryRing;

    private final int outEntryCapacity;

    private int outEntryCount;

    private final Ring<OutEntry<K, V>> outEntryRing;

    public TwoQueueCache(final int capacity) {
        inEntryCapacity = capacity / 4;
        mEntryCapacity = capacity - inEntryCapacity;
        outEntryCapacity = capacity / 2;
        inEntryRing = new Ring<InMEntry<K, V>>();
        mEntryRing = new Ring<InMEntry<K, V>>();
        outEntryRing = new Ring<OutEntry<K, V>>();
    }

    public void clear() {
        inEntryRing.clear();
        inEntryCount = 0;
        outEntryRing.clear();
        outEntryCount = 0;
        mEntryRing.clear();
        mEntryCount = 0;
        map.clear();
    }

    public boolean containsKey(final Object key) {
        final Entry<K, V> entry = map.get(key);
        return entry instanceof InMEntry;
    }

    public boolean containsValue(final Object value) {
        // We do not answer for values in out entries even if we still have it.
        for (final Entry<K, V> e : map.values()) {
            if (e instanceof InMEntry && value.equals(e.getValue())) {
                return true;
            }
        }
        return false;
    }

    private void enforceCapacity() {
        while (inEntryCount > inEntryCapacity) {
            final InMEntry<K, V> entry = inEntryRing.removeTail().getSelf();
            final OutEntry<K, V> out = new OutEntry<K, V>(entry);
            map.put(entry.getKey(), out);
            outEntryRing.insert(out);
            outEntryCount += 1;
            inEntryCount -= 1;
        }
        while (outEntryCount > outEntryCapacity) {
            final OutEntry<K, V> entry = outEntryRing.removeTail().getSelf();
            map.remove(entry.getKey());
            outEntryCount -= 1;
        }
        while (mEntryCount > mEntryCapacity) {
            final InMEntry<K, V> entry = mEntryRing.removeTail().getSelf();
            map.remove(entry.getKey());
            mEntryCount -= 1;
        }
    }

    public Set<Entry<K, V>> entrySet() {
        return new EntrySet();
    }

    public V get(final Object key) {
        final Entry<K, V> entry = map.get(key);
        if (entry == null) {
            return null;
        }
        final V value = entry.getValue();
        if (entry instanceof OutEntry && value != null) {
            synchronized (this) {
                // Use the original key in case it is GC relevant
                final K originalKey = entry.getKey();
                final InMEntry<K, V> promote = new InMEntry<K, V>(entry);
                map.put(originalKey, promote);
                outEntryRing.remove((OutEntry<K, V>) entry);
                outEntryCount -= 1;
                mEntryRing.insert(promote);
                mEntryCount += 1;
                enforceCapacity();
            }
        }
        return value;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public Set<K> keySet() {
        return new KeySet();
    }

    public V put(final K key, final V value) {
        final InMEntry<K, V> entry = new InMEntry<K, V>(key, value);
        final Entry<K, V> old = map.put(key, entry);
        if (old instanceof OutEntry) {
            outEntryRing.remove((OutEntry<K, V>) old);
            outEntryCount -= 1;
            entry.isM = true; // instant promote
            mEntryRing.insert(entry);
            mEntryCount += 1;
        } else {
            inEntryRing.insert(entry);
            inEntryCount += 1;
        }
        enforceCapacity();
        return old == null ? null : old.getValue();
    }

    public void putAll(final Map<? extends K, ? extends V> t) {
        for (final Entry<? extends K, ? extends V> entry : t.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    public V remove(final Object key) {
        final Entry<K, V> old = map.get(key);
        if (old instanceof OutEntry) {
            outEntryCount -= 1;
            outEntryRing.remove((OutEntry<K, V>) old);
        } else if (old instanceof InMEntry) {
            final InMEntry<K, V> entry = (InMEntry<K, V>) old;
            if (entry.isM) {
                mEntryCount -= 1;
                mEntryRing.remove(entry);
            } else {
                inEntryCount -= 1;
                inEntryRing.remove(entry);
            }
        }
        return old == null ? null : old.getValue();
    }

    public int size() {
        return inEntryCount + mEntryCount;
    }

    public Collection<V> values() {
        return new ValueCollection();
    }

}
