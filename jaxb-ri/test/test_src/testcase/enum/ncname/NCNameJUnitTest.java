/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package testcase.enum.ncname;

import junit.textui.TestRunner;

import util.JUnitTestBase;

/**
 * 
 * @author
 * 	Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class NCNameJUnitTest extends JUnitTestBase {

    public NCNameJUnitTest(String name) { super(name); }
    
    public void testParse() {
        assertSame( Size.fromString("large"), Size.LARGE );
        assertSame( Size.fromString("medium"), Size.MEDIUM );
        assertSame( Size.fromString("small"), Size.SMALL );
        
        assertTrue( Size.fromString("large")!=Size.SMALL );
    }
    
    public void testPrint() {
        assertEquals( Size.LARGE.toString(), "large" );
        assertEquals( Size.MEDIUM.toString(), "medium" );
        assertEquals( Size.SMALL.toString(), "small" );
    }

    public static void main(String[] args) {
        TestRunner.run(NCNameJUnitTest.class);
    }
}
