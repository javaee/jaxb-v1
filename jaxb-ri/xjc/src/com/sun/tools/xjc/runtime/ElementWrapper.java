/*
 * @(#)$Id: ElementWrapper.java,v 1.2 2004-09-16 23:16:35 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.tools.xjc.runtime;

import javax.xml.bind.Element;
import javax.xml.namespace.QName;
import org.xml.sax.SAXException;
import com.sun.xml.bind.JAXBObject;

/**
 * General-purpose element wrapper for marshalling out a type object
 * as a document.
 * 
 * <p>
 * For example, when you have the following schema:
 * <pre><xmp>
 * <xs:complexType name="foo">
 *   <xs:sequence>
 *     <xs:element name="aaa" type="..."/>
 *     <xs:element name="bbb" type="..."/>
 *     <xs:element name="ccc" type="..."/>
 *   </xs:sequence>
 * </xs:complexType>
 * </xmp></pre>
 * <p>
 * Or
 * <pre><xmp>
 * <define name="foo">
 *   <element name="aaa"> ... </element>
 *   <element name="bbb"> ... </element>
 *   <element name="ccc"> ... </element>
 * </define>
 * </xmp></pre>
 * <p>
 * JAXB normally produces a Foo class that cannot be marshalled by itself.
 * However, it is often convenient (for debugging, logging, etc) if you
 * can marshal this Foo fragment.
 * 
 * <p>
 * {@link ElementWrapper} can be used in cases like this.
 * To marshal the above Foo object, you can write:
 * <pre>
 * Foo foo = ...;
 * ElementWrapper e = new ElmentWrapper("tagName",foo);
 * marshaller.marshal(e,System.out);
 * </pre>
 * <p>
 * which produces the following XML:
 * <pre><xmp>
 * <tagName>
 *   <aaa>...</aaa>
 *   <bbb>...</bbb>
 *   <ccc>...</ccc>
 * </tagName>
 * </xmp></pre>
 * 
 * <p>
 * Note that this can be only used with a class generated from the JAXB RI.
 * You cannot set objects such as {@link Integer} or {@link String}.
 * 
 * <p>
 * This is a JAXB RI specific extension.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class ElementWrapper implements Element, JAXBObject, XMLSerializable {
    
    // always non-unll
    private QName tagName;
    private Object body;
    
    /**
     * Creates a new {@link ElementWrapper} object by using the given
     * element name and the type object.
     * 
     * @param tagName
     *      The name of the outer-most element.
     * @param body
     *      The object that can be only marshalled as a fragment.
     */
    public ElementWrapper( QName tagName, Object body ) {
        if(tagName==null)   throw new IllegalArgumentException("tag name is null");
        setBody(body);
        this.tagName = tagName;
    }
    
    

    /**
     * Gets the tag name of this element.
     * 
     * @return What you set via {@link #setTagName(QName)} or the constructor.
     */
    public QName getTagName() {
        return tagName;
    }
    
    /**
     * Sets the tag name of this element.
     * 
     * <p>
     * This name is used as the element name when you marshal this object.
     * 
     */
    public void setTagName(QName tagName) {
        if(tagName==null)   throw new IllegalArgumentException("tag name is null");
        this.tagName = tagName;
    }
    
    /**
     * Gets the body of this element.
     * 
     * @return What you set via {@link #setBody(XMLSerializable)} or the constructor.
     */
    public Object getBody() {
        return body;
    }
    
    /**
     * Sets the body of this element.
     * 
     * @param body
     *      must be non-null.
     */
    public void setBody(Object body) {
        if(body==null)      throw new IllegalArgumentException("body is null");
        if(!(body instanceof JAXBObject))
            throw new IllegalArgumentException(body.getClass().getName()+" is not a JAXB-generated class");
        this.body = body;
    }
    
    public void serializeBody(XMLSerializer target) throws SAXException {
        target.startElement(tagName.getNamespaceURI(),tagName.getLocalPart());
        target.childAsURIs((JAXBObject)body,"body");
        target.endNamespaceDecls();
        target.childAsAttributes((JAXBObject)body,"body");
        target.endAttributes();
        target.childAsBody((JAXBObject)body,"body");
        target.endElement();
    }

    public void serializeAttributes(XMLSerializer target) throws SAXException {
        // noop
    }

    public void serializeURIs(XMLSerializer target) throws SAXException {
        // noop
    }
}
