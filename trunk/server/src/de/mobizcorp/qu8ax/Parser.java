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
import java.io.PushbackInputStream;
import java.util.Stack;

import de.mobizcorp.lib.Text;
import de.mobizcorp.lib.TextBuffer;

/**
 * Recursive descent parser for XML versions 1.0 and 1.1.
 * 
 * @author Copyright(C) 2005 Klaus Rennecke, all rights reserved.
 */
public class Parser {
    private static final class AttrNode {
        public final Text lName;

        public AttrNode next;

        public final Text value;

        public AttrNode(Text lName, Text value) {
            this.lName = lName;
            this.value = value;
        }
    }

    private static final class State {
        public final TextBuffer buffer = new TextBuffer();

        public final int emptyName;

        public final PushbackInputStream in;

        public final OpenPool<Text> lNamePool;

        public final Parser parser;

        public final OpenPool<NamePair> qNamePool;

        protected boolean standalone;

        public Text version;

        public State(InputStream in, Parser parser) {
            this.in = new PushbackInputStream(in, 8);
            this.parser = parser;
            this.lNamePool = new OpenPool<Text>();
            this.qNamePool = new OpenPool<NamePair>();
            this.emptyName = lNamePool.intern(Text.EMPTY);
        }

        public boolean isWhiteSpace(final int c) {
            return c == 0x20 || c == 0x9 || c == 0xD || c == 0xA;
        }

        public boolean literal(final int w) throws IOException {
            int c = read();
            if (c == w) {
                return true;
            }
            if (c != -1) {
                unread();
            }
            return false;
        }

        public boolean literal(final Text t) throws IOException {
            for (int i = 0; i < t.size(); i++) {
                int c = in.read();
                if (c != (t.getByte(i) & 0xFF)) {
                    if (c != -1) {
                        in.unread(c);
                    }
                    while (--i >= 0) {
                        in.unread(t.getByte(i) & 0xFF);
                    }
                    return false;
                }
            }
            return true;
        }

        public int magic() throws IOException {
            int c = 0, n;
            byte[] buf = new byte[4];
            while ((n = 4 - c) > 0 && (n = in.read(buf, c, n)) != -1) {
                if (n > 0) {
                    c += n;
                }
            }
            in.unread(buf, 0, c);
            if (c != 4) {
                return 0;
            }
            return ((buf[0] & 0xFF) << 24) | ((buf[1] & 0xFF) << 16)
                    | ((buf[2] & 0xFF) << 8) | (buf[3] & 0xFF);
        }

        public int quote() throws IOException {
            int q = read();
            if (q == '\'' || q == '"') {
                return q;
            }
            unread();
            throw new SyntaxError("'\\'' or '\"' expected after '" + buffer
                    + "'");
        }

        public boolean quote(final int q) throws IOException {
            int c = read();
            if (c == q) {
                return true;
            }
            unread();
            throw new SyntaxError("'"
                    + (q == '\'' ? "\'" : String.valueOf((char) q))
                    + "' expected after '" + buffer + "'");
        }

        public int read() throws IOException {
            int b = in.read();
            if (b == -1) {
                return -1;
            }
            buffer.append((byte) b);
            if (b < 0x80) {
                return b;
            }
            int m = 0x3F, c = b;
            while ((c & m) != c) {
                b = in.read();
                if (b == -1) {
                    return -1;
                }
                buffer.append((byte) b);
                if ((b & 0xC0) != 0x80) {
                    throw new IOException("invalid utf8 byte: 0x"
                            + Integer.toHexString(b));
                }
                c = (c & m) << 6 | (b & 0x3F);
                m = (m << 5) | 0x1F;
            }
            return c;
        }

        public void unread() throws IOException {
            int scan = buffer.size();
            while (--scan >= 0) {
                byte b = buffer.getByte(scan);
                if ((b & 0xC0) != 0x80) {
                    break;
                }
            }
            int n = buffer.size() - scan;
            in.unread(buffer.getData(), scan, n);
            buffer.chop(scan, n);
        }

        public void unreadChar(int c) throws IOException {
            if (c < 0x80) {
                in.unread(c);
                buffer.chop(-1, 1);
            } else if (c < 0x800) {
                in.unread(0xC0 | ((c >>> 6) & 0x1F));
                in.unread(0x80 | (c & 0x3F));
                buffer.chop(-1, 2);
            } else if (c < 0x10000) {
                in.unread(0xE0 | ((c >>> 12) & 0x0F));
                in.unread(0x80 | ((c >>> 6) & 0x3F));
                in.unread(0x80 | (c & 0x3F));
                buffer.chop(-1, 3);
            } else {
                in.unread(0xF0 | ((c >>> 18) & 0x07));
                in.unread(0x80 | ((c >>> 12) & 0x3F));
                in.unread(0x80 | ((c >>> 6) & 0x3F));
                in.unread(0x80 | (c & 0x3F));
                buffer.chop(-1, 4);
            }
        }

        public int white() throws IOException {
            int c, n = 0;
            while ((c = read()) != -1) {
                if (isWhiteSpace(c)) {
                    n++;
                } else {
                    unread();
                    break;
                }
            }
            return n;
        }
    }

    private static class Syntax10 {

        public final void checkValid(final int c) throws IOException {
            if (!isValidChar(c)) {
                throw new SyntaxError("invalid character #x"
                        + Integer.toHexString(c));
            }
        }

