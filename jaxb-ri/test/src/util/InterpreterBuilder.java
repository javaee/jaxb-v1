/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package util;

import java.io.PrintStream;
import java.io.Reader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Validator;

import batch.core.om.Instance;
import batch.qa.ScriptHelper;
import bsh.EvalError;
import bsh.Interpreter;
import bsh.NameSpace;

/**
 * Builds default {@link Interpreter} object from
 * a standard set of parameters.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public final class InterpreterBuilder {
    
    /**
     * {@link Interpreter} that redirects stderr to stdout.
     * 
     * We don't wat scripts from cluttering stderr, which is
     * reserved for the test harness. 
     */
    private static class InterpreterEx extends Interpreter {
        
        // there's a bug in BeanShell (ClassManagerImpl.plainClassForName)
        // that makes BeanShell refer to ContextClassLoader as opposed to
        // the class loader specified in the setClassLoader method.
        // to work around the problem, set the class loader when
        // evaluating scripts.
        // 
        // see http://sourceforge.net/tracker/index.php?func=detail&aid=864572&group_id=4075&atid=104075
        // for the bug report
        private ClassLoader externalClassLoader;

        public void setClassLoader(ClassLoader classLoader){
            super.setClassLoader(classLoader);
            this.externalClassLoader =classLoader;
        }
        
        private ClassLoader replaceContextClassLoader() {
            Thread t = Thread.currentThread();
            ClassLoader oldContextClassLoader = t.getContextClassLoader();
            if(externalClassLoader!=null)
                t.setContextClassLoader(externalClassLoader);
            return oldContextClassLoader;
        }
        
        public Object eval(Reader in, NameSpace nameSpace, String sourceFileInfo) throws EvalError {
            ClassLoader old = replaceContextClassLoader();
            PrintStream stderr = System.err;
            try {
                System.setErr(System.out);
                return super.eval(in, nameSpace, sourceFileInfo);
            } finally {
                System.setErr(stderr);
                Thread.currentThread().setContextClassLoader(old);
            }
        }

        public Object eval(Reader in) throws EvalError {
            ClassLoader old = replaceContextClassLoader();
            PrintStream stderr = System.err;
            try {
                System.setErr(System.out);
                return super.eval(in);
            } finally {
                System.setErr(stderr);
                Thread.currentThread().setContextClassLoader(old);
            }
        }

        public Object eval(String statements, NameSpace nameSpace) throws EvalError {
            ClassLoader old = replaceContextClassLoader();
            PrintStream stderr = System.err;
            try {
                System.setErr(System.out);
                return super.eval(statements, nameSpace);
            } finally {
                System.setErr(stderr);
                Thread.currentThread().setContextClassLoader(old);
            }
        }

        public Object eval(String statements) throws EvalError {
            ClassLoader old = replaceContextClassLoader();
            PrintStream stderr = System.err;
            try {
                System.setErr(System.out);
                return super.eval(statements);
            } finally {
                System.setErr(stderr);
                Thread.currentThread().setContextClassLoader(old);
            }
        }

}
    
    /**
     * Creates a new {@link Interpreter} by setting the
     * class loader and importing appropriate packages.
     */
    public static Interpreter create( ClassLoader classLoader, String packageName, boolean createContext ) {
        try {
            Interpreter engine = new InterpreterEx();
    
            engine.set("scriptHelper", new ScriptHelper() );
            
            engine.setClassLoader( classLoader );
            engine.set("contextClassLoader", classLoader );
    
            // import the package
            engine.eval( "import "+ packageName+".*;" );
            engine.eval( "import javax.xml.bind.*;");

            // define helper methods
            engine.eval(
                "fail(msg) { scriptHelper.fail(msg); }" );
        
            engine.eval(
                "assert(b) { if(!b) fail(\"assertion failed\"); }" );
    
            engine.eval(
                "assert(b,msg) { if(!b) fail(msg); }" );
            
            engine.eval(
                "hasMethod(o,name) { return scriptHelper.hasMethod(o,name);}" );
                
            engine.eval(
                "roundtripSerialize(o) { return scriptHelper.roundtripSerialize(o,contextClassLoader);}" );
                
            engine.eval(
                "compareTwoTrees(o1,o2) { return scriptHelper.compareTwoTrees(context,o1,o2);}" );

            engine.eval(
                "validate(o) { return validator.validate(o);}" );
        
            engine.eval(
                "marshal(o) { return marshaller.marshal(o,System.out);}" );
            
            if( createContext ) {
                JAXBContext context = createContext(classLoader);

                engine.set("context", context );
                try {
                    engine.set("unmarshaller", context.createUnmarshaller());
                } catch( UnsupportedOperationException e ) {
                    // generated code doesn't have the unmarshaller, which is fine.
                    ; 
                }
            
                try {
                    Marshaller m = context.createMarshaller();
                    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,Boolean.TRUE);
                    m.setProperty("com.sun.xml.bind.indentString","  ");
                    engine.set("marshaller",m);
                } catch( UnsupportedOperationException e ) {
                    // generated code doesn't have the marshaller, which is fine.
                    ; 
                }
            
                try {
                    Validator v=context.createValidator();
                    v.setEventHandler(new ValidationEventDumper());
                    engine.set("validator",v);
                } catch( UnsupportedOperationException e ) {
                    // generated code doesn't have the validator, which is fine.
                    ; 
                }
            }
            
            return engine;
        } catch( EvalError e ) {
            throw new Error(e); // impossible
        } catch( JAXBException e ) {
            throw new Error(e); // impossible
        }
    }
    
    /**
     * Creates an {@link Interpreter} by adding more helper methods and
     * functions to process the specified {@link Instance}.
     */
    public static Interpreter create(
        Instance instance, ClassLoader classLoader, String packageName ) {
        
        Interpreter engine = create(classLoader,packageName,true);
        
        
        try {
            // define global functions
            // export objects
            engine.set("instance",instance);
            
            // export common function
            engine.eval(
                "unmarshal() { return unmarshaller.unmarshal(instance.document);}" );
            
            engine.eval(
                "compare(o) { return instance.compare(context,o);}" );
        } catch( Exception e ) {
            throw new Error(e); // impossible
        }
    
        return engine;
    }
    
    private static JAXBContext createContext( ClassLoader classLoader ) {
        try {
            // when we are run against an instance,
            // we can assume that the script will not measure
            // the time it takes to create JAXBContext and etc.
            // so give them more richer context
            return (JAXBContext)classLoader.loadClass("ObjectFactory").newInstance();
        } catch (InstantiationException e) {
            throw new InstantiationError(e.getMessage());
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new NoClassDefFoundError(e.getMessage());
        }
    }
}
