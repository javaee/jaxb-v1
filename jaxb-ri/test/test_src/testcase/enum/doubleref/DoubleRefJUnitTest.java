/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package testcase.enum.doubleref;

import junit.textui.TestRunner;

import util.JUnitTestBase;

/**
 * 
 * @author
 * 	Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class DoubleRefJUnitTest extends JUnitTestBase {

    public DoubleRefJUnitTest(String name) { super(name); }
    
    // just test if the schema compiles
    public void testParse() {
        Size s = Size.SMALL;
    }

    public static void main(String[] args) {
        TestRunner.run(DoubleRefJUnitTest.class);
    }
}