        public boolean isBaseChar(final int c) {
            return (0x0041 <= c && c <= 0x005A) || (0x0061 <= c && c <= 0x007A)
                    || (0x00C0 <= c && c <= 0x00D6)
                    || (0x00D8 <= c && c <= 0x00F6)
                    || (0x00F8 <= c && c <= 0x00FF)
                    || (0x0100 <= c && c <= 0x0131)
                    || (0x0134 <= c && c <= 0x013E)
                    || (0x0141 <= c && c <= 0x0148)
                    || (0x014A <= c && c <= 0x017E)
                    || (0x0180 <= c && c <= 0x01C3)
                    || (0x01CD <= c && c <= 0x01F0)
                    || (0x01F4 <= c && c <= 0x01F5)
                    || (0x01FA <= c && c <= 0x0217)
                    || (0x0250 <= c && c <= 0x02A8)
                    || (0x02BB <= c && c <= 0x02C1) || c == 0x0386
                    || (0x0388 <= c && c <= 0x038A) || c == 0x038C
                    || (0x038E <= c && c <= 0x03A1)
                    || (0x03A3 <= c && c <= 0x03CE)
                    || (0x03D0 <= c && c <= 0x03D6) || c == 0x03DA
                    || c == 0x03DC || c == 0x03DE || c == 0x03E0
                    || (0x03E2 <= c && c <= 0x03F3)
                    || (0x0401 <= c && c <= 0x040C)
                    || (0x040E <= c && c <= 0x044F)
                    || (0x0451 <= c && c <= 0x045C)
                    || (0x045E <= c && c <= 0x0481)
                    || (0x0490 <= c && c <= 0x04C4)
                    || (0x04C7 <= c && c <= 0x04C8)
                    || (0x04CB <= c && c <= 0x04CC)
                    || (0x04D0 <= c && c <= 0x04EB)
                    || (0x04EE <= c && c <= 0x04F5)
                    || (0x04F8 <= c && c <= 0x04F9)
                    || (0x0531 <= c && c <= 0x0556) || c == 0x0559
                    || (0x0561 <= c && c <= 0x0586)
                    || (0x05D0 <= c && c <= 0x05EA)
                    || (0x05F0 <= c && c <= 0x05F2)
                    || (0x0621 <= c && c <= 0x063A)
                    || (0x0641 <= c && c <= 0x064A)
                    || (0x0671 <= c && c <= 0x06B7)
                    || (0x06BA <= c && c <= 0x06BE)
                    || (0x06C0 <= c && c <= 0x06CE)
                    || (0x06D0 <= c && c <= 0x06D3) || c == 0x06D5
                    || (0x06E5 <= c && c <= 0x06E6)
                    || (0x0905 <= c && c <= 0x0939) || c == 0x093D
                    || (0x0958 <= c && c <= 0x0961)
                    || (0x0985 <= c && c <= 0x098C)
                    || (0x098F <= c && c <= 0x0990)
                    || (0x0993 <= c && c <= 0x09A8)
                    || (0x09AA <= c && c <= 0x09B0) || c == 0x09B2
                    || (0x09B6 <= c && c <= 0x09B9)
                    || (0x09DC <= c && c <= 0x09DD)
                    || (0x09DF <= c && c <= 0x09E1)
                    || (0x09F0 <= c && c <= 0x09F1)
                    || (0x0A05 <= c && c <= 0x0A0A)
                    || (0x0A0F <= c && c <= 0x0A10)
                    || (0x0A13 <= c && c <= 0x0A28)
                    || (0x0A2A <= c && c <= 0x0A30)
                    || (0x0A32 <= c && c <= 0x0A33)
                    || (0x0A35 <= c && c <= 0x0A36)
                    || (0x0A38 <= c && c <= 0x0A39)
                    || (0x0A59 <= c && c <= 0x0A5C) || c == 0x0A5E
                    || (0x0A72 <= c && c <= 0x0A74)
                    || (0x0A85 <= c && c <= 0x0A8B) || c == 0x0A8D
                    || (0x0A8F <= c && c <= 0x0A91)
                    || (0x0A93 <= c && c <= 0x0AA8)
                    || (0x0AAA <= c && c <= 0x0AB0)
                    || (0x0AB2 <= c && c <= 0x0AB3)
                    || (0x0AB5 <= c && c <= 0x0AB9) || c == 0x0ABD
                    || c == 0x0AE0 || (0x0B05 <= c && c <= 0x0B0C)
                    || (0x0B0F <= c && c <= 0x0B10)
                    || (0x0B13 <= c && c <= 0x0B28)
                    || (0x0B2A <= c && c <= 0x0B30)
                    || (0x0B32 <= c && c <= 0x0B33)
                    || (0x0B36 <= c && c <= 0x0B39) || c == 0x0B3D
                    || (0x0B5C <= c && c <= 0x0B5D)
                    || (0x0B5F <= c && c <= 0x0B61)
                    || (0x0B85 <= c && c <= 0x0B8A)
                    || (0x0B8E <= c && c <= 0x0B90)
                    || (0x0B92 <= c && c <= 0x0B95)
                    || (0x0B99 <= c && c <= 0x0B9A) || c == 0x0B9C
                    || (0x0B9E <= c && c <= 0x0B9F)
                    || (0x0BA3 <= c && c <= 0x0BA4)
                    || (0x0BA8 <= c && c <= 0x0BAA)
                    || (0x0BAE <= c && c <= 0x0BB5)
                    || (0x0BB7 <= c && c <= 0x0BB9)
                    || (0x0C05 <= c && c <= 0x0C0C)
                    || (0x0C0E <= c && c <= 0x0C10)
                    || (0x0C12 <= c && c <= 0x0C28)
                    || (0x0C2A <= c && c <= 0x0C33)
                    || (0x0C35 <= c && c <= 0x0C39)
                    || (0x0C60 <= c && c <= 0x0C61)
                    || (0x0C85 <= c && c <= 0x0C8C)
                    || (0x0C8E <= c && c <= 0x0C90)
                    || (0x0C92 <= c && c <= 0x0CA8)
                    || (0x0CAA <= c && c <= 0x0CB3)
                    || (0x0CB5 <= c && c <= 0x0CB9) || c == 0x0CDE
                    || (0x0CE0 <= c && c <= 0x0CE1)
                    || (0x0D05 <= c && c <= 0x0D0C)
                    || (0x0D0E <= c && c <= 0x0D10)
                    || (0x0D12 <= c && c <= 0x0D28)
                    || (0x0D2A <= c && c <= 0x0D39)
                    || (0x0D60 <= c && c <= 0x0D61)
                    || (0x0E01 <= c && c <= 0x0E2E) || c == 0x0E30
                    || (0x0E32 <= c && c <= 0x0E33)
                    || (0x0E40 <= c && c <= 0x0E45)
                    || (0x0E81 <= c && c <= 0x0E82) || c == 0x0E84
                    || (0x0E87 <= c && c <= 0x0E88) || c == 0x0E8A
                    || c == 0x0E8D || (0x0E94 <= c && c <= 0x0E97)
                    || (0x0E99 <= c && c <= 0x0E9F)
                    || (0x0EA1 <= c && c <= 0x0EA3) || c == 0x0EA5
                    || c == 0x0EA7 || (0x0EAA <= c && c <= 0x0EAB)
                    || (0x0EAD <= c && c <= 0x0EAE) || c == 0x0EB0
                    || (0x0EB2 <= c && c <= 0x0EB3) || c == 0x0EBD
                    || (0x0EC0 <= c && c <= 0x0EC4)
                    || (0x0F40 <= c && c <= 0x0F47)
                    || (0x0F49 <= c && c <= 0x0F69)
                    || (0x10A0 <= c && c <= 0x10C5)
                    || (0x10D0 <= c && c <= 0x10F6) || c == 0x1100
                    || (0x1102 <= c && c <= 0x1103)
                    || (0x1105 <= c && c <= 0x1107) || c == 0x1109
                    || (0x110B <= c && c <= 0x110C)
                    || (0x110E <= c && c <= 0x1112) || c == 0x113C
                    || c == 0x113E || c == 0x1140 || c == 0x114C || c == 0x114E
                    || c == 0x1150 || (0x1154 <= c && c <= 0x1155)
                    || c == 0x1159 || (0x115F <= c && c <= 0x1161)
                    || c == 0x1163 || c == 0x1165 || c == 0x1167 || c == 0x1169
                    || (0x116D <= c && c <= 0x116E)
                    || (0x1172 <= c && c <= 0x1173) || c == 0x1175
                    || c == 0x119E || c == 0x11A8 || c == 0x11AB
                    || (0x11AE <= c && c <= 0x11AF)
                    || (0x11B7 <= c && c <= 0x11B8) || c == 0x11BA
                    || (0x11BC <= c && c <= 0x11C2) || c == 0x11EB
                    || c == 0x11F0 || c == 0x11F9
                    || (0x1E00 <= c && c <= 0x1E9B)
                    || (0x1EA0 <= c && c <= 0x1EF9)
                    || (0x1F00 <= c && c <= 0x1F15)
                    || (0x1F18 <= c && c <= 0x1F1D)
                    || (0x1F20 <= c && c <= 0x1F45)
                    || (0x1F48 <= c && c <= 0x1F4D)
                    || (0x1F50 <= c && c <= 0x1F57) || c == 0x1F59
                    || c == 0x1F5B || c == 0x1F5D
                    || (0x1F5F <= c && c <= 0x1F7D)
                    || (0x1F80 <= c && c <= 0x1FB4)
                    || (0x1FB6 <= c && c <= 0x1FBC) || c == 0x1FBE
                    || (0x1FC2 <= c && c <= 0x1FC4)
                    || (0x1FC6 <= c && c <= 0x1FCC)
                    || (0x1FD0 <= c && c <= 0x1FD3)
                    || (0x1FD6 <= c && c <= 0x1FDB)
                    || (0x1FE0 <= c && c <= 0x1FEC)
                    || (0x1FF2 <= c && c <= 0x1FF4)
                    || (0x1FF6 <= c && c <= 0x1FFC) || c == 0x2126
                    || (0x212A <= c && c <= 0x212B) || c == 0x212E
                    || (0x2180 <= c && c <= 0x2182)
                    || (0x3041 <= c && c <= 0x3094)
                    || (0x30A1 <= c && c <= 0x30FA)
                    || (0x3105 <= c && c <= 0x312C)
                    || (0xAC00 <= c && c <= 0xD7A3);
        }

