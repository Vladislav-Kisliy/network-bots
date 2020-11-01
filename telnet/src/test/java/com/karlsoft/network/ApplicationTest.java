package com.karlsoft.network;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Ignore;

import java.io.IOException;

/**
 * Unit test for simple Application.
 */
@Ignore
public class ApplicationTest extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public ApplicationTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(ApplicationTest.class);
    }

    /**
     * Rigourous Test :-)
     */
//    public void testApp() throws Exception {
//        System.out.printf("#%.7spuzzler number %07d. It's %b, isn't it?%n", "You're real", 1, "false");
//
//        try {
//            throw new IOException("OOps!");
//        } catch (IOException ex) {
//            System.out.println("Error: " + ex.getMessage());
//            throw new IOException("OOps again!");
//        } finally {
//            throw new IOException("Mega OOps!");
//        }
////        assertTrue(true);
//    }

    private void increment(int i) {
        i++;
    }
}
