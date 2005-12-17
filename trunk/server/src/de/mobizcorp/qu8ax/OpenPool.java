package de.mobizcorp.femtocms.qu8ax;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A mutable name pool that can be used collect identifiers. Closing the open
 * pool creates a fixed copy of the current pool.
 * 
 * @author Copyright(C) 2005 Klaus Rennecke, all rights reserved.
 * 
 * @param <T>
 *            the key type, for instance Text or String.
 */
public class OpenPool<T> extends NamePool<T> {

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