        public boolean isCombiningChar(final int c) {
            return (0x0300 <= c && c <= 0x0345) || (0x0360 <= c && c <= 0x0361)
                    || (0x0483 <= c && c <= 0x0486)
                    || (0x0591 <= c && c <= 0x05A1)
                    || (0x05A3 <= c && c <= 0x05B9)
                    || (0x05BB <= c && c <= 0x05BD) || c == 0x05BF
                    || (0x05C1 <= c && c <= 0x05C2) || c == 0x05C4
                    || (0x064B <= c && c <= 0x0652) || c == 0x0670
                    || (0x06D6 <= c && c <= 0x06DC)
                    || (0x06DD <= c && c <= 0x06DF)
                    || (0x06E0 <= c && c <= 0x06E4)
                    || (0x06E7 <= c && c <= 0x06E8)
                    || (0x06EA <= c && c <= 0x06ED)
                    || (0x0901 <= c && c <= 0x0903) || c == 0x093C
                    || (0x093E <= c && c <= 0x094C) || c == 0x094D
                    || (0x0951 <= c && c <= 0x0954)
                    || (0x0962 <= c && c <= 0x0963)
                    || (0x0981 <= c && c <= 0x0983) || c == 0x09BC
                    || c == 0x09BE || c == 0x09BF
                    || (0x09C0 <= c && c <= 0x09C4)
                    || (0x09C7 <= c && c <= 0x09C8)
                    || (0x09CB <= c && c <= 0x09CD) || c == 0x09D7
                    || (0x09E2 <= c && c <= 0x09E3) || c == 0x0A02
                    || c == 0x0A3C || c == 0x0A3E || c == 0x0A3F
                    || (0x0A40 <= c && c <= 0x0A42)
                    || (0x0A47 <= c && c <= 0x0A48)
                    || (0x0A4B <= c && c <= 0x0A4D)
                    || (0x0A70 <= c && c <= 0x0A71)
                    || (0x0A81 <= c && c <= 0x0A83) || c == 0x0ABC
                    || (0x0ABE <= c && c <= 0x0AC5)
                    || (0x0AC7 <= c && c <= 0x0AC9)
                    || (0x0ACB <= c && c <= 0x0ACD)
                    || (0x0B01 <= c && c <= 0x0B03) || c == 0x0B3C
                    || (0x0B3E <= c && c <= 0x0B43)
                    || (0x0B47 <= c && c <= 0x0B48)
                    || (0x0B4B <= c && c <= 0x0B4D)
                    || (0x0B56 <= c && c <= 0x0B57)
                    || (0x0B82 <= c && c <= 0x0B83)
                    || (0x0BBE <= c && c <= 0x0BC2)
                    || (0x0BC6 <= c && c <= 0x0BC8)
                    || (0x0BCA <= c && c <= 0x0BCD) || c == 0x0BD7
                    || (0x0C01 <= c && c <= 0x0C03)
                    || (0x0C3E <= c && c <= 0x0C44)
                    || (0x0C46 <= c && c <= 0x0C48)
                    || (0x0C4A <= c && c <= 0x0C4D)
                    || (0x0C55 <= c && c <= 0x0C56)
                    || (0x0C82 <= c && c <= 0x0C83)
                    || (0x0CBE <= c && c <= 0x0CC4)
                    || (0x0CC6 <= c && c <= 0x0CC8)
                    || (0x0CCA <= c && c <= 0x0CCD)
                    || (0x0CD5 <= c && c <= 0x0CD6)
                    || (0x0D02 <= c && c <= 0x0D03)
                    || (0x0D3E <= c && c <= 0x0D43)
                    || (0x0D46 <= c && c <= 0x0D48)
                    || (0x0D4A <= c && c <= 0x0D4D) || c == 0x0D57
                    || c == 0x0E31 || (0x0E34 <= c && c <= 0x0E3A)
                    || (0x0E47 <= c && c <= 0x0E4E) || c == 0x0EB1
                    || (0x0EB4 <= c && c <= 0x0EB9)
                    || (0x0EBB <= c && c <= 0x0EBC)
                    || (0x0EC8 <= c && c <= 0x0ECD)
                    || (0x0F18 <= c && c <= 0x0F19) || c == 0x0F35
                    || c == 0x0F37 || c == 0x0F39 || c == 0x0F3E || c == 0x0F3F
                    || (0x0F71 <= c && c <= 0x0F84)
                    || (0x0F86 <= c && c <= 0x0F8B)
                    || (0x0F90 <= c && c <= 0x0F95) || c == 0x0F97
                    || (0x0F99 <= c && c <= 0x0FAD)
                    || (0x0FB1 <= c && c <= 0x0FB7) || c == 0x0FB9
                    || (0x20D0 <= c && c <= 0x20DC) || c == 0x20E1
                    || (0x302A <= c && c <= 0x302F) || c == 0x3099
                    || c == 0x309A;
        }

