/*
 * @(#)$Id: ScriptScenario.java,v 1.1 2004-06-25 21:13:04 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
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