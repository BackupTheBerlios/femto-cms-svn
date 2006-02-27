package de.mobizcorp;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for de.mobizcorp");
        //$JUnit-BEGIN$
        suite.addTest(de.mobizcorp.hui.AllTests.suite());
        suite.addTest(de.mobizcorp.qu8ax.AllTests.suite());
        suite.addTest(de.mobizcorp.水星.AllTests.suite());
        //$JUnit-END$
        return suite;
    }

}
