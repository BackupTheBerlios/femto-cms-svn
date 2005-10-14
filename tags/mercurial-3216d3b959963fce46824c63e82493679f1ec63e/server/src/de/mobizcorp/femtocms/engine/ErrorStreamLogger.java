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
