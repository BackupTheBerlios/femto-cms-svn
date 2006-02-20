/*
 * 水星 - Water Star.
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
package de.mobizcorp.水星;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.EmptyStackException;
import java.util.NoSuchElementException;
import java.util.Stack;

/**
 * Resource tree interface, to walk file structures. The traversal is guaranteed
 * to follow pre-order traversal, with nodes on the same level ordered by either
 * FileOrder or NameOrder. Path separators are always normalized to a forward
 * slash ('/').
 * 
 * @author Copyright(C) 2006 Klaus Rennecke, all rights reserved.
 */
public interface Walker {
    public static class FileOrder implements Comparator<File> {

        public static FileOrder INSTANCE = new FileOrder();

        public static int fileCompare(File a, File b) {
            return NameOrder.compare(a.getPath(), b.getPath(),
                    File.separatorChar);
        }

        public static String toName(String path) {
            return path.replace(File.separatorChar, '/');
        }

        public int compare(File a, File b) {
            return fileCompare(a, b);
        }
    }

    public static class FileWalker implements Walker {
        private final FilenameFilter filter;

        private final int prefixLength;

        private final Stack<File> stack = new Stack<File>();

        public FileWalker(final File base) {
            this(base, null);
        }

        public FileWalker(final File base, final FilenameFilter filter) {
            this(base, filter, base.listFiles());
        }

        public FileWalker(final File base, final FilenameFilter filter,
                final File... files) {
            this.filter = filter;
            if (base.isDirectory()) {
                prefixLength = new File(base, "dummy").getPath().length()
                        - "dummy".length();

                push(base, files);
            } else {
                throw new IllegalArgumentException("not a folder: " + base);
            }
        }

        public boolean hasNext() {
            return !stack.isEmpty();
        }

        public String next() {
            try {
                final File next = stack.pop();
                if (next.isDirectory()) {
                    push(next, next.listFiles());
                }
                final String path = next.getPath();
                return FileOrder.toName(prefixLength == 0 ? path : path
                        .substring(prefixLength));
            } catch (EmptyStackException e) {
                throw new NoSuchElementException(e.toString());
            }
        }

        private void push(final File base, final File... files) {
            Arrays.sort(files, ReverseOrder.INSTANCE);
            for (final File file : files) {
                if (filter == null || filter.accept(base, file.getName())) {
                    stack.push(file);
                }
            }
        }
    }

    public static class FilterWalker implements Walker {
        private final Walker delegate;

        private final Filter filter;

        private String nextElement;

        public FilterWalker(final Walker delegate, final Filter filter) {
            this.delegate = delegate;
            this.filter = filter;
        }

        public boolean hasNext() {
            while (nextElement == null && delegate.hasNext()) {
                final String candidate = delegate.next();
                if (filter.accept(candidate)) {
                    nextElement = candidate;
                }
            }
            return nextElement != null;
        }

        public String next() {
            if (hasNext()) {
                final String result = nextElement;
                nextElement = null;
                return result;
            } else {
                throw new NoSuchElementException();
            }
        }
    }

    public static class CollectionWalker implements Walker {

        private final String[] names;

        private int index;

        public CollectionWalker(final Collection<String> names) {
            this.names = names.toArray(new String[names.size()]);
            Arrays.sort(this.names, NameOrder.INSTANCE);
            this.index = 0;
        }

        public final boolean hasNext() {
            return index < names.length;
        }

        public final String next() {
            if (hasNext()) {
                return names[index++];
            } else {
                throw new NoSuchElementException();
            }
        }

    }

    public static class JoinWalker implements Walker {

        private final Walker[] walkers;

        private final String[] values;

        private int start;

        public JoinWalker(final Walker... walkers) {
            this.walkers = walkers;
            this.values = new String[walkers.length];
            for (int i = 0; i < walkers.length; i++) {
                if (walkers[i].hasNext()) {
                    values[i] = walkers[i].next();
                }
            }
            for (int i = 1; i < walkers.length; i++) {
                for (int j = i; j > 0
                        && NameOrder.nameCompare(values[j - 1], values[j]) > 0; j--) {
                    swap(j - 1, j);
                }
            }
        }

        public boolean hasNext() {
            return values[values.length - 1] != null;
        }

        public String next() {
            for (int i = start; i < values.length; i++) {
                if (values[i] != null) {
                    start = i;
                    final String result = values[i];
                    values[i] = walkers[i].hasNext() ? walkers[i].next() : null;
                    for (int j = i + 1; j < values.length
                            && NameOrder.nameCompare(values[j - 1], values[j]) > 0; j++) {
                        swap(j - 1, j);
                    }
                    return result;
                }
            }
            throw new NoSuchElementException();
        }

        private final void swap(int i, int j) {
            final Walker w = walkers[i];
            walkers[i] = walkers[j];
            walkers[j] = w;
            final String s = values[i];
            values[i] = values[j];
            values[j] = s;
        }

    }

    public static class UniqueWalker implements Walker {

        private final Walker delegate;

        private String next, prev;

        public UniqueWalker(final Walker delegate) {
            this.delegate = delegate;
        }

        public boolean hasNext() {
            while (next == null && delegate.hasNext()) {
                String candidate = delegate.next();
                if (prev == null || !prev.equals(candidate)) {
                    next = candidate;
                }
            }
            return next != null;
        }

        public String next() {
            if (hasNext()) {
                prev = next;
                next = null;
                return prev;
            } else {
                throw new NoSuchElementException();
            }
        }

    }

    public static class NameOrder implements Comparator<String> {

        public static final NameOrder INSTANCE = new NameOrder();

        public static int compare(final String a, final String b,
                final char separator) {
            if (a == b) {
                return 0;
            } else if (a == null) {
                return -1;
            } else if (b == null) {
                return 1;
            }
            final int la = a.length();
            final int lb = b.length();
            final int end = la > lb ? lb : la;
            for (int i = 0; i < end; i++) {
                final char ca = a.charAt(i);
                final char cb = b.charAt(i);
                if (ca != cb) {
                    return (ca == separator ? 0 : ca)
                            - (cb == separator ? 0 : cb);
                }
            }
            return la - lb;
        }

        public int compare(final String a, final String b) {
            return nameCompare(a, b);
        }

        public static int nameCompare(final String a, final String b) {
            return compare(a, b, '/');
        }

    }

    public static class ReverseOrder extends FileOrder {

        public static final ReverseOrder INSTANCE = new ReverseOrder();

        @Override
        public int compare(File a, File b) {
            return super.compare(a, b) * -1;
        }

    }

    public boolean hasNext();

    public String next();
}
