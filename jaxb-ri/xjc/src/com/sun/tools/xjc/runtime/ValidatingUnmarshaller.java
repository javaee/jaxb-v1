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

/*
 * @(#)$Id: ValidatingUnmarshaller.java,v 1.2 2005-09-10 18:20:45 kohsuke Exp $
 */
package com.sun.tools.xjc.runtime;

import javax.xml.bind.JAXBException;
import javax.xml.bind.ValidationEvent;

import org.iso_relax.verifier.impl.ForkContentHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.sun.msv.grammar.Grammar;
import com.sun.msv.verifier.Verifier;
import com.sun.msv.verifier.VerifierFilter;
import com.sun.msv.verifier.regexp.REDocumentDeclaration;
import com.sun.xml.bind.validator.Locator;

/**
 * Filter implementation of SAXUnmarshallerHandler.
 * 
 * <p>
 * This component internally uses a VerifierFilter to validate
 * SAX events that goes through this component.
 * Discovered error information is just passed down to the next component.
 * 
 * <p>
 * This will enable the implementation to validate all sources of SAX events
 * in the RI - XMLReader, DOMScanner
 *
 * SAX events will go the VerifierFilter and then to the SAXUnmarshaller...
 *
 */
public class ValidatingUnmarshaller extends ForkContentHandler
    implements SAXUnmarshallerHandler {
    
    /**
     * Creates a new instance of ValidatingUnmarshaller.
     */
    public static ValidatingUnmarshaller create( Grammar grammar, 
                            SAXUnmarshallerHandler _core,
                            Locator locator ) {
        
        // create a VerifierFilter and configure it
        // so that error messages will be sent to the core,
        Verifier v = new Verifier( 
            new REDocumentDeclaration(grammar),
            new ErrorHandlerAdaptor(_core,locator) );
        v.setPanicMode( true );

        return new ValidatingUnmarshaller(
            new VerifierFilter( v ), _core );
    }
    
    private ValidatingUnmarshaller( VerifierFilter filter,
                            SAXUnmarshallerHandler _core ) {
        
        super( filter, _core );
        this.core = _core;
    }
    
    // delegate to the next component
    public Object getResult() throws JAXBException, IllegalStateException { 
        return core.getResult();
    }

    public void handleEvent(ValidationEvent event, boolean canRecover ) throws SAXException {
        // SAXUnmarshallerHandler already checks for RuntimeExceptions, so 
        // there is no need to wrap this call in a try/catch
        core.handleEvent(event,canRecover);
    }
    
    private final SAXUnmarshallerHandler core;
    
    
    private final AttributesImpl xsiLessAtts = new AttributesImpl();
    
    public void startElement( String nsUri, String local, String qname, Attributes atts ) throws SAXException {
        // create an attributes set for MSV that doesn't contains xsi:schemaLocation
        xsiLessAtts.clear();
        int len = atts.getLength();
        for( int i=0; i<len; i++ ) {
            String aUri = atts.getURI(i);
            String aLocal = atts.getLocalName(i);
            if(aUri.equals("http://www.w3.org/2001/XMLSchema-instance")
            && (aLocal.equals("schemaLocation") ||
                aLocal.equals("noNamespaceSchemaLocation") //||
                /*aLocal.equals("type")*/))
                continue;
                
            // we do handle xsi:nil.
            xsiLessAtts.addAttribute( aUri, aLocal,
                atts.getQName(i), atts.getType(i), atts.getValue(i) );
        }
        
        super.startElement(nsUri,local,qname,xsiLessAtts);
    }
}







