/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2001-2011 Oracle and/or its affiliates. All rights reserved.
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

package javax.xml.bind.helpers;

import javax.xml.bind.Marshaller;
import javax.xml.bind.JAXBException;
import javax.xml.bind.PropertyException;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.dom.DOMResult;

import java.io.UnsupportedEncodingException;
// J2SE1.4 feature
// import java.nio.charset.Charset;
// import java.nio.charset.UnsupportedCharsetException;

/**
 * Partial default <tt>Marshaller</tt> implementation.
 * 
 * <p>
 * This class provides a partial default implementation for the
 * {@link javax.xml.bind.Marshaller} interface.
 * 
 * <p>
 * The only method that a JAXB Provider has to implement is
 * {@link Marshaller#marshal(Object, Result) marshal(Object, javax.xml.transform.Result)}.
 *
 * @author <ul><li>Kohsuke Kawaguchi, Sun Microsystems, Inc.</li></ul>
 * @version $Revision: 1.21 $ $Date: 2003/02/13 23:41:06 $
 * @see javax.xml.bind.Marshaller
 * @since JAXB1.0
 */
public abstract class AbstractMarshallerImpl implements Marshaller
{
    /** handler that will be used to process errors and warnings during marshal */
    private ValidationEventHandler eventHandler = 
        new DefaultValidationEventHandler();
    
    //J2SE1.4 feature
    //private Charset encoding = null;
    
    /** store the value of the encoding property. */
    private String encoding = "UTF-8";
    
    /** store the value of the schemaLocation property. */
    private String schemaLocation = null;

    /** store the value of the noNamespaceSchemaLocation property. */
    private String noNSSchemaLocation = null;
    
    /** store the value of the formattedOutput property. */
    private boolean formattedOutput = false;
           
    public final void marshal( Object obj, java.io.OutputStream os ) 
        throws JAXBException {
            
        checkNotNull( obj, "obj", os, "os" );
        marshal( obj, new StreamResult(os) );
    }
    
    public final void marshal( Object obj, java.io.Writer w ) 
        throws JAXBException {
            
        checkNotNull( obj, "obj", w, "writer" );
        marshal( obj, new StreamResult(w) );
    }
    
    public final void marshal( Object obj, org.xml.sax.ContentHandler handler ) 
        throws JAXBException {
            
        checkNotNull( obj, "obj", handler, "handler" );
        marshal( obj, new SAXResult(handler) );
    }
    
    public final void marshal( Object obj, org.w3c.dom.Node node ) 
        throws JAXBException {
            
        checkNotNull( obj, "obj", node, "node" );
        marshal( obj, new DOMResult(node) );
    }
    
    /**
     * By default, the getNode method is unsupported and throw
     * an {@link java.lang.UnsupportedOperationException}.
     * 
     * Implementations that choose to support this method must
     * override this method.
     */
    public org.w3c.dom.Node getNode( Object obj ) throws JAXBException {
        
        checkNotNull( obj, "obj", Boolean.TRUE, "foo" );
        
        throw new UnsupportedOperationException();
    }
    
    /**
     * Convenience method for getting the current output encoding.
     * 
     * @return the current encoding or "UTF-8" if it hasn't been set.
     */
    protected String getEncoding() {
        return encoding;
    }
    
    /**
     * Convenience method for setting the output encoding.
     * 
     * @param encoding a valid encoding as specified in the Marshaller class 
     * documentation
     */
    protected void setEncoding( String encoding ) {
        this.encoding = encoding;
    }
    
    /**
     * Convenience method for getting the current schemaLocation.
     * 
     * @return the current schemaLocation or null if it hasn't been set
     */
    protected String getSchemaLocation() {
        return schemaLocation;
    }
    
    /**
     * Convenience method for setting the schemaLocation.
     * 
     * @param location the schemaLocation value
     */
    protected void setSchemaLocation( String location ) {
        schemaLocation = location;
    }
    
    /**
     * Convenience method for getting the current noNamespaceSchemaLocation.
     * 
     * @return the current noNamespaceSchemaLocation or null if it hasn't
     * been set
     */
    protected String getNoNSSchemaLocation() {
        return noNSSchemaLocation;
    }
    
    /**
     * Convenience method for setting the noNamespaceSchemaLocation.
     * 
     * @param location the noNamespaceSchemaLocation value
     */
    protected void setNoNSSchemaLocation( String location ) {
        noNSSchemaLocation = location;
    }
    
    /**
     * Convenience method for getting the formatted output flag.
     * 
     * @return the current value of the formatted output flag or false if
     * it hasn't been set.
     */
    protected boolean isFormattedOutput() {
        return formattedOutput;
    }
    
    /**
     * Convenience method for setting the formatted output flag.
     * 
     * @param v value of the formatted output flag.
     */
    protected void setFormattedOutput( boolean v ) {
        formattedOutput = v;
    }
    
    
    static String[] aliases = {
        "UTF-8", "UTF8",
        "UTF-16", "Unicode",
        "UTF-16BE", "UnicodeBigUnmarked",
        "UTF-16LE", "UnicodeLittleUnmarked",
        "US-ASCII", "ASCII",
        "TIS-620", "TIS620",
        
        // taken from the project-X parser
        "ISO-10646-UCS-2", "Unicode",
    
        "EBCDIC-CP-US", "cp037",
        "EBCDIC-CP-CA", "cp037",
        "EBCDIC-CP-NL", "cp037",
        "EBCDIC-CP-WT", "cp037",
    
        "EBCDIC-CP-DK", "cp277",
        "EBCDIC-CP-NO", "cp277",
        "EBCDIC-CP-FI", "cp278",
        "EBCDIC-CP-SE", "cp278",
    
        "EBCDIC-CP-IT", "cp280",
        "EBCDIC-CP-ES", "cp284",
        "EBCDIC-CP-GB", "cp285",
        "EBCDIC-CP-FR", "cp297",
    
        "EBCDIC-CP-AR1", "cp420",
        "EBCDIC-CP-HE", "cp424",
        "EBCDIC-CP-BE", "cp500",
        "EBCDIC-CP-CH", "cp500",
    
        "EBCDIC-CP-ROECE", "cp870",
        "EBCDIC-CP-YU", "cp870",
        "EBCDIC-CP-IS", "cp871",
        "EBCDIC-CP-AR2", "cp918",
        
        // IANA also defines two that JDK 1.2 doesn't handle:
        //  EBCDIC-CP-GR        --> CP423
        //  EBCDIC-CP-TR        --> CP905
    };
    
