/*
 * @(#)$Id: ScenarioException.java,v 1.1 2004-06-25 21:13:03 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package batch.performance;

/**
 * Signals a failure in the scenario execution.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class ScenarioException extends Exception {
    public ScenarioException() {
        super();
    }

    public ScenarioException(String message) {
        super(message);
    }

    public ScenarioException(String message, Throwable cause) {
        super(message, cause);
    }

    public ScenarioException(Throwable cause) {
        super(cause);
    }

}