        public boolean isDigit(int c) {
            return (0x0030 <= c && c <= 0x0039) || (0x0660 <= c && c <= 0x0669)
                    || (0x06F0 <= c && c <= 0x06F9)
                    || (0x0966 <= c && c <= 0x096F)
                    || (0x09E6 <= c && c <= 0x09EF)
                    || (0x0A66 <= c && c <= 0x0A6F)
                    || (0x0AE6 <= c && c <= 0x0AEF)
                    || (0x0B66 <= c && c <= 0x0B6F)
                    || (0x0BE7 <= c && c <= 0x0BEF)
                    || (0x0C66 <= c && c <= 0x0C6F)
                    || (0x0CE6 <= c && c <= 0x0CEF)
                    || (0x0D66 <= c && c <= 0x0D6F)
                    || (0x0E50 <= c && c <= 0x0E59)
                    || (0x0ED0 <= c && c <= 0x0ED9)
                    || (0x0F20 <= c && c <= 0x0F29);
        }

        public boolean isExtender(final int c) {
            return c == 0x00B7 || c == 0x02D0 || c == 0x02D1 || c == 0x0387
                    || c == 0x0640 || c == 0x0E46 || c == 0x0EC6 || c == 0x3005
                    || (0x3031 <= c && c <= 0x3035)
                    || (0x309D <= c && c <= 0x309E)
                    || (0x30FC <= c && c <= 0x30FE);
        }

        public boolean isIdeographic(final int c) {
            return (0x4E00 <= c && c <= 0x9FA5) || c == 0x3007
                    | (0x3021 <= c && c <= 0x3029);
        }

        public boolean isLetter(final int c) {
            return isBaseChar(c) || isIdeographic(c);
        }

        public boolean isNameChar(final int c) {
            return isLetter(c) || isDigit(c) || c == '.' || c == '-'
                    || c == '_' || c == ':' || isCombiningChar(c)
                    || isExtender(c);
        }

        public boolean isNameStartChar(final int c) {
            return isLetter(c) || c == '_' || c == ':';
        }

        // Char ::= #x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD] |
        // [#x10000-#x10FFFF]
        public boolean isValidChar(final int c) throws IOException {
            return c == 0x9 || c == 0xA || c == 0xD
                    || (0x20 <= c && c <= 0xD7FF)
                    || (0xE000 <= c && c <= 0xFFFD)
                    || (0x10000 <= c && c <= 0x10FFFF);
        }

