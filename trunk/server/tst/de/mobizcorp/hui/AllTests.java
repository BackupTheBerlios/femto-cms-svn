package de.mobizcorp.hui;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for de.mobizcorp.hui");
        //$JUnit-BEGIN$
        suite.addTestSuite(RFC2279Test.class);
        //$JUnit-END$
        return suite;
    }

}
