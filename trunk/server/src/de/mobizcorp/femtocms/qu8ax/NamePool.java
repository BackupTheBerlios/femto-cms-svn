/*
 * femtocms minimalistic content management.
 * Copyright(C) 2005 Klaus Rennecke, all rights reserved.
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
package de.mobizcorp.femtocms.qu8ax;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A set of generic name pool classes. These can be used to generate integer
 * identifiers for key objects.
 * 
 * @author Copyright(C) 2005 Klaus Rennecke, all rights reserved.
 * 
 * @param <T>
 *            the key type, for instance Text or String.
 */
public abstract class NamePool<T> {

    /**
     * A mutable name pool that falls back to a fixed parent pool for undefined
     * identifiers.
     * 
     * @author Copyright(C) 2005 Klaus Rennecke, all rights reserved.
     * 
     * @param <T>
     *            the key type, for instance Text or String.
     */
    public static class ChildPool<T> extends OpenPool<T> {

        private final FixedPool<T> parent;

        private final int split;

        public ChildPool(FixedPool<T> parent) {
            this.parent = parent;
            this.split = parent.reg.size();
        }

        @Override
        public FixedPool<T> close() {
            FixedPool<T> result = super.close();
            result.map.putAll(parent.map);
            result.reg.addAll(0, parent.reg);
            return result;
        }

        @Override
        public boolean contains(T t) {
            return super.contains(t) || parent.contains(t);
        }

        @Override
        public T extern(int id) {
            if (id < split) {
                return parent.extern(id);
            } else {
                return super.extern(id - split);
            }
        }

        @Override
        protected int register(T t) {
            Entry<T> entry = parent.map.get(t);
            if (null != entry) {
                return entry.id;
            }
            entry = new NamePool.Entry<T>(reg.size() + split, t);
            map.put(t, entry);
            reg.add(entry);
            return entry.id;
        }

    }

    public static class Entry<T> {
        public final int id;

        public final T value;

        public Entry(int id, T value) {
            this.id = id;
            this.value = value;
        }
    }

    /**
     * A fixed name pool intended to be used as a constant.
     * 
     * @author Copyright(C) 2005 Klaus Rennecke, all rights reserved.
     * 
     * @param <T>
     *            the key type, for instance Text or String.
     */
    public static class FixedPool<T> extends NamePool<T> {

        public FixedPool(HashMap<T, Entry<T>> map, ArrayList<Entry<T>> reg) {
            super(map, reg);
        }

        public ChildPool<T> open() {
            return new ChildPool<T>(this);
        }

        @Override
        protected int register(T t) {
            throw new UnsupportedOperationException("this pool is fixed");
        }

    }

    /**
     * A mutable name pool that can be used collect identifiers. Closing the
     * open pool creates a fixed copy of the current pool.
     * 
     * @author Copyright(C) 2005 Klaus Rennecke, all rights reserved.
     * 
     * @param <T>
     *            the key type, for instance Text or String.
     */
    public static class OpenPool<T> extends NamePool<T> {

        public FixedPool<T> close() {
            return new FixedPool<T>(new HashMap<T, Entry<T>>(map),
                    new ArrayList<Entry<T>>(reg));
        }

        @Override
        protected int register(T t) {
            Entry<T> entry = new NamePool.Entry<T>(reg.size(), t);
            map.put(t, entry);
            reg.add(entry);
            return entry.id;
        }
    }

    protected final HashMap<T, Entry<T>> map;

    protected final ArrayList<Entry<T>> reg;

    public NamePool() {
        this(new HashMap<T, Entry<T>>(), new ArrayList<Entry<T>>());
    }

    protected NamePool(HashMap<T, Entry<T>> map, ArrayList<Entry<T>> reg) {
        this.map = map;
        this.reg = reg;
    }

    public boolean contains(T t) {
        return map.containsKey(t);
    }

    public T extern(int id) {
        return reg.get(id).value;
    }

    public final int intern(T t) {
        Entry<T> entry = map.get(t);
        if (null != entry) {
            return entry.id;
        } else {
            return register(t);
        }
    }

    protected abstract int register(T t);
}
