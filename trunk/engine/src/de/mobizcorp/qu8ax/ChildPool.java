/*
 * Quick UTF-8 API for XML.
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
package de.mobizcorp.qu8ax;

/**
 * A mutable name pool that falls back to a fixed parent pool for undefined
 * identifiers.
 * 
 * @author Copyright(C) 2005 Klaus Rennecke, all rights reserved.
 * 
 * @param <T>
 *            the key type, for instance Text or String.
 */
public class ChildPool<T> extends OpenPool<T> {

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
