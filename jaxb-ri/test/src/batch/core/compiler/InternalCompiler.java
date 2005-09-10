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
