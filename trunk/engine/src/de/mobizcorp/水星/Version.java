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

import java.io.DataInput;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import de.mobizcorp.lib.FNV1aHash;

/**
 * Version abstraction, serves as identifiers for versions.
 * 
 * @author Copyright(C) 2006 Klaus Rennecke, all rights reserved.
 */
public final class Version {
	private static final byte[] hashBuffer = new byte[8192];

	private static final MessageDigest hashDigest;

	public static final int ID_SIZE = 20;

	public static final Version NULL = new Version(new byte[ID_SIZE]);

	static {
		try {
			hashDigest = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	public static boolean check(byte[] data, Version id, Version p0, Version p1) {
		return Util.compare(id(id), hash(data, id(p0), id(p1))) == 0;
	}

	public static Version create(byte[] data, Version p1, Version p2) {
		byte[] id = hash(data, id(p1), id(p2));
		return NULL.isId(id) ? NULL : new Version(id);
	}

	public static Version create(ByteBuffer buffer) {
		byte[] id = new byte[ID_SIZE];
		buffer.get(id);
		return NULL.isId(id) ? NULL : new Version(id);
	}

	public static Version create(DataInput input) throws IOException {
		byte[] id = new byte[ID_SIZE];
		input.readFully(id);
		return NULL.isId(id) ? NULL : new Version(id);
	}

	public static Version create(InputStream data, Version p1, Version p2)
			throws IOException {
		byte[] id = hash(data, id(p1), id(p2));
		return NULL.isId(id) ? NULL : new Version(id);
	}

	public static Version create(final String hex) {
		byte[] id = Util.bin(hex);
		return NULL.isId(id) ? NULL : new Version(id);
	}

	public static synchronized byte[] hash(byte[] in, final byte[] p1,
			final byte[] p2) {
		final MessageDigest digest = hashDigest;
		digest.reset();
		update(digest, p1, p2);
		digest.update(in);
		return hashDigest.digest();
	}

	public static synchronized byte[] hash(final InputStream in,
			final byte[] p1, final byte[] p2) throws IOException {
		int n;
		final byte[] buffer = hashBuffer;
		final MessageDigest digest = hashDigest;
		digest.reset();
		update(digest, p1, p2);
		while ((n = in.read()) != -1) {
			if (n > 0) {
				digest.update(buffer, 0, n);
			}
		}
		return hashDigest.digest();
	}

	public static byte[] id(Version version) {
		return version == null ? null : version.id;
	}

	private static void update(final MessageDigest digest, final byte[] p1,
			final byte[] p2) {
		if (Util.compare(p1, p2) == 1) {
            if (p2 != null)
                digest.update(p2);
            if (p1 != null)
                digest.update(p1);
        } else {
            if (p1 != null)
                digest.update(p1);
            if (p2 != null)
                digest.update(p2);
		}
	}

	private final byte[] id;

	private Version(final byte[] id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else if (o instanceof Version) {
			Version other = (Version) o;
			return Util.compare(this.id, other.id) == 0;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return FNV1aHash.hash(id);
	}

	public boolean isId(byte[] id) {
		return Util.compare(this.id, id) == 0;
	}

	public String toString() {
		return Util.hex(id);
	}

}