        protected AttrNode nt_Attribute(final State s, final Stack<NamePair> c,
                final AttrNode attrRing) throws IOException {
            int start = s.buffer.size();
            if (!nt_Name(s)) {
                return attrRing;
            }
            Text name = s.buffer.part(start, s.buffer.size() - start);
            if (s.read() != '=') {
                throw new SyntaxError("equal sign expected after '" + s.buffer
                        + "'");
            }
            Text value = nt_AttValue(s);
            if (value == null) {
                throw new SyntaxError("attribute value expected after '"
                        + s.buffer + "'");
            }
            if (name.equals(XMLNS)) {
                final int px = s.lNamePool.intern(Text.EMPTY);
                c.push(new NamePair(px, s.lNamePool.intern(value)));
                return attrRing;
            } else if (name.startsWith(XMLNS_)) {
                final Text prefix = name.part(XMLNS_.size(), name.size()
                        - XMLNS_.size());
                final int px = s.lNamePool.intern(prefix);
                c.push(new NamePair(px, s.lNamePool.intern(value)));
                return attrRing;
            }
            AttrNode node = new AttrNode(name, value);
            if (attrRing == null) {
                node.next = node;
            } else {
                node.next = attrRing.next;
                attrRing.next = node;
            }
            return node;
        }

        // AttValue ::= '"' ([^<&"] | Reference)* '"' | "'" ([^<&'] |
        // Reference)* "'"
        public Text nt_AttValue(final State s) throws IOException {
            int before = s.buffer.size();
            int q = s.read();
            if (q != '"' && q != '\'') {
                if (q != -1) {
                    s.unread();
                }
                return null;
            }
            int c;
            do {
                checkValid(c = s.read());
                if (c == '<') {
                    throw new SyntaxError(
                            "illegal character in attribute value: '<'");
                }
                if (c == -1) {
                    throw new SyntaxError("end of file in attribute value");
                }
                if (c == '&') {
                    s.unread();
                    int start = s.buffer.size();
                    if (!nt_EntityRef(s)) {
                        throw new SyntaxError(
                                "invalid entity reference in attribute value");
                    }
                    int len = s.buffer.size() - start;
                    Text entityName = s.buffer.part(start + 1, len - 2);
                    s.buffer.chop(start, len);
                    s.buffer.append(s.parser.resolver
                            .resolveInternal(entityName));
                }
            } while (c != q);
            return s.buffer.part(before + 1, s.buffer.size() - before - 2);
        }

        protected boolean nt_CDSect(final State s, final Handler h)
                throws IOException {
            if (!s.literal(XML_CDATA_START)) {
                return false;
            }
            s.buffer.clear();
            for (;;) {
                int c = s.read();
                if (c == ']') {
                    c = s.read();
                    if (c != ']') {
                        if (c != -1) {
                            s.unread();
                        }
                        continue;
                    }
                    c = s.read();
                    if (c != '>') {
                        if (c != -1) {
                            s.unread();
                        }
                        s.unread();
                        continue;
                    }
                    break;
                } else if (c == -1) {
                    throw new SyntaxError(
                            "end of file in CDATA section after '" + s.buffer
                                    + "'");
                } else if (!isValidChar(c)) {
                    s.unread();
                    throw new SyntaxError("invalid character 0x"
                            + Integer.toHexString(c)
                            + " in CDATA section after '" + s.buffer + "'");
                }
            }
            h.handleCharacterData(false, s.buffer.part(0, s.buffer.size() - 3));
            return true;
        }

        protected void nt_CharData(final State s, final Handler h)
                throws IOException {
            s.buffer.clear();
            for (;;) {
                int c = s.read();
                if (c == '<' || c == '&') {
                    s.unread();
                    break;
                } else if (c == ']') {
                    c = s.read();
                    if (c != ']') {
                        if (c != -1) {
                            s.unread();
                        }
                        continue;
                    }
                    c = s.read();
                    if (c != '>') {
                        if (c != -1) {
                            s.unread();
                        }
                        s.unread();
                        continue;
                    }
                    s.unread();
                    s.unread();
                    s.unread();
                    break;
                } else if (!isValidChar(c)) {
                    s.unread();
                    throw new SyntaxError("invalid character 0x"
                            + Integer.toHexString(c) + " after '" + s.buffer
                            + "'");
                }
            }
            if (s.buffer.size() > 0) {
                h.handleCharacterData(true, s.buffer.toText());
            }
        }

        // Comment ::= '<!--' ((Char - '-') | ('-' (Char - '-')))* '-->'
        public boolean nt_Comment(final State s, final Handler h)
                throws IOException {
            if (s.literal(XML_COMMENT_START)) {
                int c = -1, p, start = s.buffer.size();
                do {
                    p = c;
                    checkValid(c = s.read());
                } while (c != -1 && !(p == '-' && c == '-'));
                if (c == -1) {
                    throw new SyntaxError("end of file inside comment");
                } else {
                    // Unread the -- and grab text
                    s.unread();
                    s.unread();
                    Text t = s.buffer.part(start, s.buffer.size() - start);
                    if (!s.literal(XML_COMMENT_END)) {
                        throw new SyntaxError("'--' inside comment");
                    } else {
                        s.buffer.clear();
                        h.handleWhitespace(true, t);
                        return true;
                    }
                }
            } else {
                return false;
            }
        }

        protected boolean nt_Content(final State s, Stack<NamePair> c,
                final Parser parser) throws IOException {
            nt_CharData(s, parser.handler);
            while (nt_Element(s, c, parser) || nt_Reference(s, parser)
                    || nt_CDSect(s, parser.handler) || nt_PI(s, parser.handler)
                    || nt_Comment(s, parser.handler)) {
                nt_CharData(s, parser.handler);
            }
            return true; // nt_content is allowed to match nothing
        }

