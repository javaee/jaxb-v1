/*
 * @(#)$Id: JAXBContextInstanciationScenario.java,v 1.1 2004-06-25 21:13:04 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package batch.performance.scenarios;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import batch.performance.ScenarioException;

/**
 * Invokes {@link javax.xml.bind.JAXBContext#newInstance()}. 
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class JAXBContextInstanciationScenario extends AbstractScenarioImpl {

    private JAXBContext context;
    
    public String getName() {
        return "JAXBContextInstanciation";
    }

    public void run() throws ScenarioException {
         try {
            context = JAXBContext.newInstance(descriptor.schema.targetPackageName,classLoader);
        } catch (JAXBException e) {
            throw new ScenarioException(e);
        }
    }

    public void teardown() throws ScenarioException {
        super.teardown();
        
        // reading the variable prevents Eclipse from flagging a warning 
        if(context!=null)   context = null;
    }
}
