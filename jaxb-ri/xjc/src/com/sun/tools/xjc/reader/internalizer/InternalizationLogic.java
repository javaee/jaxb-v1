/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader.internalizer;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;

/**
 * Encapsulates schema-language dependent internalization logic.
 * 
 * {@link Internalizer} and {@link DOMForest} are responsible for
 * doing schema language independent part, and this object is responsible
 * for schema language dependent part.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface InternalizationLogic {
    /**
     * Creates a new instance of XMLFilter that can be used to
     * find references to external schemas.
     * 
     * <p>
     * Schemas that are included/imported need to be a part of
     * {@link DOMForest}, and this filter will be expected to
     * find such references.
     * 
     * <p>
     * Once such a reference is found, the filter is expected to
     * call the parse method of DOMForest.
     * 
     * <p>
     * {@link DOMForest} will register ErrorHandler to the returned
     * object, so any error should be sent to that error handler.
     */
    XMLFilter createExternalReferenceFinder( DOMForest parent );
    
    /**
     * Checks if the specified element is a valid target node
     * to attach a customization.
     * 
     * @param parent
     *      The owner DOMForest object. Probably useful only
     *      to obtain context information, such as error handler.
     * @param bindings
     *      &lt;jaxb:bindings> element or a customization element.
     * @return
     *      true if it's OK, false if not.
     * 
     * @exception SAXException
     *      Errors shall be passed to the error handler, but the error handler
     *      can throw SAXException.
     */
    boolean checkIfValidTargetNode( DOMForest parent, Element bindings, Element target )
        throws SAXException;
    
    /**
     * Prepares an element that actually receives customizations.
     * 
     * <p>
     * For example, in XML Schema, target nodes can be any schema
     * element but it is always the &lt;xsd:appinfo> element that
     * receives customization.
     * 
     * @param target
     *      The target node designated by the customization.
     * @return
     *      Always return non-null valid object
     */
    Element refineTarget( Element target );
}
