/*
 * @(#)$Id: NonValiatingUnmarshallScenario.java,v 1.1 2004-06-25 21:13:05 kohsuke Exp $
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
import javax.xml.bind.Unmarshaller;

import batch.core.om.Instance;
import batch.performance.PerformanceTestDescriptor;
import batch.performance.ScenarioException;

/**
 * 
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class NonValiatingUnmarshallScenario extends AbstractScenarioImpl {

    private JAXBContext context;
    private Unmarshaller unmarshaller;

    public void prepare(PerformanceTestDescriptor descriptor, ClassLoader classLoader, Instance instance)
        throws ScenarioException {
        super.prepare(descriptor, classLoader, instance);
        
        try {
           context = JAXBContext.newInstance(descriptor.schema.targetPackageName,classLoader);
           unmarshaller = context.createUnmarshaller();
       } catch (JAXBException e) {
           throw new ScenarioException(e);
       }
    }

    public String getName() {
        return "NonValiatingUnmarshall";
    }

    public void run() throws ScenarioException {
        try {
            for( int i=0; i<100; i++ ) {
                unmarshaller.unmarshal(instance.document);
            }
        } catch( JAXBException e ) {
            throw new ScenarioException(e);
        }
    }

    public void teardown() throws ScenarioException {
        super.teardown();
        context = null;
        unmarshaller = null;
    }
}
