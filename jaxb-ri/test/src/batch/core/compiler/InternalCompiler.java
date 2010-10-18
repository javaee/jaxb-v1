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
