/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package batch.core;

/**
 * Signals a failure of XJC execution.
 * 
 * This exception will be thrown if XJC exists with non-zero.
 * This should mean that XJC has detected errors in the schema.
 * 
 * @author
 * 	Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class XJCException extends Exception {
    public XJCException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public XJCException(Throwable cause) {
        super(cause);
    }

    public XJCException(String message) {
        super(message);
    }
}
