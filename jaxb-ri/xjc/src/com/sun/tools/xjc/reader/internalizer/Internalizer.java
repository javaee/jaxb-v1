/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader.internalizer;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.TransformerException;

import com.sun.org.apache.xpath.internal.XPathAPI;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.sun.tools.xjc.reader.Const;
import com.sun.tools.xjc.util.DOMUtils;
import com.sun.tools.xjc.util.EditDistance;



/**
 * Internalizes external binding declarations.
 * <p>
 * The static "transform" method is the entry point.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
class Internalizer {

    /**
     * Internalize all &lt;jaxb:bindings> customizations in the given forest.
     * 
     * @exception SAXException
     *      Errors will be passed to the error handler, but the error handler
     *      can throw SAXException.
     */
    static void transform( DOMForest forest ) throws SAXException {
        new Internalizer( forest ).transform();
    }

    
    private Internalizer( DOMForest forest ) {
        this.errorHandler = forest.getErrorHandler();
        this.forest = forest;
    }
    
    /**
     * DOMForest object currently being processed.
     */
    private final DOMForest forest;
    
    /**
     * All errors found during the transformation is sent to this object.
     */
    private ErrorHandler errorHandler;
    
    
    
    private void transform() throws SAXException {
        
        Map targetNodes = new HashMap();
        
        //
        // identify target nodes for all <jaxb:bindings>
        //
        for( Iterator itr = forest.outerMostBindings.iterator(); itr.hasNext(); ) {
            Element jaxbBindings = (Element)itr.next();
            
            // initially, the inherited context is itself
            buildTargetNodeMap( jaxbBindings, jaxbBindings, targetNodes );
        }
        
        //
        // then move them to their respective positions.
        //
        for( Iterator itr = forest.outerMostBindings.iterator(); itr.hasNext(); ) {
            Element jaxbBindings = (Element)itr.next();
            
            move( jaxbBindings, targetNodes );
        }
        
    }
    
    /**
     * Validates attributes of a &lt;jaxb:bindings> element.     */
    private void validate( Element bindings ) throws SAXException {
        NamedNodeMap atts = bindings.getAttributes();
        for( int i=0; i<atts.getLength(); i++ ) {
            Attr a = (Attr)atts.item(i);
            if( a.getNamespaceURI()!=null )
                continue;   // all foreign namespace OK.
            if( a.getLocalName().equals("node") )
                continue;
            if( a.getLocalName().equals("schemaLocation"))
                continue;
            
            // TODO: flag error for this undefined attribute
        }
    }
    
    /**
     * Determines the target node of the "bindings" element
     * by using the inherited target node, then put
     * the result into the "result" map.
     */
    private void buildTargetNodeMap( Element bindings, Node inheritedTarget, Map result )
        throws SAXException {
        // start by the inherited target
        Node target = inheritedTarget;
        
        validate(bindings); // validate this node
        
        // look for @schemaLocation
        if( bindings.getAttributeNode("schemaLocation")!=null ) {
            String schemaLocation = bindings.getAttribute("schemaLocation");
            
            try {
                // absolutize this URI.
                // TODO: use the URI class
                // TODO: honor xml:base
                schemaLocation = new URL(
                    new URL( forest.getSystemId(bindings.getOwnerDocument()) ),
                    schemaLocation ).toExternalForm();
            } catch( MalformedURLException e ) {
                ;   // continue with the original schemaLocation value
            }
            
            target = forest.get(schemaLocation);
            if(target==null) {
                reportError( bindings,
                    Messages.format(Messages.ERR_INCORRECT_SCHEMA_REFERENCE,
                        schemaLocation,
                        EditDistance.findNearest(schemaLocation,forest.listSystemIDs())));
                
                return; // abort processing this <jaxb:bindings>
            }
        }
        
        // look for @node
        if( bindings.getAttributeNode("node")!=null ) {
            String nodeXPath = bindings.getAttribute("node");
            
            // evaluate this XPath
            NodeList nlst;
            try {
                nlst = XPathAPI.selectNodeList( target, nodeXPath, bindings );
            } catch( TransformerException e ) {
                reportError( bindings,
                    Messages.format(Messages.ERR_XPATH_EVAL,e.getMessage()),
                    e );
                return; // abort processing this <jaxb:bindings>
            }
            
            if( nlst.getLength()==0 ) {
                reportError( bindings,
                    Messages.format(Messages.NO_XPATH_EVAL_TO_NO_TARGET,
                        nodeXPath) );
                return; // abort
            }
            
            if( nlst.getLength()!=1 ) {
                reportError( bindings,
                    Messages.format(Messages.NO_XPATH_EVAL_TOO_MANY_TARGETS,
                        nodeXPath,new Integer(nlst.getLength())) );
                return; // abort
            }
            
            Node rnode = nlst.item(0);
            if(!(rnode instanceof Element )) {
                reportError( bindings,
                    Messages.format(Messages.NO_XPATH_EVAL_TO_NON_ELEMENT,
                        nodeXPath) );
                return; // abort
            }
            
            if( !forest.logic.checkIfValidTargetNode(forest,bindings,(Element)rnode) ) {
                reportError( bindings,
                    Messages.format(Messages.XPATH_EVAL_TO_NON_SCHEMA_ELEMENT,
                        nodeXPath,
                        rnode.getNodeName() ) );
                return; // abort
            }
            
            target = (Element)rnode;
        }
        
        // update the result map
        result.put( bindings, target );
        
        // look for child <jaxb:bindings> and process them recursively
        Element[] children = DOMUtils.getChildElements( bindings, Const.JAXB_NSURI, "bindings" );
        for( int i=0; i<children.length; i++ )
            buildTargetNodeMap( children[i], target, result );
    }
    
    /**
     * Moves JAXB customizations under their respective target nodes.
     */
    private void move( Element bindings, Map targetNodes ) throws SAXException {
        Node target = (Node)targetNodes.get(bindings);
        if(target==null)
            // this must be the result of an error on the external binding.
            // recover from the error by ignoring this node
            return;
        
        Element[] children = DOMUtils.getChildElements(bindings);
        for( int i=0; i<children.length; i++ ) {
            Element item = children[i];
            
            if( "bindings".equals(item.getLocalName()) )
                // process child <jaxb:bindings> recursively
                move( (Element)item, targetNodes );
            else {
                if(!(target instanceof Element )) {
                    reportError( item,
                        Messages.format(Messages.CONTEXT_NODE_IS_NOT_ELEMENT ) );
                    return; // abort
                }
                
                if(!forest.logic.checkIfValidTargetNode(forest,item,(Element)target)) {
                    reportError( item,
                        Messages.format(Messages.ORPHANED_CUSTOMIZATION, item.getNodeName() ) );
                    return; // abort
                }
                // move this node under the target
                moveUnder( (Element)item, (Element)target );
            }
        }
    }
    
    /**
     * Moves the "decl" node under the "target" node.
     * 
     * @param decl
     *      A JAXB customization element (e.g., &lt;jaxb:class>)
     * 
     * @param target
     *      XML Schema element under which the declaration should move.
     *      For example, &lt;xs:element>
     */
    private void moveUnder( Element decl, Element target ) {
        Element realTarget = forest.logic.refineTarget(target);
        
        declExtensionNamespace( decl, target );
        
        // copy in-scope namespace declarations of the decl node
        // to the decl node itself so that this move won't change
        // the in-scope namespace bindings.
        Element p = decl;
        Set inscopes = new HashSet();
        while(true) {
            NamedNodeMap atts = p.getAttributes();
            for( int i=0; i<atts.getLength(); i++ ) {
                Attr a = (Attr)atts.item(i);
                if( Const.XMLNS_URI.equals(a.getNamespaceURI()) ) {
                    String prefix;
                    if( a.getName().indexOf(':')==-1 )  prefix = "";
                    else                                prefix = a.getLocalName();
                    
                    if( inscopes.add(prefix) && p!=decl ) {
                        // if this is the first time we see this namespace bindings,
                        // copy the declaration.
                        // if p==decl, there's no need to. Note that
                        // we want to add prefix to inscopes even if p==Decl
                        
                        decl.setAttributeNodeNS( (Attr)a.cloneNode(true) );
                    }
                }
            }
            
            if( p.getParentNode() instanceof Document )
                break;
            
            p = (Element)p.getParentNode();
        }
        
        if( !inscopes.contains("") ) {
            // if the default namespace was undeclared in the context of decl,
            // it must be explicitly set to "" since the new environment might
            // have a different default namespace URI.
            decl.setAttributeNS(Const.XMLNS_URI,"xmlns","");
        }


        // finally move the declaration to the target node.
        if( realTarget.getOwnerDocument()!=decl.getOwnerDocument() ) {
            // if they belong to different DOM documents, we need to clone them
            Element original = decl;
            decl = (Element)realTarget.getOwnerDocument().importNode(decl,true);
            
            // this effectively clones a ndoe,, so we need to copy locators.
            copyLocators( original, decl );
        }
        
        
        realTarget.appendChild( decl );
    }
    
    /**
     * Recursively visits sub-elements and declare all used namespaces.
     * TODO: the fact that we recognize all namespaces in the extension
     * is a bad design.
     */
    private void declExtensionNamespace(Element decl, Element target) {
        // if this comes from external namespaces, add the namespace to
        // @extensionBindingPrefixes.
        if( !Const.JAXB_NSURI.equals(decl.getNamespaceURI()) )
            declareExtensionNamespace( target, decl.getNamespaceURI() );
        
        NodeList lst = decl.getChildNodes();
        for( int i=0; i<lst.getLength(); i++ ) {
            Node n = lst.item(i);
            if( n instanceof Element )
                declExtensionNamespace( (Element)n, target );
        }
    }


    /** Attribute name. */
    private static final String EXTENSION_PREFIXES = "extensionBindingPrefixes";
    
    /**
     * Adds the specified namespace URI to the jaxb:extensionBindingPrefixes
     * attribute of the target document.
     */
    private void declareExtensionNamespace( Element target, String nsUri ) {
        // look for the attribute
        Element root = target.getOwnerDocument().getDocumentElement();
        Attr att = root.getAttributeNodeNS(Const.JAXB_NSURI,EXTENSION_PREFIXES);
        if( att==null ) {
            String jaxbPrefix = allocatePrefix(root,Const.JAXB_NSURI);
            // no such attribute. Create one.
            att = target.getOwnerDocument().createAttributeNS(
                Const.JAXB_NSURI,jaxbPrefix+":"+EXTENSION_PREFIXES);
            root.setAttributeNodeNS(att);
        }
        
        String prefix = allocatePrefix(root,nsUri);
        if( att.getValue().indexOf(prefix)==-1 )
            // avoid redeclaring the same namespace twice.
            att.setValue( att.getValue()+" "+prefix);
    }
    
    /**
     * Declares a new prefix on the given element and associates it
     * with the specified namespace URI.
     * <p>
     * Note that this method doesn't use the default namespace
     * even if it can.
     */
    private String allocatePrefix( Element e, String nsUri ) {
        // look for existing namespaces.
        NamedNodeMap atts = e.getAttributes();
        for( int i=0; i<atts.getLength(); i++ ) {
            Attr a = (Attr)atts.item(i);
            if( Const.XMLNS_URI.equals(a.getNamespaceURI()) ) {
                if( a.getName().indexOf(':')==-1 )  continue;
                
                if( a.getValue().equals(nsUri) )
                    return a.getLocalName();    // found one
            }
        }
        
        // none found. allocate new.
        while(true) {
            String prefix = "p"+(int)(Math.random()*1000000)+"_";
            if(e.getAttributeNodeNS(Const.XMLNS_URI,prefix)!=null)
                continue;   // this prefix is already allocated.
            
            e.setAttributeNS(Const.XMLNS_URI,"xmlns:"+prefix,nsUri);
            return prefix;
        }
    }
    
    
    /**
     * Copies location information attached to the "src" node to the "dst" node.
     */
    private void copyLocators( Element src, Element dst ) {
        forest.locatorTable.storeStartLocation(
            dst, forest.locatorTable.getStartLocation(src) );
        forest.locatorTable.storeEndLocation(
            dst, forest.locatorTable.getEndLocation(src) );
        
        // recursively process child elements
        Element[] srcChilds = DOMUtils.getChildElements(src);
        Element[] dstChilds = DOMUtils.getChildElements(dst);
        
        for( int i=0; i<srcChilds.length; i++ )
            copyLocators( srcChilds[i], dstChilds[i] );
    }
    

    private void reportError( Element errorSource, String formattedMsg ) throws SAXException {
        reportError( errorSource, formattedMsg, null );
    }
    
    private void reportError( Element errorSource,
        String formattedMsg, Exception nestedException ) throws SAXException {
        
        SAXParseException e = new SAXParseException( formattedMsg,
            forest.locatorTable.getStartLocation(errorSource),
            nestedException );
        errorHandler.error(e);
    }
}
