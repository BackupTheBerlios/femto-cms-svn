package de.mobizcorp.femtocms.qu8ax;

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
