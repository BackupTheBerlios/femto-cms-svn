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
import java.util.Arrays;
import java.util.Comparator;
import java.util.EmptyStackException;
import java.util.NoSuchElementException;
import java.util.Stack;

import de.mobizcorp.lib.Text;

/**
 * Resource tree interface, to walk file structures. The traversal is guaranteed
 * to follow pre-order traversal, with nodes on the same level ordered by either
 * FileOrder or NameOrder. Path separators are always normalized to a forward
 * slash ('/').
 * 
 * @author Copyright(C) 2006 Klaus Rennecke, all rights reserved.
 */
public interface Walker {
    public static class NameOrder implements Comparator<String> {

        public int compare(final String a, final String b) {
            return nameCompare(a, b);
        }

        public static int nameCompare(final String a, final String b) {
            if (a == b) {
                return 0;
            }
            final int la = a.length();
            final int lb = b.length();
            final int end = la > lb ? lb : la;
            for (int i = 0; i < end; i++) {
                final char ca = a.charAt(i);
                final char cb = b.charAt(i);
                if (ca != cb) {
                    return (ca == '/' ? 0 : ca) - (cb == '/' ? 0 : cb);
                }
            }
            return la - lb;
        }

    }

    public static class TextOrder implements Comparator<Text> {

        public int compare(final Text a, final Text b) {
            return textCompare(a, b);
        }

        public static int textCompare(final Text a, final Text b) {
            if (a == b) {
                return 0;
            }
            final int la = a.size();
            final int lb = b.size();
            final int end = la > lb ? lb : la;
            for (int i = 0; i < end; i++) {
                final byte ca = a.getByte(i);
                final byte cb = b.getByte(i);
                if (ca != cb) {
                    final int ua = a.getUnicode(i);
                    final int ub = b.getUnicode(i);
                    return (ua == '/' ? 0 : ua) - (ub == '/' ? 0 : ub);
                }
            }
            return la - lb;
        }

    }

    public static class FileOrder implements Comparator<File> {

        public static FileOrder INSTANCE = new FileOrder();

        public int compare(File a, File b) {
            return fileCompare(a, b);
        }

        public static int fileCompare(File a, File b) {
            return NameOrder.nameCompare(toName(a.getPath()), toName(b
                    .getPath()));
        }

        public static String toName(String path) {
            return path.replace(File.separatorChar, '/');
        }
    }

    public static class FileWalker implements Walker {
        private final Stack<File> stack = new Stack<File>();

        private final int prefixLength;

        public FileWalker(final File base) {
            if (base.isDirectory()) {
                pushList(base);
                prefixLength = new File(base, "dummy").getPath().length()
                        - "dummy".length();
            } else {
                stack.push(base);
                prefixLength = 0;
            }
        }

        public boolean hasNext() {
            return !stack.isEmpty();
        }

        public String next() {
            try {
                final File next = stack.pop();
                if (next.isDirectory()) {
                    pushList(next);
                }
                final String path = next.getPath();
                return FileOrder.toName(prefixLength == 0 ? path : path
                        .substring(prefixLength));
            } catch (EmptyStackException e) {
                throw new NoSuchElementException(e.toString());
            }
        }

        private void pushList(final File next) {
            final File[] files = next.listFiles();
            Arrays.sort(files, FileOrder.INSTANCE);
            for (final File file : files) {
                stack.push(file);
            }
        }
    }

    public boolean hasNext();

    public String next();
}
