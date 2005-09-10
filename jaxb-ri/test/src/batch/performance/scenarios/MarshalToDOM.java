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
import javax.xml.bind.Marshaller;
import javax.xml.transform.dom.DOMResult;

import batch.core.om.Instance;
import batch.performance.PerformanceTestDescriptor;
import batch.performance.ScenarioException;

public class MarshalToDOM extends AbstractScenarioImpl {

    private JAXBContext context;
    private Marshaller marshaller;
    private Object u;

    public String getName() {
        return "MarshalToDOM";
    }

    public void run() throws ScenarioException {
        try {
            for (int i=0; i<100; i++) {
                marshaller.marshal(u, new DOMResult());
            }
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    public void prepare(
        PerformanceTestDescriptor descriptor,
        ClassLoader classLoader,
        Instance instance)
        throws ScenarioException {
        super.prepare(descriptor, classLoader, instance);
        try {
            context =
                JAXBContext.newInstance(
                                        descriptor.schema.targetPackageName,
                                        classLoader);
            marshaller = context.createMarshaller();
            u = instance.unmarshal(context);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    public void teardown() throws ScenarioException {
        super.teardown();
        context = null;
        marshaller = null;
    }
}
