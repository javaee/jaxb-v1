/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package testcase.proxygroup.foo;

public class BlargImpl implements Blarg {
    
    public void throwACheckedException() throws Exception {
        throw new NumberFormatException();
    }
    
    public void throwAnUncheckedException() {
        throw new NullPointerException();
    }
    
    public void throwAnError() {
        throw new InternalError();
    }
    
}