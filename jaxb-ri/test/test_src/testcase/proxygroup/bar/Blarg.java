/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package testcase.proxygroup.bar;

public interface Blarg {
    
    public void throwACheckedException() throws Exception;
    
    public void throwAnUncheckedException();
    
    public void throwAnError();
    
}