        protected boolean nt_Element(final State s, final Stack<NamePair> c,
                final Parser parser) throws IOException {
            // STag ::= '<' Name (S Attribute)* S? '>'
            if (!s.literal('<')) {
                return false;
            }
            int start = s.buffer.size();
            if (!nt_Name(s)) {
                s.unread(); // recover the '<'
                return false;
            }
            int depth = c.size();
            try {
                Text name = s.buffer.part(start, s.buffer.size() - start);
                AttrNode attrRing = null;
                while (nt_S(s)) {
                    attrRing = nt_Attribute(s, c, attrRing);
                }
                boolean isEmpty = s.literal('/');
                if (!s.literal('>')) {
                    throw new SyntaxError("'>' expected after '" + s.buffer
                            + "'");
                }
                int qName = parser.qualify(s, name, c, false);
                parser.handler.handleStartElement(qName);
                if (attrRing != null) {
                    AttrNode scan = attrRing;
                    do {
                        scan = scan.next;
                        parser.handler.handleAddAttribute(parser.qualify(s,
                                scan.lName, c, true), scan.value);
                    } while (scan != attrRing);
                }
                if (!isEmpty) {
                    parser.handler.handleOpenElement(qName);
                    nt_Content(s, c, parser);
                    s.buffer.clear();
                    if (!s.literal('<') || !s.literal('/') || !s.literal(name)) {
                        throw new SyntaxError("unterminated element '" + name
                                + "'");
                    } else {
                        nt_S(s); // optional
                    }
                    if (!s.literal('>')) {
                        throw new SyntaxError("'>' expected after '" + s.buffer
                                + "'");
                    }
                    parser.handler.handleCloseElement(qName);
                }
                return true;
            } finally {
                c.setSize(depth);
            }
        }

        // EncName ::= [A-Za-z] ([A-Za-z0-9._] | '-')*
        public Text nt_EncName(final State s) throws IOException {
            int start = s.buffer.size();
            int c = s.read();
            if (!('A' <= c && c <= 'Z') && !('a' <= c && c <= 'z')) {
                s.unread();
                throw new SyntaxError("invalid encoding name after '"
                        + s.buffer + "'");
            }
            do {
                c = s.read();
            } while (('A' <= c && c <= 'Z') || ('a' <= c && c <= 'z')
                    || ('0' <= c && c <= '9') || c == '.' || c == '_'
                    || c == '-');
            if (c != -1) {
                s.unread();
            }
            return s.buffer.part(start, s.buffer.size() - start);
        }

        public boolean nt_EntityRef(final State s) throws IOException {
            int c = s.read();
            if (c != '&') {
                if (c != -1) {
                    s.unread();
                }
                return false;
            }
            c = s.read();
            if (c == '#') {
                // FIXME: this is copy-paste from nt_Reference
                int radix;
                c = s.read();
                if (c == 'x') {
                    radix = 16;
                    c = s.read();
                } else {
                    radix = 10;
                }
                while (Character.digit(c, radix) != -1) {
                    c = s.read();
                }
                if (c != ';') {
                    if (c != -1) {
                        s.unread();
                    }
                    throw new SyntaxError("unterminated character reference");
                }
                return true;
            } else {
                s.unread();
            }
            if (!nt_Name(s)) {
                s.unread();
                return false;
            }
            if ((c = s.read()) != ';') {
                if (c != -1) {
                    s.unread();
                }
                throw new SyntaxError("unterminated entity reference");
            }
            return true;
        }

        // Eq ::= S? '=' S?
        public boolean nt_Eq(final State s) throws IOException {
            nt_S(s);
            if (s.read() != '=') {
                s.unread();
                return false;
            }
            nt_S(s);
            return true;
        }

        // Name ::= (Letter | '_' | ':') (NameChar)*
        public boolean nt_Name(final State s) throws IOException {
            int c = -1;
            try {
                if (isNameStartChar(c = s.read())) {
                    while (isNameChar(c = s.read()))
                        ;
                    return true;
                } else {
                    return false;
                }
            } finally {
                if (c != -1) {
                    s.unread();
                }
            }
        }

        // PI ::= '<?' PITarget (S (Char* - (Char* '?>' Char*)))? '?>'
        public boolean nt_PI(final State s, final Handler h) throws IOException {
            if (s.literal(XML_PI_START)) {
                int start = s.buffer.size();
                if (!nt_Name(s)) {
                    throw new SyntaxError("target expected for PI");
                }
                Text t = s.buffer.part(start, s.buffer.size() - start);
                start = s.buffer.size();
                Text v = Text.EMPTY;
                if (nt_S(s)) {
                    int c = -1, p;
                    do {
                        p = c;
                        checkValid(c = s.read());
                    } while (c != -1 && !(p == '?' && c == '>'));
                    if (c == -1) {
                        throw new SyntaxError("end of file inside PI");
                    } else {
                        s.unread();
                        s.unread();
                        v = s.buffer.part(start, s.buffer.size() - start);
                    }
                }
                if (!s.literal(XML_PI_END)) {
                    throw new SyntaxError("'?>' expected to end PI");
                } else {
                    s.buffer.clear();
                    h.handleInstruction(t, v);
                    return true;
                }
            } else {
                return false;
            }
        }

        protected boolean nt_Reference(final State s, final Parser p)
                throws IOException {
            int c = s.read();
            if (c != '&') {
                if (c != -1) {
                    s.unread();
                }
                return false;
            }
            Text result;
            c = s.read();
            if (c == '#') {
                int radix;
                int value = 0;
                c = s.read();
                if (c == 'x') {
                    radix = 16;
                    c = s.read();
                } else {
                    radix = 10;
                }
                int n;
                while ((n = Character.digit(c, radix)) != -1) {
                    value = value * radix + n;
                    c = s.read();
                }
                if (c != ';') {
                    if (c != -1) {
                        s.unread();
                    }
                    throw new SyntaxError("unterminated character reference");
                } else {
                    result = new TextBuffer().append(value).toText();
                }
            } else {
                s.unread();
                int start = s.buffer.size();
                if (!nt_Name(s)) {
                    throw new SyntaxError("invalid reference after '"
                            + s.buffer + "'");
                }
                if ((c = s.read()) != ';') {
                    throw new SyntaxError("unterminated entity reference");
                }
                Text entityName = s.buffer.part(start, s.buffer.size() - start
                        - 1);
                result = p.resolver.resolveInternal(entityName);
                if (result == null) {
                    throw new IOException(
                            "unresolved internal entity reference: '"
                                    + entityName + "'");
                }
            }
            p.handler.handleCharacterData(true, result);
            return true;
        }

