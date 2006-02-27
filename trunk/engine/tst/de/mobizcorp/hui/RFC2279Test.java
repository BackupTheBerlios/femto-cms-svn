package de.mobizcorp.hui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;

import junit.framework.TestCase;

public class RFC2279Test extends TestCase {

    private static final int SAMPLE_SIZE = 800;

    /*
     * Test method for 'de.mobizcorp.hui.RFC2279.write(int, OutputStream)'
     */
    public void testWrite() throws IOException {
        final int seed = "testWrite()".hashCode();
        final Random random = new Random(seed);
        final StringBuffer buffer = new StringBuffer();
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final int[] samples = new int[SAMPLE_SIZE];
        final int[] indexes = new int[SAMPLE_SIZE];
        for (int i = 0; i < SAMPLE_SIZE; i++) {
            int c;
            do {
                c = random.nextInt(Character.MAX_CODE_POINT);
            } while (!Character.isValidCodePoint(c) || !Character.isDefined(c)
                    || Character.isHighSurrogate((char) c)
                    || Character.isLowSurrogate((char) c));
            indexes[i] = bos.size();
            buffer.appendCodePoint(c);
            RFC2279.write(c, bos);
            samples[i] = c;
        }
        final byte[] result = bos.toByteArray();
        final byte[] expect = buffer.toString().getBytes("UTF-8");
        assertEquals("result length", expect.length, result.length);
        for (int i = 0; i < result.length; i++) {
            assertEquals("byte " + i, expect[i], result[i]);
        }
    }

    /*
     * Test method for 'de.mobizcorp.hui.RFC2279.read(InputStream)'
     */
    public void testRead() throws IOException {
        final int seed = "testRead()".hashCode();
        final Random random = new Random(seed);
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        for (int i = 0; i < SAMPLE_SIZE; i++) {
            final int c = random.nextInt(Integer.MAX_VALUE);
            RFC2279.write(c, bos);
        }
        random.setSeed(seed);
        final ByteArrayInputStream in = new ByteArrayInputStream(bos
                .toByteArray());
        for (int i = 0; i < SAMPLE_SIZE; i++) {
            final int result = RFC2279.read(in);
            final int expect = random.nextInt(Integer.MAX_VALUE);
            assertEquals("value " + i, expect, result);
        }
        assertEquals("stream spent", -1, in.read());
    }

}
