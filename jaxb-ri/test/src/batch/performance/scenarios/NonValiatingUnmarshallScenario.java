/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
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
