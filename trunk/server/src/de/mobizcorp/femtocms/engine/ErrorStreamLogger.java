/*
 * femtocms minimalistic content management.
 * Copyright(C) 2005 mobizcorp Europe Ltd., all rights reserved.
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
package de.mobizcorp.femtocms.engine;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Copyright(C) 2005 mobizcorp Europe Ltd., all rights reserved.
 */
public class ErrorStreamLogger extends Thread {
    private InputStream in;

    private final Process child;

    private int exitCode;

    private int count;

    private ErrorStreamLogger(InputStream in, Process child) {
        this.in = in;
        this.child = child;
    }

    public static ErrorStreamLogger attach(Process process) {
        ErrorStreamLogger result = new ErrorStreamLogger(process
                .getErrorStream(), process);
        result.start();
        return result;
    }

    @Override
    public void run() {
        try {
            int n;
            byte[] buffer = new byte[1024];
            while ((n = in.read(buffer)) > 0) {
                System.out.write(buffer, 0, n);
                System.out.flush();
                count += n;
            }
            if (child != null) {
                exitCode = child.waitFor();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public int getExitCode() throws InterruptedException {
        if (isAlive()) {
            join();
        }
        return exitCode;
    }

    public boolean ok() throws InterruptedException {
        return getExitCode() == 0 && count == 0;
    }
}
