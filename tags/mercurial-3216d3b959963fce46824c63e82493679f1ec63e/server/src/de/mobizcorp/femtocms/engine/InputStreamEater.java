package de.mobizcorp.femtocms.engine;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Copyright(C) 2005 mobizcorp Europe Ltd., all rights reserved.
 */
public class InputStreamEater extends BufferedInputStream {

    public InputStreamEater(InputStream in) {
        super(in);
    }

    @Override
    public void close() throws IOException {
        try {
            int n;
            // Avoid broken pipe errors - skip to end.
            while ((n = available()) > 0) {
                super.skip(n);
            }
        } catch (IOException e) {
            // ignored
        }
        super.close();
    }
}
