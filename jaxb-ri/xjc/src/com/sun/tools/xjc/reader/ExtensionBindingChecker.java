/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.NamespaceSupport;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * This filter checks jaxb:extensionBindingPrefix and
 * pass/filter extension bindings.
 * 
 * <p>
 * This filter also remembers enabled extension namespaces
 * and filters out any foreign namespaces that doesn't belong
 * to those. The net effect is that disabled customizations
 * will never pass through this filter.
 * 
 * <p>
 * The class needs to know the list of extension binding namespaces
 * that the RI recognizes.
 * To add new URI, modify the isSupportedExtension method.
 * 

 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class ExtensionBindingChecker extends XMLFilterImpl {
    
    /** Remembers in-scope namespace bindings. */
    private final NamespaceSupport nsSupport = new NamespaceSupport();
    
    /**
     * Number of the elements encountered. Used to detect the root element.
     */
    private int count=0;
    
    /** Set of namespace URIs that designates enabled extensions. */
    private final Set enabledExtensions = new HashSet();
    
    private Locator locator;
    
    /**
     * When we are pruning a sub tree, this field holds the depth of
     * elements that are being cut. Used to resume event forwarding.
     * 
     * As long as this value is 0, we will pass through data.     */
    private int cutDepth=0;
    
    /**
     * This object will receive SAX events while a sub tree is being
     * pruned.     */
    private static final ContentHandler stub = new DefaultHandler();
    
    /**
     * This field remembers the user-specified ContentHandler.
     * So that we can restore it once the sub tree is completely pruned.     */
    private ContentHandler next;
    
    /**
     * Namespace URI of the target schema language. Elements in this 
     * namespace are always allowed.
     */
    private final String schemaLanguage;
    
    
    /**
     * @param handler
     *      This error handler will receive detected errors.
     */
    public ExtensionBindingChecker( String schemaLanguage, ErrorHandler handler ) {
        this.schemaLanguage = schemaLanguage;
        setErrorHandler(handler);
    }
    
    /**
     * Checks if the given namespace URI is supported as the extension
     * bindings.
     */
    protected boolean isSupportedExtension( String namespaceUri ) {
        // so far we only support one extension
        return namespaceUri.equals(Const.XJC_EXTENSION_URI);
    }

    /**
     * Returns true if the elements with the given namespace URI
     * should be blocked by this filter.
     */
    private boolean needsToBePruned( String uri ) {
        if( uri.equals(schemaLanguage) )
            return false;
        if( uri.equals(Const.JAXB_NSURI) )
            return false;
        if( enabledExtensions.contains(uri) )
            return false;
        
        // we don't need to prune something unless
        // the rest of the processor recognizes it as something special.
        // this allows us to send the documentation and other harmless
        // foreign XML fragments, which may be picked up as documents.
        return isSupportedExtension(uri);
    }

    

    public void startDocument() throws SAXException {
        super.startDocument();
        
        count=0;
        cutDepth=0;
        nsSupport.reset();
        enabledExtensions.clear();
    }
    
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        super.startPrefixMapping(prefix, uri);
        nsSupport.pushContext();
        nsSupport.declarePrefix(prefix,uri);
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        super.endPrefixMapping(prefix);
        nsSupport.popContext();
    }

    public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
        throws SAXException {
        
        if( cutDepth==0 ) {
            String v = atts.getValue(Const.JAXB_NSURI,"extensionBindingPrefixes");
            if(v!=null) {
                if(count!=0)
                    // the binding attribute is allowed only at the root level.
                    error( Messages.format( Messages.ERR_UNEXPECTED_EXTENSION_BINDING_PREFIXES ) );
                
                // then remember the associated namespace URIs.
                StringTokenizer tokens = new StringTokenizer(v);
                while(tokens.hasMoreTokens()) {
                    String prefix = tokens.nextToken();
                    String uri = nsSupport.getURI(prefix);
                    if( uri==null ) {
                        // undeclared prefix
                        error( Messages.format( Messages.ERR_UNDECLARED_PREFIX, prefix ) );
                    } else {
                        if( !isSupportedExtension(uri) )
                            // we currently don't support any extension. report errors.
                            error( Messages.format( Messages.ERR_UNSUPPORTED_EXTENSION, prefix ) );
                        
                        enabledExtensions.add(uri);
                    }
                }
            }
            
            if( needsToBePruned(namespaceURI) ) {
                // start pruning the tree. Call the super class method directly.
                if( isSupportedExtension(namespaceURI) ) {
                    // but this is a supported customization.
                    // isn't the user forgetting @jaxb:extensionBindingPrefixes?
                    warning( Messages.format( Messages.ERR_SUPPORTED_EXTENSION_IGNORED, namespaceURI ) );
                }
                super.setContentHandler(stub);
                cutDepth=1;
            }
        } else
            cutDepth++;
        
        count++;
        super.startElement(namespaceURI, localName, qName, atts);
    }

    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        super.endElement(namespaceURI, localName, qName);
        
        if( cutDepth!=0 ) {
            cutDepth--;
            if( cutDepth == 0 )
                // pruning completed. restore the user handler
                super.setContentHandler(next);
        }
    }

    public void setDocumentLocator(Locator locator) {
        super.setDocumentLocator(locator);
        this.locator = locator;
    }
    
    public void setContentHandler(ContentHandler handler) {
        next = handler;
        // changes take effect immediately unless the sub-tree is being pruned
        if(getContentHandler()!=stub)
            super.setContentHandler(handler);
    }
    
    
    /**
     * Reports an error and returns the created SAXParseException
     */
    private SAXParseException error( String msg ) throws SAXException {
        SAXParseException spe = new SAXParseException( msg, locator );
        getErrorHandler().error(spe);
        return spe;
    }
    
    /**
     * Reports a warning.
     */
    private void warning( String msg ) throws SAXException {
        SAXParseException spe = new SAXParseException( msg, locator );
        getErrorHandler().warning(spe);
    }

}
