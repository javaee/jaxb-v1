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

package com.sun.tools.xjc.reader.internalizer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;

import com.sun.xml.xsom.parser.JAXPParser;
import com.sun.xml.xsom.parser.XMLParser;


/**
 * Builds a DOM forest and maintains association from
 * system IDs to DOM trees.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public final class DOMForest {
    /** actual data storage map&lt;SystemId,Document>. */
    private final Map core = new HashMap();
    
    /** Stores location information for all the trees in this forest. */
    public final LocatorTable locatorTable = new LocatorTable();
    
    /** Stores all the outer-most &lt;jaxb:bindings> customizations. */
    public final Set outerMostBindings = new HashSet();
    
    /** Used to resolve references to other schema documents. */
    private EntityResolver entityResolver = null;
    
    /** Errors encountered during the parsing will be sent to this object. */
    private ErrorHandler errorHandler = null;
    
    /** Schema language dependent part of the processing. */
    protected final InternalizationLogic logic;
    
    private final SAXParserFactory parserFactory;    
    private final DocumentBuilder documentBuilder;
    
    
    public DOMForest(
        SAXParserFactory parserFactory, DocumentBuilder documentBuilder,
        InternalizationLogic logic ) {
        
        this.parserFactory = parserFactory;
        this.documentBuilder = documentBuilder;
        this.logic = logic;
    }

    public DOMForest( InternalizationLogic logic ) throws ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        this.documentBuilder = dbf.newDocumentBuilder();
            
        this.parserFactory = SAXParserFactory.newInstance();
        this.parserFactory.setNamespaceAware(true);
        
        this.logic = logic;
    }
    
    /**
     * Gets the DOM tree associated with the specified system ID,
     * or null if none is found.
     */
    public Document get( String systemId ) {
        Document doc = (Document)core.get(systemId);
        
        if( doc==null && systemId.startsWith("file:/") && !systemId.startsWith("file://") ) {
            // As of JDK1.4, java.net.URL.toExternal method returns URLs like
            // "file:/abc/def/ghi" which is an incorrect file protocol URL according to RFC1738.
            // Some other correctly functioning parts return the correct URLs ("file:///abc/def/ghi"),
            // and this descripancy breaks DOM look up by system ID.
            
            // this extra check solves this problem.
            doc = (Document)core.get( "file://"+systemId.substring(5) );
        }
        
        return doc;
    }
    
    /**
     * Gets the system ID from which the given DOM is parsed.
     * <p>
     * Poor-man's base URI.
     */
    public String getSystemId( Document dom ) {
        for( Iterator itr=core.entrySet().iterator(); itr.hasNext(); ) {
            Map.Entry e = (Map.Entry)itr.next();
            if( e.getValue()==dom )
                return (String)e.getKey();
        }
        return null;
    }

    public Document parse( InputSource source ) throws SAXException, IOException {
        if( source.getSystemId()==null )
            throw new IllegalArgumentException();
        
        return parse( source.getSystemId(), source );
    }

    /**
     * Parses an XML at the given location (
     * and XMLs referenced by it) into DOM trees
     * and stores them to this forest.
     * 
     * @return the parsed DOM document object.
     */
    public Document parse( String systemId ) throws SAXException, IOException {
        
        if( core.containsKey(systemId) )
            // this document has already been parsed. Just ignore.
            return (Document)core.get(systemId);
        
        InputSource is=null;
        
        // allow entity resolver to find the actual byte stream.
        if( entityResolver!=null )
            is = entityResolver.resolveEntity(null,systemId);
        if( is==null )
            is = new InputSource(systemId);
        
        // but we still use the original system Id as the key.
        return parse( systemId, is );
    }
    
    public Document parse( String systemId, InputSource inputSource ) throws SAXException, IOException {
        Document dom = documentBuilder.newDocument();
        
        // put into the map before growing a tree, to
        // prevent recursive reference from causing infinite loop.
        core.put( systemId, dom );
        
        try {
            XMLReader reader = parserFactory.newSAXParser().getXMLReader();
            XMLFilter f = logic.createExternalReferenceFinder(this);
            f.setParent(reader);
            reader = f;         // insert the reference finder so that
                                // included/imported schemas will be also parsed
            reader = new VersionChecker(reader);
            reader = new WhitespaceStripper(reader);
            reader.setContentHandler(new DOMBuilder(dom,locatorTable,outerMostBindings));
            if(errorHandler!=null)
                reader.setErrorHandler(errorHandler);
            if(entityResolver!=null)
                reader.setEntityResolver(entityResolver);
            reader.parse(inputSource);
        } catch( ParserConfigurationException e ) {
            // in practice, this exception won't happen.
            e.printStackTrace();
        }
        
        return dom;
    }
    
    /**
     * Performs internalization.
     * 
     * This method should be called only once, only after all the
     * schemas are parsed.
     * 
     * @exception SAXException
     *      Errors will be passed to the error handler, but the error handler
     *      can throw SAXException.
     */
    public void transform() throws SAXException {
        Internalizer.transform(this);
    }
    
    /**
     * Creates {@link XMLParser} for XSOM which reads documents from
     * this DOMForest rather than doing a fresh parse.
     * 
     * The net effect is that XSOM will read transformed XML Schemas
     * instead of the original documents.
     */
    public XMLParser createParser() {
        return new DOMForestParser(this,new JAXPParser());
    }
    
    
    
    public EntityResolver getEntityResolver() {
        return entityResolver;
    }

    public void setEntityResolver(EntityResolver entityResolver) {
        this.entityResolver = entityResolver;
    }
    
    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    public void setErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }
    
    /**
     * Gets all the parsed documents.
     */
    public Document[] listDocuments() {
        return (Document[])core.values().toArray(new Document[core.size()]);
    }
    
    /**
     * Gets all the system IDs of the documents.
     */
    public String[] listSystemIDs() {
        return (String[]) core.keySet().toArray(new String[core.keySet().size()]);
    }
    
    /**
     * Dumps the contents of the forest to the specified stream.
     * 
     * This is a debug method. As such, error handling is sloppy.
     */
    public void dump( OutputStream out ) throws IOException {
        try {
            // create identity transformer
            Transformer it = TransformerFactory.newInstance().newTransformer();
            
            for( Iterator itr=core.entrySet().iterator(); itr.hasNext(); ) {
                Map.Entry e = (Map.Entry)itr.next();
                
                out.write( ("---<< "+e.getKey()+"\n").getBytes() );
                
                it.transform( new DOMSource((Document)e.getValue()), new StreamResult(out) );
                
                out.write( "\n\n\n".getBytes() );
            }
        } catch( TransformerException e ) {
            e.printStackTrace();
        }
    }
}
