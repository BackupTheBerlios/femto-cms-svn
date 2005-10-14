package de.mobizcorp.femtocms.engine;

import java.io.InputStream;

/**
 * @author Copyright(C) 2005 mobizcorp Europe Ltd., all rights reserved.
 */
public class EmptySource extends StreamResource {

    public static class EmptyInputStream extends InputStream {

        public static final EmptyInputStream INSTANCE = new EmptyInputStream();

        private EmptyInputStream() {
        }

        @Override
        public int read() {
            return -1;
        }

    }

    public static final EmptySource INSTANCE = new EmptySource();

    private EmptySource() {
        super.setLastModified(1);
        super.setInputStream(EmptyInputStream.INSTANCE);
    }

    @Override
    public final void setLastModified(long lastModified) {
        throw new UnsupportedOperationException("setLastModified");
    }

}
