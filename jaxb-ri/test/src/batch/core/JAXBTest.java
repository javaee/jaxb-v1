/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package batch.core;

import com.sun.tools.xjc.reader.xmlschema.parser.VersionNumber;

import junit.framework.Test;

/**
 * 
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface JAXBTest extends Test {
    /**
     * Returns true if this test should be run
     * against the specified version of the JAXB RI.
     */
    boolean isApplicable(VersionNumber v);
}
