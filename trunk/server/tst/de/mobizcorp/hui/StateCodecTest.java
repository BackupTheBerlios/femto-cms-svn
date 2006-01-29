package de.mobizcorp.hui;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;

import junit.framework.TestCase;

public class StateCodecTest extends TestCase {

    private static final int SAMPLE_SIZE = 800;

    /*
     * Test method for 'de.mobizcorp.hui.StateCodec.write(byte[])'
     */
    public void testWriteByteArray() throws IOException {
        final int seed = "testWriteByteArray()".hashCode();
        final Random random = new Random(seed);
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final StateCodec out = new StateCodec(bos);
        final byte[] samples = new byte[SAMPLE_SIZE];
        random.nextBytes(samples);
        out.write(samples);
        out.close();
        final byte[] encoded = bos.toByteArray();
        final byte[] results = StateCodec.fromBase64(encoded);
        assertEquals("result length", samples.length, results.length);
        for (int i = 0; i < SAMPLE_SIZE; i++) {
            final byte result = results[i];
            final byte expect = samples[i];
            assertEquals("value " + i, expect, result);
        }
    }

    /*
     * Test method for 'de.mobizcorp.hui.StateCodec.write(int)'
     */
    public void testWrite() throws IOException {
        final int seed = "testWrite()".hashCode();
        final Random random = new Random(seed);
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final StateCodec out = new StateCodec(bos);
        final byte[] samples = new byte[SAMPLE_SIZE];
        random.nextBytes(samples);
        for (int i = 0; i < SAMPLE_SIZE; i++) {
            out.write(samples[i] & 0xff);
        }
        out.close();
        final byte[] encoded = bos.toByteArray();
        final byte[] results = StateCodec.fromBase64(encoded);
        assertEquals("result length", samples.length, results.length);
        for (int i = 0; i < SAMPLE_SIZE; i++) {
            final byte result = results[i];
            final byte expect = samples[i];
            assertEquals("value " + i, expect, result);
        }
    }
    
    private static final byte[] ALPHABET;
    
    static {
        ALPHABET = new byte[64];
        int scan = 0;
        for (int i = 0; i < 26; i++) {
            ALPHABET[scan++] = (byte) ('A' + i);
        }
        for (int i = 0; i < 26; i++) {
            ALPHABET[scan++] = (byte) ('a' + i);
        }
        for (int i = 0; i < 10; i++) {
            ALPHABET[scan++] = (byte) ('0' + i);
        }
        ALPHABET[scan++] = '-';
        ALPHABET[scan++] = '_';
    }

    /*
     * Test method for 'de.mobizcorp.hui.StateCodec.fromBase64(byte)'
     */
    public void testFromBase64() {
        for (int i = 0; i < ALPHABET.length; i++) {
            final int n = StateCodec.fromBase64(ALPHABET[i]);
            assertEquals("alphabet " + i, i, n);
        }
    }

    /*
     * Test method for 'de.mobizcorp.hui.StateCodec.toBase64(byte)'
     */
    public void testToBase64() {
        for (int i = 0; i < ALPHABET.length; i++) {
            final byte b = StateCodec.toBase64(i);
            assertEquals("alphabet " + i, ALPHABET[i], b);
        }
    }

}
