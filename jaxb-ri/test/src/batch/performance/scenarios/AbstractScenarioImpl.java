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

import batch.core.om.Instance;
import batch.performance.PerformanceTestDescriptor;
import batch.performance.Scenario;
import batch.performance.ScenarioException;

/**
 * A helper {@link Scenario} implementation that keeps
 * all the parameters to the fields.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
abstract class AbstractScenarioImpl implements Scenario {
    protected PerformanceTestDescriptor descriptor;
    protected ClassLoader classLoader;
    protected Instance instance;
    
    public void prepare(PerformanceTestDescriptor descriptor, ClassLoader classLoader, Instance instance) throws ScenarioException {
        this.descriptor = descriptor;
        this.classLoader = classLoader;
        this.instance = instance;
    }

    public void teardown() throws ScenarioException {
        descriptor = null;
        classLoader = null;
        instance = null;
    }
}
