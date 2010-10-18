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

package com.sun.tools.xjc.reader.relaxng;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.reader.Const;
import com.sun.tools.xjc.reader.internalizer.DOMForest;
import com.sun.tools.xjc.util.DOMUtils;

/**
 * Converts JAXB standard element-based customization to the
 * attribute-based ones which {@link TRELAXNGReader} understands.
 * 
 * <p>
 * This is a kind of quick hack to make the ends meet between
 * TRELAXNGReader and JAXB customization.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class CustomizationConverter {
    
    private final Options options;

    /**
     * @param opts
     *      the customization may update certain options.
     */
    public CustomizationConverter( Options opts ) {
        this.options = opts;
    }
    
    public void fixup( DOMForest forest ) {
        Document[] docs = forest.listDocuments();
        for( int i=0; i<docs.length; i++ )
            fixup( docs[i].getDocumentElement() );
    }
    
    private void fixup( Element e ) {
        if( !e.getNamespaceURI().equals(Const.RELAXNG_URI) )
            return;     // no need for fixing its sub-tree.
        
        // list up child elements
        NodeList nl = e.getChildNodes();
        for( int i=0; i<nl.getLength(); i++ ) {
            Node c = nl.item(i);
            if(!(c instanceof Element))     continue;   // we don't care
            Element child = (Element)c;
            String childNS = child.getNamespaceURI();
            
            if( childNS.equals(Const.RELAXNG_URI) )
                fixup(child);       // recursively fix all the RELAX NG nodes.
            else
            if( childNS.equals(Const.JAXB_NSURI)
            ||  childNS.equals(Const.XJC_EXTENSION_URI))
                fixup(e,child);
            
            // otherwise we'll just leave this node as is.
        }
    }
    
    /**
     * @param parent
     *      A RELAX NG element to apply a customization
     * @param child
     *      A JAXB customization element.
     */
    private void fixup( Element parent, Element child ) {
        String name = child.getLocalName().intern();
        
        if( name=="schemaBindings" ) {
            Element p = DOMUtils.getFirstChildElement(child,Const.JAXB_NSURI,"package");
            if(p==null)     return;
//            copyAttribute(parent,p,"name");
            parent.setAttributeNS(Const.JAXB_NSURI,"package",p.getAttribute("name"));
            return;
        }
        
        if( name=="class" ) {
            parent.setAttributeNS(Const.JAXB_NSURI,"role", "class" );
            copyAttribute(parent,child,"name");
            return;
        }
        
        if( name=="property" ) {
            parent.setAttributeNS(Const.JAXB_NSURI,"role", "field" );
            copyAttribute(parent,child,"name");
            return;
        }
        
        if( name=="javaType" ) {
            parent.setAttributeNS(Const.JAXB_NSURI,"role", "primitive" );
            copyAttribute(parent,child,"name");
            copyAttribute(parent,child,"parseMethod");
            copyAttribute(parent,child,"printMethod");
            copyAttribute(parent,child,"hasNsContext");
            return;
        }
        
        if( name=="interface") {
            parent.setAttributeNS(Const.JAXB_NSURI,"role", "interface" );
            copyAttribute(parent,child,"name");
            return;
        }
        
        if( name=="ignore" ) {
            parent.setAttributeNS(Const.JAXB_NSURI,"role", "ignore" );
            return;
        }
        
        if( name=="super" ) {
            parent.setAttributeNS(Const.JAXB_NSURI,"role", "superClass" );
            return;
        }
        
        if( name=="dom" ) {
            parent.setAttributeNS(Const.JAXB_NSURI,"role", "dom" );
            copyAttribute(parent,child,"type");
            return;
        }
        
        if( name=="noMarshaller" ) {
            options.generateMarshallingCode = false;
            return;
        }
        
        if( name=="noUnmarshaller" ) {
            options.generateUnmarshallingCode = false;
            options.generateValidatingUnmarshallingCode = false;
            return;
        }
        
        if( name=="noValidator" ) {
            options.generateValidationCode = false;
            return;
        }
        
        if( name=="noValidatingUnmarshaller" ) {
            options.generateValidatingUnmarshallingCode = false;
            return;
        }
    }
    
    private static void copyAttribute( Element dst, Element src, String attName ) {
        if( src.getAttributeNode(attName)!=null )
            dst.setAttributeNS(Const.JAXB_NSURI,attName, src.getAttribute(attName) );
    }
}
