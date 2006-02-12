/*
 * Half User Interface.
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
package de.mobizcorp.hui;

/**
 * Path abstraction.
 * 
 * @author Copyright(C) 2005 Klaus Rennecke, all rights reserved.
 */
public class Path<T> {

    private final T node;
    
    private final Path<T> next;
    
    public Path(T node, Path<T> next) {
        this.node = node;
        this.next = next;
    }

    public Path getNext() {
        return next;
    }

    public T getNode() {
        return node;
    }
    
    public T getLast() {
        Path<T> scan = this;
        while (scan.next != null) {
            scan = scan.next;
        }
        return scan.node;
    }
    
    public T getParent(T node) {
        T last;
        Path<T> scan = this;
        while (scan != null) {
            last = scan.node;
            scan = scan.next;
            if (scan.node == node) {
                return last;
            }
        }
        return null;
    }
}
