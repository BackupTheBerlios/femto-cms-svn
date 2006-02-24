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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Lock file implementation.
 * 
 * @author Copyright(C) 2006 Klaus Rennecke, all rights reserved.
 */
public class Lock {

    public static class LockFailed extends Exception {

        public LockFailed(String message) {
            super(message);
        }
    }

    private final File file;

    private boolean held;

    private final Runnable onRelease;

    public Lock(final File file, final Runnable onRelease) {
        this.file = file;
        this.onRelease = onRelease;
    }

    public void lock(long timeout) throws LockFailed, InterruptedException {
        final long start = timeout == 0 ? 0 : System.currentTimeMillis();
        while  (!held) {
            LockFailed failure = null;
            try {
                tryLock();
                return;
            } catch (LockFailed e) {
                failure = e;
            }
            long rest = timeout > 0 ? timeout - System.currentTimeMillis()
                    + start : 1000;
            if (rest <= 0) {
                throw failure;
            }
            Thread.sleep(rest > 1000 ? 1000 : rest);
        }
    }

    private String readLock() {
        try {
            FileReader reader = new FileReader(file);
            try {
                final StringBuffer b = new StringBuffer();
                final char[] cb = new char[512];
                int n;
                while ((n = reader.read(cb)) != -1) {
                    if (n > 0) {
                        b.append(cb, 0, n);
                    }
                }
                return b.toString();
            } finally {
                reader.close();
            }
        } catch (IOException e) {
            return "";
        }
    }
    
    public void release() {
        if (held) {
            if (onRelease != null) {
                onRelease.run();
            }
            file.delete();
        }
    }

    public void tryLock() throws LockFailed {
        if (!held) {
            try {
                if (file.createNewFile()) {
                    held = true;
                    FileWriter writer = new FileWriter(file);
                    try {
                        writer.write(Integer.toString(System
                                .identityHashCode(Thread.currentThread())));
                    } finally {
                        writer.close();
                    }
                    return;
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            throw new LockFailed(readLock());
        }
    }
}
