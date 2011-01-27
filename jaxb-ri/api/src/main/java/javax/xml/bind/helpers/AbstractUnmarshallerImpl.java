/*
 * @(#)$Id: AbstractUnmarshallerImpl.java,v 1.7 2003/02/13 23:41:06 kk122374 Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package javax.xml.bind.helpers;

import java.io.File;
import java.net.URL;
import java.net.MalformedURLException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.dom.DOMSource;
import javax.xml.bind.JAXBException;
import javax.xml.bind.PropertyException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEventHandler;
import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Partial default <tt>Unmarshaller</tt> implementation.
 * 
 * <p>
 * This class provides a partial default implementation for the
 * {@link javax.xml.bind.Unmarshaller} interface.
 * 
 * <p>
 * A JAXB Provider has to implement three methods
 * (getUnmarshallerHandler, unmarshal(Node), and
 * unmarshal(XMLReader,InputSource)
 *
 * @author <ul><li>Kohsuke Kawaguchi, Sun Microsystems, Inc.</li></ul>
 * @version $Revision: 1.7 $ $Date: 2003/02/13 23:41:06 $
 * @see javax.xml.bind.Unmarshaller
 * @since JAXB1.0
 */
public abstract class AbstractUnmarshallerImpl implements Unmarshaller
{    
    /** handler that will be used to process errors and warnings during unmarshal */
    private ValidationEventHandler eventHandler = 
        new DefaultValidationEventHandler();
    
    /** whether or not the unmarshaller will validate */
    private boolean validating = false;

    /**
     * XMLReader that will be used to parse a document.
     */
    private XMLReader reader = null;
    
    /**
     * Obtains a configured XMLReader.
     * 
     * This method is used when the client-specified
     * {@link SAXSource} object doesn't have XMLReader.
     * 
     * {@link Unmarshaller} is not re-entrant, so we will
     * only use one instance of XMLReader.
     */
    protected XMLReader getXMLReader() throws JAXBException {
        if(reader==null) {
            try {
                SAXParserFactory parserFactory;
                parserFactory = SAXParserFactory.newInstance();
                parserFactory.setNamespaceAware(true);
                // there is no point in asking a validation because 
                // there is no guarantee that the document will come with
                // a proper schemaLocation.
                parserFactory.setValidating(false);
                reader = parserFactory.newSAXParser().getXMLReader();
            } catch( ParserConfigurationException e ) {
                throw new JAXBException(e);
            } catch( SAXException e ) {
                throw new JAXBException(e);
            }
        }
        return reader;
    }
    
    public Object unmarshal( Source source ) throws JAXBException {
        if( source == null ) {
            throw new IllegalArgumentException(
                Messages.format( Messages.MUST_NOT_BE_NULL, "source" ) );
        }
        
        if(source instanceof SAXSource)
            return unmarshal( (SAXSource)source );
        if(source instanceof StreamSource)
            return unmarshal( streamSourceToInputSource((StreamSource)source));
        if(source instanceof DOMSource)
            return unmarshal( ((DOMSource)source).getNode() );
        
        // we don't handle other types of Source
        throw new IllegalArgumentException();
    }

    // use the client specified XMLReader contained in the SAXSource.
    private final Object unmarshal( SAXSource source ) throws JAXBException {
        
        XMLReader reader = source.getXMLReader();
        if( reader == null )
            reader = getXMLReader();
        
        return unmarshal( reader, source.getInputSource() );
    }

    /**
     * Unmarshals an object by using the specified XMLReader and the InputSource.
     * 
     * The callee should call the setErrorHandler method of the XMLReader
     * so that errors are passed to the client-specified ValidationEventHandler.
     */
    protected abstract Object unmarshal( XMLReader reader, InputSource source ) throws JAXBException;
    
    public final Object unmarshal( InputSource source ) throws JAXBException {
        if( source == null ) {
            throw new IllegalArgumentException(
                Messages.format( Messages.MUST_NOT_BE_NULL, "source" ) );
        }

        return unmarshal( getXMLReader(), source );
    }
        

    private Object unmarshal( String url ) throws JAXBException {
        return unmarshal( new InputSource(url) );
    }
    
    public final Object unmarshal( URL url ) throws JAXBException {
        if( url == null ) {
            throw new IllegalArgumentException(
                Messages.format( Messages.MUST_NOT_BE_NULL, "url" ) );
        }

        return unmarshal( url.toExternalForm() );
    }
    
    public final Object unmarshal( File f ) throws JAXBException {
        if( f == null ) {
            throw new IllegalArgumentException(
                Messages.format( Messages.MUST_NOT_BE_NULL, "file" ) );
        }

        try {
            // copied from JAXP
	    String path = f.getAbsolutePath();
	    if (File.separatorChar != '/')
	    	path = path.replace(File.separatorChar, '/');
	    if (!path.startsWith("/"))
	    	path = "/" + path;
	    if (!path.endsWith("/") && f.isDirectory())
	    	path = path + "/";
	    return unmarshal(new URL("file", "", path));
        } catch( MalformedURLException e ) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }
    
