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

    public static class Entry<V> {
        public final int id;

        public final V value;

        public Entry(int id, V value) {
            this.id = id;
            this.value = value;
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
