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
package de.mobizcorp.lib;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Copyright(C) 2005 mobizcorp Europe Ltd., all rights reserved.
 */
public class InputStreamEater extends BufferedInputStream {

    private Process p;

    public InputStreamEater(InputStream in) {
        super(in);
    }
    
    public InputStreamEater(Process p) {
        this(p.getInputStream());
        this.p = p;
    }

    @Override
    public void close() throws IOException {
        try {
            int n;
            // Avoid broken pipe errors - skip to end.
            while ((n = available()) > 0) {
                super.skip(n);
            }
            if (p != null) {
                p.waitFor();
            }
        } catch (InterruptedException e) {
            throw new IOException(e.toString());
        }
        super.close();
    }
}
