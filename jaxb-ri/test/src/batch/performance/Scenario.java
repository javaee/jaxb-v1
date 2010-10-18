/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package batch.performance;

import batch.core.om.Instance;

/**
 * Scenario to be measured. 
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface Scenario {
    
    /**
     * Human readable name of the scenario.
     */
    public String getName();
    
    /**
     * Called first to prepare the scenario.
     * 
     * The code that runs in this method is not considered
     * as a subject of the measurement.
     * 
     * @param classLoader
     *      This {@link ClasLoader} can load the generated classes.
     *      Use the reflection to create the ObjectFactory class
     *      in the root package.
     * @param instance
     *      If the scenario is associated with an instance, that instance.
     *      Otherwise null to indicate that the scenario is run once
     *      against the whole {@link PerformanceTestDescriptor}.
     */
    void prepare( PerformanceTestDescriptor descriptor, ClassLoader classLoader, Instance instance ) throws ScenarioException;    
    
    // TODO: instead of passing a ClassLoader, we might want to pass in
    // something more, so that the method can easily access JAXBContext,
    // for instance. 
    
    /**
     * Called to execute the scenario after the {@link #prepare()} method.
     * 
     * This method will become the subject of measurement.
     * Those scenarios whose memory footprint is measured should
     * use class fields to store references so that the objects
     * will stay in memory even after this method returns.
     */
    void run() throws ScenarioException;
    
    /**
     * Called after the {@link #run()} method to release unnecessary resources.
     * 
     * The code that runs in this method is not considered
     * as a subject of the measurement.
     */
    void teardown() throws ScenarioException;
}
