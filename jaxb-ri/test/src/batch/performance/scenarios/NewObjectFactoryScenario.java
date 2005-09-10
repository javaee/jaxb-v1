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
