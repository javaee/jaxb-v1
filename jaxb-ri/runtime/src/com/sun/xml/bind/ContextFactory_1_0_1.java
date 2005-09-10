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
