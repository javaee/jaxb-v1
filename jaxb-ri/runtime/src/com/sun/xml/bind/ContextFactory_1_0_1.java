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

package com.sun.xml.bind;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 * This class is responsible for producing RI JAXBContext objects.  In
 * the RI, this is the class that the javax.xml.bind.context.factory 
 * property will point to.
 *
 * <p>
 * Used to create JAXBContext objects for v1.0.1
 * 
 * @since 1.0.1
 */
public class ContextFactory_1_0_1 {

    public static JAXBContext createContext( String contextPath, 
                                             ClassLoader classLoader ) 
        throws JAXBException {
        
        String packageName = new StringTokenizer(contextPath,":").nextToken();
        
        Class cls;
        try {
            // com/acme/foo/jaxb.properties
            String propFileName = packageName.replace( '.', '/' ) + "/jaxb.properties";
            Properties props = loadJAXBProperties( classLoader, propFileName );
            // props can't be null since we've already loaded it before.
            
            String jaxbContextImplClassName = props.getProperty(RUNTIME_KEY);
            if(jaxbContextImplClassName==null)
                throw new JAXBException( Messages.format(Messages.INCORRECT_VERSION,packageName) );
            
            cls = classLoader.loadClass(jaxbContextImplClassName);
        } catch (ClassNotFoundException e) {
            throw new JAXBException(e);
        }
        try {
            return (JAXBContext)cls.getConstructor(new Class[]{String.class,ClassLoader.class})
                .newInstance(new Object[]{contextPath,classLoader});
        } catch (InvocationTargetException e) {
            Throwable t = e.getTargetException();
            if( t==null )   t=e;
            
            throw new JAXBException(t);
        } catch (Exception e) {
            if( e instanceof RuntimeException )
                throw (RuntimeException)e;
            else
                throw new JAXBException(e);
        }
    }

    private static Properties loadJAXBProperties( ClassLoader classLoader,
                                                  String propFileName ) 
        throws JAXBException {
                                                    
        try {
            InputStream is = 
                classLoader.getResourceAsStream( propFileName );

            if( is == null )    return null;
            
            Properties props = new Properties();
            props.load( is );
            is.close();
            return props;
        } catch( IOException ioe ) {
            throw new JAXBException( ioe.toString(), ioe );
        }
    }


    /** property name used to store the build id **/
    public static final String RUNTIME_KEY = "com.sun.xml.bind.jaxbContextImpl";
}
