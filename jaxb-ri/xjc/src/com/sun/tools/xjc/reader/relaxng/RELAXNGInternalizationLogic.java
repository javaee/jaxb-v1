/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader.relaxng;

import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;

import com.sun.tools.xjc.reader.Const;
import com.sun.tools.xjc.reader.internalizer.AbstractReferenceFinderImpl;
import com.sun.tools.xjc.reader.internalizer.DOMForest;
import com.sun.tools.xjc.reader.internalizer.InternalizationLogic;

/**
 * RELAX NG specific internalization logic.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class RELAXNGInternalizationLogic implements InternalizationLogic {
    
    /**
     * This filter looks for &lt;xs:import> and &lt;xs:include>
     * and parses those documents referenced by them.
     */
    private class ReferenceFinder extends AbstractReferenceFinderImpl {
        ReferenceFinder( DOMForest parent ) {
            super(parent);
        }
        
        protected String findExternalResource( String nsURI, String localName, Attributes atts) {
            if( Const.RELAXNG_URI.equals(nsURI)
            && ("include".equals(localName) || "externalRef".equals(localName) ) )
                return atts.getValue("href");
            else
                return null;
        }
    };

    public XMLFilter createExternalReferenceFinder(DOMForest parent) {
        return new ReferenceFinder(parent);
    }

    public boolean checkIfValidTargetNode(DOMForest parent, Element bindings, Element target) throws SAXException {
        return Const.RELAXNG_URI.equals(target.getNamespaceURI());
    }

    public Element refineTarget(Element target) {
        // no refinement necessary
        return target;
    }

}