    /**
     * Gets the corresponding Java encoding name from an IANA name.
     * 
     * This method is a helper method for the derived class to convert
     * encoding names.
     * 
     * @exception UnsupportedEncodingException
     *      If this implementation couldn't find the Java encoding name.
     */
    protected String getJavaEncoding( String encoding ) throws UnsupportedEncodingException {
        try {
            "1".getBytes(encoding);
            return encoding;
        } catch( UnsupportedEncodingException e ) {
            // try known alias
            for( int i=0; i<aliases.length; i+=2 ) {
                if(encoding.equals(aliases[i])) {
                    "1".getBytes(aliases[i+1]);
                    return aliases[i+1];
                }
            }
            
            throw new UnsupportedEncodingException(encoding);
        }
        /* J2SE1.4 feature
        try {
            this.encoding = Charset.forName( _encoding );
        } catch( UnsupportedCharsetException uce ) {
            throw new JAXBException( uce );
        }
         */
    }
    
    /**
     * Default implementation of the setProperty method handles
     * the four defined properties in Marshaller. If a provider 
     * needs to handle additional properties, it should override 
     * this method in a derived class.
     */
    public void setProperty( String name, Object value )
        throws PropertyException {
        
        if( name == null ) {
            throw new IllegalArgumentException( 
                Messages.format( Messages.MUST_NOT_BE_NULL, "name" ) );
        }
        
        // recognize and handle four pre-defined properties.
        if( JAXB_ENCODING.equals(name) ) {
            checkString( name, value );
            setEncoding( (String)value );
            return;
        }
        if( JAXB_FORMATTED_OUTPUT.equals(name) ) {
            checkBoolean( name, value );                    
            setFormattedOutput( ((Boolean)value).booleanValue() );
            return;
        }
        if( JAXB_NO_NAMESPACE_SCHEMA_LOCATION.equals(name) ) {
            checkString( name, value );
            setNoNSSchemaLocation( (String)value );
            return;
        }
        if( JAXB_SCHEMA_LOCATION.equals(name) ) {
            checkString( name, value );
            setSchemaLocation( (String)value );
            return;
        }
        
        throw new PropertyException(name, value);
    }
    
    /**
     * Default implementation of the getProperty method handles
     * the four defined properties in Marshaller.  If a provider 
     * needs to support additional provider specific properties, 
     * it should override this method in a derived class.
     */
    public Object getProperty( String name )
        throws PropertyException {
            
        if( name == null ) {
            throw new IllegalArgumentException( 
                Messages.format( Messages.MUST_NOT_BE_NULL, "name" ) );
        }
        
        // recognize and handle four pre-defined properties.
        if( JAXB_ENCODING.equals(name) )
            return getEncoding();
        if( JAXB_FORMATTED_OUTPUT.equals(name) )
            return isFormattedOutput()?Boolean.TRUE:Boolean.FALSE;
        if( JAXB_NO_NAMESPACE_SCHEMA_LOCATION.equals(name) )
            return getNoNSSchemaLocation();
        if( JAXB_SCHEMA_LOCATION.equals(name) )
            return getSchemaLocation();
                   
        throw new PropertyException(name);
    }
    /**
     * @see javax.xml.bind.Marshaller#getEventHandler()
     */
    public ValidationEventHandler getEventHandler() throws JAXBException {
        return eventHandler;
    }

    /**
     * @see javax.xml.bind.Marshaller#setEventHandler(ValidationEventHandler)
     */
    public void setEventHandler(ValidationEventHandler handler)
        throws JAXBException {
        
        if( handler == null ) {
            eventHandler = new DefaultValidationEventHandler();
        } else {
            eventHandler = handler;
        }
    }




    /*
     * assert that the given object is a Boolean
     */
    private void checkBoolean( String name, Object value ) throws PropertyException {
        if(!(value instanceof Boolean))
            throw new PropertyException(
                Messages.format( Messages.MUST_BE_BOOLEAN, name ) );
    }
    
    /*
     * assert that the given object is a String
     */
    private void checkString( String name, Object value ) throws PropertyException {
        if(!(value instanceof String))
            throw new PropertyException(
                Messages.format( Messages.MUST_BE_STRING, name ) );
    }
    
    /*
     * assert that the parameters are not null
     */
    private void checkNotNull( Object o1, String o1Name,
                               Object o2, String o2Name ) {
    
        if( o1 == null ) {
            throw new IllegalArgumentException( 
                Messages.format( Messages.MUST_NOT_BE_NULL, o1Name ) );
        }
        if( o2 == null ) {
            throw new IllegalArgumentException( 
                Messages.format( Messages.MUST_NOT_BE_NULL, o2Name ) );
        }
    }
}
