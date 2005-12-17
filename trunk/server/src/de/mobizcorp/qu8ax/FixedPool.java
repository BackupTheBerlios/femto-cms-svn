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

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A fixed name pool intended to be used as a constant.
 * 
 * @author Copyright(C) 2005 Klaus Rennecke, all rights reserved.
 * 
 * @param <T>
 *            the key type, for instance Text or String.
 */
public class FixedPool<T> extends NamePool<T> {

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
