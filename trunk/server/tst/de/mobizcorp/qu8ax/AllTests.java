package de.mobizcorp.qu8ax;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for de.mobizcorp.qu8ax");
        //$JUnit-BEGIN$
        suite.addTestSuite(OpenPoolTest.class);
        suite.addTestSuite(ParserTest.class);
        suite.addTestSuite(FixedPoolTest.class);
        suite.addTestSuite(WriterTest.class);
        suite.addTestSuite(FilterTest.class);
        //$JUnit-END$
        return suite;
    }

}
