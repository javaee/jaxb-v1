/*
 * @(#)$Id: JavacException.java,v 1.1 2004-06-25 21:12:59 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package batch.core;

/**
 * Signals a failure in Javac invocation.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class JavacException extends Exception {
    public JavacException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public JavacException(Throwable cause) {
        super(cause);
    }

    public JavacException(String message) {
        super(message);
    }
}
