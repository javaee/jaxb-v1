/*
 * @(#)$Id: NullJAXBTest.java,v 1.1 2004-06-25 21:13:11 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package util;

import junit.framework.TestResult;

import com.sun.tools.xjc.reader.xmlschema.parser.VersionNumber;

import batch.core.JAXBTest;

/**
 * {@link JAXBTest} that runs nothing.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class NullJAXBTest implements JAXBTest {
    private NullJAXBTest() {}
    
    /** Singleton. */
    public static final JAXBTest theInstance = new NullJAXBTest();

    public boolean isApplicable(VersionNumber v) {
        return true;
    }

    public int countTestCases() {
        return 0;
    }

    public void run(TestResult result) {
    }
}
