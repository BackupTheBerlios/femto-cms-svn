package de.mobizcorp.水星;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for de.mobizcorp.水星");
        //$JUnit-BEGIN$
        suite.addTestSuite(StoreTest.class);
        //$JUnit-END$
        return suite;
    }

}