    public final Object unmarshal( java.io.InputStream is ) 
        throws JAXBException {
            
        if( is == null ) {
            throw new IllegalArgumentException(
                Messages.format( Messages.MUST_NOT_BE_NULL, "is" ) );
        }

        InputSource isrc = new InputSource( is );
        return unmarshal( isrc );
    }
    
    private static InputSource streamSourceToInputSource( StreamSource ss ) {
        InputSource is = new InputSource();
        is.setSystemId( ss.getSystemId() );
        is.setByteStream( ss.getInputStream() );
        is.setCharacterStream( ss.getReader() );
        
        return is;
    }
    
    
    /**
     * Indicates whether or not the Unmarshaller is configured to validate
     * during unmarshal operations.
     * <p>
     * <i><b>Note:</b> I named this method isValidating() to stay in-line
     * with JAXP, as opposed to naming it getValidating(). </i>
     *
     * @return true if the Unmarshaller is configured to validate during
     *        unmarshal operations, false otherwise
     * @throws JAXBException if an error occurs while retrieving the validating
     *        flag
     */
    public boolean isValidating() throws JAXBException {
        return validating;
    }
    
    /**
     * Allow an application to register a validation event handler.
     * <p>
     * The validation event handler will be called by the JAXB Provider if any
     * validation errors are encountered during calls to any of the
     * <tt>unmarshal</tt> methods.  If the client application does not register
     * a validation event handler before invoking the unmarshal methods, then
     * all validation events will be silently ignored and may result in
     * unexpected behaviour.
     *
     * @param handler the validation event handler
     * @throws JAXBException if an error was encountered while setting the
     *        event handler
     */
    public void setEventHandler(ValidationEventHandler handler) 
        throws JAXBException {
        
        if( handler == null ) {
            eventHandler = new DefaultValidationEventHandler();
        } else {
            eventHandler = handler;
        }
    }
    
    /**
     * Specifies whether or not the Unmarshaller should validate during
     * unmarshal operations.  By default, the <tt>Unmarshaller</tt> does
     * not validate.
     * <p>
     * This method may only be invoked before or after calling one of the
     * unmarshal methods.
     *
     * @param validating true if the Unmarshaller should validate during
     *       unmarshal, false otherwise
     * @throws JAXBException if an error occurred while enabling or disabling
     * validation at unmarshal time
     */
    public void setValidating(boolean validating) throws JAXBException {
        this.validating = validating;
    }
    
    /**
     * Return the current event handler or the default event handler if one
     * hasn't been set.
     *
     * @return the current ValidationEventHandler or the default event handler
     *        if it hasn't been set
     * @throws JAXBException if an error was encountered while getting the
     *        current event handler
     */
    public ValidationEventHandler getEventHandler() throws JAXBException {
        return eventHandler;
    }
    
    
    /**
     * Creates an UnmarshalException from a SAXException.
     * 
     * This is an utility method provided for the derived classes.
     * 
     * <p>
     * When a provider-implemented ContentHandler wants to throw a
     * JAXBException, it needs to wrap the exception by a SAXException.
     * If the unmarshaller implementation blindly wrap SAXException
     * by JAXBException, such an exception will be a JAXBException
     * wrapped by a SAXException wrapped by another JAXBException.
     * This is silly.
     * 
     * <p>
     * This method checks the nested exception of SAXException
     * and reduce those excessive wrapping.
     * 
     * @return the resulting UnmarshalException
     */
    protected UnmarshalException createUnmarshalException( SAXException e ) {
        // check the nested exception to see if it's an UnmarshalException
        Exception nested = e.getException();
        if(nested instanceof UnmarshalException)
            return (UnmarshalException)nested;
        
        if(nested instanceof RuntimeException)
            // typically this is an unexpected exception,
            // just throw it rather than wrap it, so that the full stack
            // trace can be displayed.
            throw (RuntimeException)nested;
                
        
        // otherwise simply wrap it
        if(nested!=null)
            return new UnmarshalException(nested);
        else
            return new UnmarshalException(e);
    }
    
    /**
     * Default implementation of the setProperty method always 
     * throws PropertyException since there are no required
     * properties. If a provider needs to handle additional 
     * properties, it should override this method in a derived class.
     */
    public void setProperty( String name, Object value )
        throws PropertyException {

        if( name == null ) {
            throw new IllegalArgumentException(
                Messages.format( Messages.MUST_NOT_BE_NULL, "name" ) );
        }

        throw new PropertyException(name, value);
    }
    
    /**
     * Default implementation of the getProperty method always 
     * throws PropertyException since there are no required
     * properties. If a provider needs to handle additional 
     * properties, it should override this method in a derived class.
     */
    public Object getProperty( String name )
        throws PropertyException {
            
        if( name == null ) {
            throw new IllegalArgumentException(
                Messages.format( Messages.MUST_NOT_BE_NULL, "name" ) );
        }

        throw new PropertyException(name);
    }
    
}
