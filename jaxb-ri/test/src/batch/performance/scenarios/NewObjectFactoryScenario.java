/*
 * @(#)$Id: NewObjectFactoryScenario.java,v 1.1 2004-06-25 21:13:05 kohsuke Exp $
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
 * Creates a new instance of the generated ObjectFactory.
 * <p>
 * We expect this to be roughly the same as
 * {@link javax.xml.bind.JAXBContext#newInstance()}.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class NewObjectFactoryScenario extends AbstractScenarioImpl {
    
    private Object context;
    
    public String getName() {
        return "NewObjectFactory";
    }

    public void run() throws ScenarioException {
        try {
            context = classLoader.loadClass("ObjectFactory").newInstance();
        } catch (Exception e) {
            throw new ScenarioException(e);
        }
    }
    
    public void teardown() throws ScenarioException {
        super.teardown();
        
        // reading the variable prevents Eclipse from flagging a warning 
        if(context!=null)   context = null;
    }
}
