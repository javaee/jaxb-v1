/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/*
 * @(#)$Id: UnmarshallerImpl.java,v 1.1 2004-06-25 21:15:24 kohsuke Exp $
 */
package com.sun.tools.xjc.runtime;

import java.io.IOException;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshallerHandler;
import javax.xml.bind.helpers.AbstractUnmarshallerImpl;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import com.sun.xml.bind.DatatypeConverterImpl;
import com.sun.xml.bind.unmarshaller.DOMScanner;
import com.sun.xml.bind.unmarshaller.InterningXMLReader;
import com.sun.xml.bind.validator.DOMLocator;
import com.sun.xml.bind.validator.Locator;
import com.sun.xml.bind.validator.SAXLocator;

/**
 * Default Unmarshall implementation.
 * 
 * <p>
 * This class can be extended by the generated code to provide
 * type-safe unmarshall methods.
 *
 * @author
 *  <a href="mailto:kohsuke.kawaguchi@sun.com">Kohsuke KAWAGUCHI</a>
 */
public class UnmarshallerImpl extends AbstractUnmarshallerImpl
{
    /** parent JAXBContext object that created this unmarshaller */
    private DefaultJAXBContextImpl context = null;
    
    private final GrammarInfo grammarInfo;
    
    public UnmarshallerImpl( DefaultJAXBContextImpl context, GrammarInfo gi ) {
        
        this.context = context;
        this.grammarInfo = gi;

        // initialize datatype converter with ours
        DatatypeConverter.setDatatypeConverter(DatatypeConverterImpl.theInstance);
    }
    
    public void setValidating(boolean validating) throws JAXBException {
        if( MetaVariable.W ) {// META-IF(W)
            super.setValidating(validating);
            if(validating==true)
                // make sure that we can actually load the grammar.
                // this could be a lengthy operation if your schema is big.
                context.getGrammar();
        } else {// META-ELSE
            throw new UnsupportedOperationException(
                "When generating this code, the compiler option was specified not to generate code for the unmarshal-time validation");
        }// META-ENDIF
    }
    
    public UnmarshallerHandler getUnmarshallerHandler() {
        // TODO: use only one instance.
        
        // use InterningUnmarshallerHandler since we don't know
        // if the caller will intern Strings before firing SAX events.
        
        // we don't know the Locator to be used,
        // but SAXLocator would always be a good default,
        // as the source of SAX2 events can always set org.xml.sax.Locator.
        return new InterningUnmarshallerHandler( 
                createUnmarshallerHandler(new SAXLocator()));
    }
    
    
    
    /**
     * Creates and configures a new unmarshalling pipe line.
     * Depending on the setting, we put a validator as a filter.
     * 
     * @return
     *      A component that implements both UnmarshallerHandler
     *      and ValidationEventHandler. All the parsing errors
     *      should be reported to this error handler for the unmarshalling
     *      process to work correctly.
     * 
     * @param locator
     *      The object that is responsible to obtain the source
     *      location information for {@link ValidationEvent}s.
     */
    private SAXUnmarshallerHandler createUnmarshallerHandler( Locator locator ) {

        SAXUnmarshallerHandler unmarshaller =
            new SAXUnmarshallerHandlerImpl( this, grammarInfo );

// META-IF(W)
        try {
            
            // use the simple check to determine if validation is on
            if( isValidating() ) { 
                // if the validation is turned on, insert another
                // component into the event pipe line.
                unmarshaller = ValidatingUnmarshaller.create(
                    context.getGrammar(), unmarshaller, locator );
            }
        } catch( JAXBException e ) {
            // impossible since we've already made sure that a grammar is accessible.
            e.printStackTrace();
        }
// META-ENDIF
        
        return unmarshaller;
    }


    protected Object unmarshal( XMLReader reader, InputSource source ) throws JAXBException {
        
        SAXLocator locator = new SAXLocator();
        SAXUnmarshallerHandler handler = createUnmarshallerHandler(locator);
        
        reader = InterningXMLReader.adapt(reader);
        
        reader.setContentHandler(handler);
        // saxErrorHandler will be set by the createUnmarshallerHandler method.
        // configure XMLReader so that the error will be sent to it.
        // This is essential for the UnmarshallerHandler to be able to abort
        // unmarshalling when an error is found.
        //
        // Note that when this XMLReader is provided by the client code,
        // it might be already configured to call a client error handler.
        // This will clobber such handler, if any.
        //
        // Ryan noted that we might want to report errors to such a client
        // error handler as well.
        reader.setErrorHandler(
            new ErrorHandlerAdaptor(handler,locator));
        
        try {
            reader.parse(source);
        } catch( IOException e ) {
            throw new JAXBException(e);
        } catch( SAXException e ) {
            throw createUnmarshalException(e);
        }
        
        Object result = handler.getResult();
        
        // avoid keeping unnecessary references too long to let the GC
        // reclaim more memory.
        // setting null upsets some parsers, so use a dummy instance instead.
        reader.setContentHandler(dummyHandler);
        reader.setErrorHandler(dummyHandler);
        
        return result;
    }
    
    public final Object unmarshal( Node node ) throws JAXBException {
        try {
            DOMScanner scanner = new DOMScanner();
            UnmarshallerHandler handler = new InterningUnmarshallerHandler( 
                createUnmarshallerHandler(new DOMLocator(scanner)));
            
            if(node instanceof Element)
                scanner.parse((Element)node,handler);
            else
            if(node instanceof Document)
                scanner.parse(((Document)node).getDocumentElement(),handler);
            else
                // no other type of input is supported
                throw new IllegalArgumentException();
            
            return handler.getResult();
        } catch( SAXException e ) {
            throw createUnmarshalException(e);
        }
    }
    
    private static final DefaultHandler dummyHandler = new DefaultHandler();
}