        // S ::= (#x20 | #x9 | #xD | #xA)+
        public boolean nt_S(final State s) throws IOException {
            return s.white() > 0;
        }

        // 'yes' | 'no'
        public boolean nt_YesNo(final State s) throws IOException {
            if (s.literal(NO)) {
                return false;
            } else if (s.literal(YES)) {
                return true;
            } else {
                throw new SyntaxError("'yes' or 'no' expected after '"
                        + s.buffer + "'");
            }
        }
    }

    private static class Syntax11 extends Syntax10 {

        @Override
        public boolean isNameChar(final int c) {
            return isNameStartChar(c) || c == '-' || c == '.'
                    || ('0' <= c && c <= '9') || c == 0xB7
                    || (0x0300 <= c && c <= 0x036F)
                    || (0x203F <= c && c <= 0x2040);
        }

        @Override
        public boolean isNameStartChar(final int c) {
            return c == ':' || ('A' <= c && c <= 'Z') || c == '_'
                    || ('a' <= c && c <= 'z') || (0xC0 <= c && c <= 0xD6)
                    || (0xD8 <= c && c <= 0xF6) || (0xF8 <= c && c <= 0x2FF)
                    || (0x370 <= c && c <= 0x37D)
                    || (0x37F <= c && c <= 0x1FFF)
                    || (0x200C <= c && c <= 0x200D)
                    || (0x2070 <= c && c <= 0x218F)
                    || (0x2C00 <= c && c <= 0x2FEF)
                    || (0x3001 <= c && c <= 0xD7FF)
                    || (0xF900 <= c && c <= 0xFDCF)
                    || (0xFDF0 <= c && c <= 0xFFFD)
                    || (0x10000 <= c && c <= 0xEFFFF);
        }

        public boolean isRestrictedChar(final int c) throws IOException {
            return (0x1 <= c && c <= 0x8) || (0xB <= c && c <= 0xC)
                    || (0xE <= c && c <= 0x1F) || (0x7F <= c && c <= 0x84)
                    || (0x86 <= c && c <= 0x9F);
        }

        @Override
        public boolean isValidChar(final int c) throws IOException {
            return ((0x1 <= c && c <= 0xD7FF) || (0xE000 <= c && c <= 0xFFFD) || (0x10000 <= c && c <= 0x10FFFF))
                    && !isRestrictedChar(c);
        }
    }

    public static class SyntaxError extends IOException {
        private static final long serialVersionUID = -1153201134111925366L;

        public SyntaxError(String message) {
            super(message);
        }
    }

    public static final Text COLON = Text.constant((byte) ':');

    public static final Text NO = Text.constant((byte) 'n', (byte) 'o');

    public static final Text XML_CDATA_START = Text.valueOf("<![CDATA[");

    public static final Text XML_COMMENT_END = Text.constant(new byte[] { '-',
            '-', '>' });

    public static final Text XML_COMMENT_START = Text.constant(new byte[] {
            '<', '!', '-', '-' });

    public static final Text XML_DECL_10 = Text.constant(new byte[] { '1', '.',
            '0' });

    public static final Text XML_DECL_11 = Text.constant(new byte[] { '1', '.',
            '1' });

    public static final Text XML_DECL_ENCODING = Text.valueOf("encoding");

    public static final Text XML_DECL_END = Text.constant((byte) '?',
            (byte) '>');

    public static final Text XML_DECL_STANDALONE = Text.valueOf("standalone");

    public static final Text XML_DECL_START = Text.valueOf("<?xml");

    public static final Text XML_DECL_VERSION = Text.valueOf("version");

    public static final Text XML_ENCODING_UTF_8 = Text.valueOf("UTF-8");

    public static final Text XML_ENCODING_UTF8 = Text.constant(new byte[] {
            'U', 'T', 'F', '8' });

    public static final Text XML_PI_END = Text.constant((byte) '?', (byte) '>');

    public static final Text XML_PI_START = Text.constant((byte) '<',
            (byte) '?');

    public static final Text XMLNS = Text.valueOf("xmlns");

    public static final Text XMLNS_ = new Text(XMLNS, COLON);

    public static final Text YES = Text.constant(new byte[] { 'y', 'e', 's' });

    protected final Handler handler;

    protected final Resolver resolver;

    public Parser(Resolver resolver, Handler handler) {
        this.resolver = resolver;
        this.handler = handler;
    }

    protected void checkMagic(final State s) throws IOException {
        int bom = s.magic();
        if ((bom & 0xFFFFFF00) == 0xEFBBBF00) {
            s.in.skip(3); // skip UTF-8 byte order mark
        } else if (bom == 0x3C3F786D) {
            // "<?xm" at start, use declaration
        } else if (bom == 0x4C6FA794) {
            // EBCDIC, reject.
            throw new IOException("EBCDIC is unsupported");
        } else if (bom == 0x0000FEFF || bom == 0x0000003C) {
            // UCS-4 big endian, reject.
            throw new IOException("UCS-4BE is unsupported");
        } else if (bom == 0xFFFE0000 || bom == 0x3C000000) {
            // UCS-4 little endian, reject.
            throw new IOException("UCS-4LE is unsupported");
        } else if (bom == 0x0000FFFE || bom == 0x00003C00) {
            // UCS-4 odd endian.
            throw new IOException("UCS-4-2143 is unsupported");
        } else if (bom == 0xFEFF0000 || bom == 0x003C0000) {
            // UCS-4 odd endian.
            throw new IOException("UCS-4-3412 is unsupported");
        } else if ((bom & 0xFFFF0000) == 0xFEFF0000 || bom == 0x003C003F) {
            // UTF-16 big endian, reject.
            throw new IOException("UTF-16BE is unsupported");
        } else if ((bom & 0xFFFF0000) == 0xFFFE0000 || bom == 0x3C003F00) {
            // UTF-16 little endian, reject.
            throw new IOException("UTF-16LE is unsupported");
        }
    }

