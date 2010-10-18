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

import org.dom4j.Element;

import com.sun.xml.bind.JAXBAssertionError;

import util.InterpreterBuilder;
import batch.core.om.Instance;
import bsh.EvalError;
import bsh.Interpreter;

/**
 * 
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class ScriptScenario implements Scenario {
    
    private final String name;
    
    /**
     * Script execution engine.
     */
    private Interpreter interpreter;
    
    private final String prepareScript;
    private final String runScript;
    private final String teardownScript;

    /**
     * Parses ECMAScript-based scenario from XML description
     * of the form:
     * 
     * <pre><xmp>
     * <scenario>
     *   <prepare> ... </prepare>
     *   <run> ... </run>
     *   <teardown> ... </teardown>
     * </scenario>
     */
    public ScriptScenario( Element performanceElement ) {
        prepareScript =  performanceElement.elementText("prepare");
        runScript =      performanceElement.elementText("run");
        teardownScript = performanceElement.elementText("teardown");
        
        name = performanceElement.attributeValue("name","script-based scenario");
    }
    
    public final String getName() {
        return name;
    }
    
    protected final Interpreter getInterpreter() {
        return interpreter;
    }

    public void prepare( PerformanceTestDescriptor descriptor,
        ClassLoader classLoader, Instance instance ) throws ScenarioException {
        
        interpreter = createInterpreter(descriptor, classLoader, instance);
        
        execute(prepareScript);
    }
    
    /**
     * Sets up a script interpreter.
     * 
     * <p>
     * Derived classes may override this method to do additional set up.
     */
    protected Interpreter createInterpreter(PerformanceTestDescriptor descriptor, ClassLoader classLoader, Instance instance) throws JAXBAssertionError {
        Interpreter i;
        
        if( instance==null ) {
            // create the bare minimum interpreter.
            // this allows the test to measure the time it takes to
            // create a new JAXBContext and etc.
            i = InterpreterBuilder.create(
                classLoader, descriptor.schema.targetPackageName, false );
        } else {
            // when we are run against an instance,
            // we can assume that the script will not measure
            // the time it takes to create JAXBContext and etc.
            // so give them more richer context
            i = InterpreterBuilder.create( instance,
                classLoader, descriptor.schema.targetPackageName );
        }
        
        try {
            // set descriptor for script.
            i.set("descriptor",descriptor);
        } catch (EvalError e) {
            // impossible.
            throw new JAXBAssertionError(e);
        }
        
        return i;
    }

    public void run() throws ScenarioException {
        execute(runScript);
    }

    public void teardown() throws ScenarioException {
        execute(teardownScript);
        interpreter = null; // release the interpreter, which is a heavy component.
    }
    
    private void execute( String script ) throws ScenarioException {
        try {
            interpreter.eval(script);
        } catch (EvalError e) {
            throw new ScenarioException(e);
        }
    }
}
