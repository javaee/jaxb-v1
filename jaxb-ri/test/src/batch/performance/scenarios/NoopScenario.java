/*
 * @(#)$Id: NoopScenario.java,v 1.1 2004-06-25 21:13:06 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package batch.performance.scenarios;

import batch.performance.ScenarioException;

/**
 * 
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class NoopScenario extends AbstractScenarioImpl {

    public String getName() {
        return "Noop";
    }

    public void run() throws ScenarioException {
        // do nothing
    }

}
