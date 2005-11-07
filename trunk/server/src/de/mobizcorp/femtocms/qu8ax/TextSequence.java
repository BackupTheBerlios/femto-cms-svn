/*
 * femtocms minimalistic content management.
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
package de.mobizcorp.femtocms.qu8ax;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A sequence of characters in Unicode, encoded as UTF-8 byte array.
 * 
 * @author Copyright(C) 2005 Klaus Rennecke, all rights reserved.
 */
public interface TextSequence {

	/**
	 * Create an iterator over this sequence.
	 * 
	 * @return the iterator.
	 */
	public TextIterator iterator();

	/**
	 * Answer the length of this text sequence in Unicode character points. Note
	 * that this may <em>not</em> be equal to toString().length() because Java
	 * characters are only UCS-2 and may require two characters to represenent
	 * one Unicode code point.
	 * 
	 * @return the number of characters in this text.
	 */
	public int length();

	/**
	 * Return a subsequence of this starting at <var>off</var>, with <var>len</var>
	 * octets.
	 * 
	 * @param off
	 *            start index in octets.
	 * @param len
	 *            size of result in octets.
	 * @return partial text sequence.
	 */
	public Text part(int off, int len);

	/**
	 * Answer the size of this text sequence in octets.
	 * 
	 * @return the size, in encoded bytes.
	 */
	public int size();

	/**
	 * Convert this text sequence to a standard Java String.
	 * 
	 * @return this text as a string.
	 */
	public String toString();

	/**
	 * Convert this text sequence to an immutable Text.
	 * 
	 * @return the Text.
	 */
	public Text toText();

	/**
	 * Write the data of this text sequence to the output byte array <var>out</var>.
	 * 
	 * @param out
	 *            the target byte array.
	 * @param off
	 *            offset into <var>out</var> where to start.
	 * @param len
	 *            amount of space in <var>out</var> counting from <var>off</var>.
	 * @return the actual amount of bytes copied.
	 */
	public int writeTo(byte[] out, int off, int len);

	/**
	 * Write the data of this text sequence to the given output stream.
	 * 
	 * @param out
	 *            an output stream.
	 * @throws IOException
	 *             propagated from I/O.
	 */
	public void writeTo(OutputStream out) throws IOException;
}
