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