    protected int findUri(final State s, final int prefix,
            final Stack<NamePair> c) throws SyntaxError {
        int scan = c.size();
        while (--scan >= 0) {
            if (prefix == c.get(scan).getA()) {
                return c.get(scan).getB();
            }
        }
        if (prefix == s.emptyName) {
            return s.emptyName;
        } else {
            throw new SyntaxError("undeclared prefix: '"
                    + s.lNamePool.extern(prefix) + "'");
        }
    }

    protected Syntax10 nt_Declaration(final State s) throws IOException {
        // XMLDecl ::= '<?xml' VersionInfo EncodingDecl? SDDecl? S?'?>'
        if (!s.literal(XML_DECL_START)) {
            return new Syntax10();
        }
        // VersionInfo ::= S 'version' Eq ("'" VersionNum "'" | '"' VersionNum
        // '"')
        if (s.white() < 1) {
            throw new SyntaxError("white space expected after '" + s.buffer
                    + "'");
        }
        if (!s.literal(XML_DECL_VERSION)) {
            throw new SyntaxError("'version' expected after '" + s.buffer + "'");
        }
        // Eq ::= S? '=' S?
        s.white();
        if (s.read() != '=') {
            s.unread();
            throw new SyntaxError("'=' expected after '" + s.buffer + "'");
        }
        s.white();
        int q = s.quote();
        Syntax10 syntax;
        if (s.literal(XML_DECL_11)) {
            // VersionNum ::= '1.1'
            syntax = new Syntax11();
        } else if (s.literal(XML_DECL_10)) {
            // VersionNum ::= '1.0'
            syntax = new Syntax10();
        } else {
            throw new SyntaxError("'1.0' or '1.1' expected after '" + s.buffer
                    + "'");
        }
        s.quote(q);
        boolean space = s.white() > 0;
        if (space && s.literal(XML_DECL_ENCODING)) {
            // EncodingDecl ::= S 'encoding' Eq ('"' EncName '"' | "'" EncName
            // "'" )
            if (!syntax.nt_Eq(s)) {
                throw new SyntaxError("'=' expected after '" + s.buffer + "'");
            }
            q = s.quote();
            Text name = syntax.nt_EncName(s);
            s.quote(q);
            if (!name.equals(XML_ENCODING_UTF8)
                    && !name.equals(XML_ENCODING_UTF_8)) {
                throw new IOException("unsupported encoding: '" + name + "'");
            }
            space = s.white() > 0;
        }
        if (space && s.literal(XML_DECL_STANDALONE)) {
            // SDDecl ::= S 'standalone' Eq (("'" ('yes' | 'no') "'") | ('"'
            // ('yes' | 'no') '"'))
            // Note that XML 1.1 requires #x20+ instead of the initial S. We
            // don't check this here so we are a bit more lenient; there is no
            // explanation why it would require space only in the specification.
            if (!syntax.nt_Eq(s)) {
                throw new SyntaxError("'=' expected after '" + s.buffer + "'");
            }
            q = s.quote();
            s.standalone = syntax.nt_YesNo(s);
            s.quote(q);
            space = s.white() > 0;
        }
        if (!s.literal(XML_DECL_END)) {
            throw new SyntaxError("'?>' expected after '" + s.buffer + "'");
        }
        s.buffer.clear();
        return syntax;
    }

    protected void nt_Epilog(State s, Syntax10 syntax) throws IOException {
        nt_Misc(s, syntax);
        handler.handleCloseDocument();
    }

    protected void nt_Misc(State s, Syntax10 syntax) throws IOException {
        while (syntax.nt_S(s) || syntax.nt_Comment(s, handler)
                || syntax.nt_PI(s, handler))
            ;
    }

    // prolog ::= XMLDecl? Misc* (doctypedecl Misc*)?
    protected Syntax10 nt_Prolog(State s) throws IOException {
        Syntax10 syntax = nt_Declaration(s);
        handler.handleOpenDocument(s.lNamePool, s.qNamePool);
        nt_Misc(s, syntax);
        // Note: doctype declaration not supported.
        return syntax;
    }

    public void parse(InputStream in) throws IOException {
        State s = new State(in, this);
        Stack<NamePair> c = new Stack<NamePair>();
        checkMagic(s);
        Syntax10 syntax = nt_Prolog(s);
        syntax.nt_Element(s, c, this);
        nt_Epilog(s, syntax);
    }

    protected int qualify(final State s, Text name, final Stack<NamePair> c,
            final boolean asAttribute) throws SyntaxError {
        int colon = name.indexOf(COLON);
        int uri;
        if (colon == -1) {
            if (asAttribute) {
                uri = s.emptyName;
            } else {
                uri = findUri(s, s.emptyName, c);
            }
        } else {
            uri = findUri(s, s.lNamePool.intern(name.part(0, colon)), c);
            name = name.part(colon + 1, name.size() - colon - 1);
        }
        return s.qNamePool.intern(new NamePair(uri, s.lNamePool.intern(name)));
    }

    public static final int nameFor(final int uri, Text name,
            final NamePool<NamePair> qNames, final NamePool<Text> lNames) {
        return qNames.intern(new NamePair(uri, lNames.intern(name)));
    }

}
