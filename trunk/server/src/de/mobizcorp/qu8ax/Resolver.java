/*
 * Quick UTF-8 API for XML.
 * Copyright(C) 2005 Klaus Rennecke, all rights reserved.
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
package de.mobizcorp.qu8ax;

import java.io.IOException;
import java.io.InputStream;

/**
 * Resolver for internal and external entities.
 * 
 * @author Copyright(C) 2005 Klaus Rennecke, all rights reserved.
 */
public class Resolver {

    public static final Text ENT_AMP = Text.constant((byte) '&', (byte) 'a',
            (byte) 'm', (byte) 'p', (byte) ';');

    public static final Text ENT_GT = Text.constant((byte) '&', (byte) 'g',
            (byte) 't', (byte) ';');

    public static final Text ENT_LT = Text.constant((byte) '&', (byte) 'l',
            (byte) 't', (byte) ';');

    public static final Text ENT_QUOT = Text.constant((byte) '&', (byte) 'q',
            (byte) 'u', (byte) 'o', (byte) 't', (byte) ';');

    public static final Resolver INSTANCE = new Resolver();

    public static final Text NAME_AMP = Text.constant((byte) 'a', (byte) 'm',
            (byte) 'p');

    public static final Text NAME_GT = Text.constant((byte) 'g', (byte) 't');

    public static final Text NAME_LT = Text.constant((byte) 'l', (byte) 't');

    public static final Text NAME_QUOT = Text.constant((byte) 'q', (byte) 'u',
            (byte) 'o', (byte) 't');

    public static final Text VAL_AMP = Text.constant((byte) '&');

    public static final Text VAL_GT = Text.constant((byte) '>');

    public static final Text VAL_LT = Text.constant((byte) '<');

    public static final Text VAL_QUOT = Text.constant((byte) '"');

    protected Resolver() {
        // only instantiate extending classes
    }

    public InputStream resolveExternal(Text spec) throws IOException {
        throw new IOException("external entities not supported: " + spec);
    }

    public Text resolveInternal(Text spec) {
        if (NAME_AMP.equals(spec)) {
            return VAL_AMP;
        } else if (NAME_GT.equals(spec)) {
            return VAL_GT;
        } else if (NAME_LT.equals(spec)) {
            return VAL_LT;
        } else if (NAME_QUOT.equals(spec)) {
            return VAL_QUOT;
        } else {
            if (spec.size() > 1 && spec.getByte(0) == '#') {
                // character literal
                int c;
                if (spec.getByte(1) == 'x') {
                    c = spec.part(2, spec.size() - 2).toInt(16);
                } else {
                    c = spec.part(1, spec.size() - 1).toInt(10);
                }
                return new TextBuffer(4).append(c).toText();
            } else {
                return null;
            }
        }
    }
}
