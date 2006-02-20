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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Stream factory interface, abstracting on opening files.
 * 
 * @author Copyright(C) 2006 Klaus Rennecke, all rights reserved.
 */
public interface StreamFactory {
    public static class AtomicOutputStream extends FileOutputStream {

        private static File getParent(final File file) {
            final File parent = file.getParentFile();
            return parent == null ? file.getAbsoluteFile().getParentFile()
                    : parent;
        }

        private final File file;

        private File temp;

        public AtomicOutputStream(final File file) throws IOException {
            this(file, File.createTempFile(file.getName(), ".new",
                    getParent(file)));
        }

        private AtomicOutputStream(final File file, final File temp)
                throws FileNotFoundException {
            super(temp);
            this.temp = temp;
            this.file = file;
        }

        @Override
        public synchronized void close() throws IOException {
            File oldTemp = temp;
            temp = null;
            super.close();
            if (oldTemp != null) {
                // first attempt: direct overwrite by rename
                if (oldTemp.renameTo(file)) {
                    return;
                }
                if (!file.delete()) {
                    throw new IOException("cannot remove target file: " + file);
                }
                // second attempt: rename with old file deleted
                if (oldTemp.renameTo(file)) {
                    return;
                }
                // third attempt: copy file onto the target
                FileOutputStream out = new FileOutputStream(file);
                try {
                    FileInputStream in = new FileInputStream(oldTemp);
                    try {
                        final byte[] buffer = new byte[8192];
                        int n;
                        while ((n = in.read(buffer)) != -1) {
                            if (n > 0) {
                                out.write(buffer, 0, n);
                            }
                        }
                    } finally {
                        in.close();
                    }
                } finally {
                    out.close();
                    oldTemp.delete();
                }
            }
        }

    }

    public static class Local implements StreamFactory {
        
        public static final String SEPARATOR = File.separator;

        private final File base;

        public Local(final File base) {
            this.base = base;
        }

        public File file(final String path) {
            return new File(base, path);
        }

        public String join(final String path, final String name) {
            return path + SEPARATOR + name;
        }

        public OutputStream openAtomic(final String path) throws IOException {
            return new AtomicOutputStream(file(path));
        }

        public InputStream openInput(final String path) throws IOException {
            return new FileInputStream(file(path));
        }

        public OutputStream openOutput(final String path) throws IOException {
            return new FileOutputStream(file(path));
        }

    }

    public String join(String path, String name);

    public OutputStream openAtomic(String path) throws IOException;

    public InputStream openInput(String path) throws IOException;

    public OutputStream openOutput(String path) throws IOException;
}
