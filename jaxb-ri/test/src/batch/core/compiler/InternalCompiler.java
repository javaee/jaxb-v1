/*
 * @(#)$Id: InternalCompiler.java,v 1.1 2004-06-25 21:13:01 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package batch.core.compiler;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

import batch.core.Util;
import batch.core.XJCException;

/**
 * {@link Compiler} that uses XJC in the same VM.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class InternalCompiler extends AbstractXJCCompilerImpl {
    
    /**
     * ClassLoader that should be used to load the tested XJC.
     */
    private final ClassLoader ocl=Util.getForeheadClassLoader("xjc");
    
    /**
     * The run command to be invoked.
     */
    private final Method driverMain = findDriverMain();
    
    public InternalCompiler( Collection xjcParams ) {
        super(xjcParams);
    }

    public InternalCompiler() {
        super();
    }
    
    private Method findDriverMain() {
        
        try {
            Class driver = ocl.loadClass("com.sun.tools.xjc.Driver");
            return driver.getMethod("run",new Class[]{String[].class,PrintStream.class,PrintStream.class});
        } catch (ClassNotFoundException e) {
            throw new NoClassDefFoundError("Unable to find the following class from the object JAXB RI: "+e.getMessage());
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodError("Unable to find the run method in the object JAXB RI: "+e.getMessage());
        }
    }

    protected void invoke( String[] args ) throws XJCException {
        final ClassLoader ccl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(ocl);
        
        try {
            try {
                Integer r = (Integer)driverMain.invoke(null,new Object[]{args,null,System.out});
                if(r.intValue()!=0)
                    throw new XJCException("XJC exited gracefully but with error code "+r.intValue());
            } catch (InvocationTargetException e) {
                throw new XJCException(e.getTargetException());
            } catch (IllegalAccessException e) {
                throw new XJCException("Unable to invoke the run method",e);
            }
        } finally {
            Thread.currentThread().setContextClassLoader(ccl);
        }
    }
}
