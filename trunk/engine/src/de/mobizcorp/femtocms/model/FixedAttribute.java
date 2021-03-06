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
package de.mobizcorp.femtocms.model;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;

/**
 * @author Copyright(C) 2005 mobizcorp Europe Ltd., all rights reserved.
 */
public abstract class FixedAttribute extends FixedNode implements Attr {

    public void setValue(String value) throws DOMException {
        throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                "setValue");
    }

}
