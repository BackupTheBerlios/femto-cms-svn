package de.mobizcorp.水星;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.DataFormatException;

import junit.framework.TestCase;
import de.mobizcorp.lib.ErrorStreamLogger;
import de.mobizcorp.lib.InputStreamEater;
import de.mobizcorp.lib.Text;
import de.mobizcorp.lib.TextBuffer;
import de.mobizcorp.水星.Store.TagEntry;

public class StoreTest extends TestCase {

    private static final String CWD = "../hg";

    /*
     * Test method for 'de.mobizcorp.水星.Store.file(Text)'
     */
    public void testFile() throws IOException, DataFormatException {
        final Store s = new Store(CWD);
        final File cwd = new File(CWD);
        assertTrue(cwd.isDirectory());
        final File tmp = File.createTempFile("StoreTest.", ".tmp");
        final Manifest m = s.manifest();
        for (Text name : m.read(m.tip()).keySet()) {
            Element element = s.file(Text.valueOf(name));
            Text t1 = Text.constant(element.read(element.tip())), t2;
            // we must cat to file or the line endings will not match
            exec(cwd, "hg", "-y", "cat", "-o", tmp.toString(), name.toString())
                    .close();
            InputStream in = new FileInputStream(tmp);
            try {
                t2 = new TextBuffer().append(in).toText();
            } finally {
                in.close();
            }
            assertEquals("contents", t1, t2);
            tmp.delete();
        }
    }

    /*
     * Test method for 'de.mobizcorp.水星.Store.lookup(Text)'
     */
    public void testLookup() {

    }

    /*
     * Test method for 'de.mobizcorp.水星.Store.manifest()'
     */
    public void testManifest() {

    }

    /*
     * Test method for 'de.mobizcorp.水星.Store.tags()'
     */
    public void testTags() {
    }

    /*
     * Test method for 'de.mobizcorp.水星.Store.tags(Version)'
     */
    public void testTagsVersion() {

    }

    /*
     * Test method for 'de.mobizcorp.水星.Store.taglist()'
     */
    public void testTaglist() throws IOException, DataFormatException {
        final Store s = new Store(CWD);
        System.out.println("tags:");
        for (TagEntry t : s.taglist()) {
            System.out.printf("%-30s %5d:%s\n", t.t, t.g, t.v);
        }
        System.out.flush();
    }

    /*
     * Test method for 'de.mobizcorp.水星.Store.transaction()'
     */
    public void testTransaction() {

    }

    private InputStream exec(File base, String... args) throws IOException {
        ProcessBuilder builder = new ProcessBuilder(args).directory(base);
        Process process = builder.start();
        ErrorStreamLogger.attach(process);
        return new InputStreamEater(process);
    }

}
