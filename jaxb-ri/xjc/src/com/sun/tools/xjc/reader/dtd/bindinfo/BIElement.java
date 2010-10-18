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

package com.sun.tools.xjc.reader.dtd.bindinfo;

import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.dom4j.Element;
import org.xml.sax.Locator;

import com.sun.codemodel.JDefinedClass;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.reader.annotator.AnnotatorController;


/**
 * &lt;element> declaration in the binding file.
 */
public final class BIElement
{
    /**
     * Wraps a given &lt;element> element in the binding file.
     * 
     * <p>
     * Should be created only from {@link BindInfo}.
     */
    BIElement( BindInfo bi, Element _e ) {
        this.parent = bi;
        this.e = _e;
        
        {
            Element c = e.element("content");
            if(c!=null) {
                if(c.attribute("property")!=null) {
                    // if @property is there, this is a general declaration
                    this.rest = BIContent.create(c,this);
                } else {
                    // this must be a model-based declaration
                    Iterator itr = c.elementIterator();
                    while(itr.hasNext()) {
                        Element p = (Element)itr.next();
                        if(p.getName().equals("rest"))
                            this.rest = BIContent.create(p,this);
                        else
                            this.contents.add(BIContent.create(p,this));
                    }
                }
            }
        }
        
        // parse <attribute>s
        Iterator itr = e.elementIterator("attribute");
        while(itr.hasNext()) {
            BIAttribute a = new BIAttribute( this, (Element)itr.next() );
            attributes.put(a.name(),a);
        }
        
        if(isClass()) {
            // if this is a class-declaration, create JClass object now
            String className = e.attributeValue("class");
            if(className==null)
                // none was specified. infer the name.
                className = parent.nameConverter.toClassName(name());
            clazz = parent.classFactory.createInterface(
                parent.getTargetPackage(),
                className,
                null ); // TODO: source location support
        } else {
            // this is not an element-class declaration
            clazz = null;
        }
        
        // process conversion declarations
        itr = e.elementIterator("conversion");
        while( itr.hasNext() ) {
            BIConversion c = new BIUserConversion(bi,(Element)itr.next());
            conversions.put(c.name(),c);
        }
        itr = e.elementIterator("enumeration");
        while( itr.hasNext() ) {
            BIConversion c = BIEnumeration.create( (Element)itr.next(), this );
            conversions.put(c.name(),c);
        }
        
        // parse <constructor>s
        itr = e.elementIterator("constructor");
        while( itr.hasNext() )
            constructors.add( new BIConstructor( (Element)itr.next() ) );
    }
    
    
    
    
    /** The parent {@link BindInfo} object to which this object belongs. */
    final BindInfo parent;
    
    /** &lt;element> element which this object is wrapping. */
    private final Element e;
    
    /**
     * Content-property declarations.
     * <p>
     * This vector will be empty if no content-property declaration is made.
     */
    private final Vector contents = new Vector();
    
    /** Conversion declarations. */
    private final Map conversions = new java.util.HashMap();
    
    /**
     * The "rest" content-property declaration.
     * <p>
     * This field is null when there was no "rest" declaration.
     */
    private BIContent rest;
    
    /** Attribute-property declarations. */
    private final Map attributes = new java.util.HashMap();
    
    /** Constructor declarations. */
    private final Vector constructors = new Vector();
    
    /**
     * the class which is generated by this declaration.
     * This field will be null if this declaration is an element-property
     * declaration.
     */
    private final JDefinedClass clazz;
    
    
    
    
    
    /** Gets the element name. */
    public String name() { return e.attributeValue("name"); }
    
    /**
     * Checks if the element type is "class".
     * If false, that means this element will be a value.
     */
    public boolean isClass() {
        return "class".equals(e.attributeValue("type"));
    }
    
    /**
     * Checks if this element is designated as a root element.
     */
    public boolean isRoot() {
        return "true".equals(e.attributeValue("root"));
    }
    
    /**
     * Gets the JClass object that represents this declaration.
     * 
     * <p>
     * This method returns null if this declaration
     * is an element-property declaration.
     */
    public JDefinedClass getClassObject() {
        return clazz;
    }
    
    /**
     * Creates constructor declarations for this element.
     * 
     * <p>
     * This method should only be called by DTDReader <b>after</b>
     * the normalization has completed.
     * 
     * @param   src
     *      The ClassItem object that corresponds to this declaration
     */
    public void declareConstructors( ClassItem src, AnnotatorController controller ) {
        for( int i=0; i<constructors.size(); i++ )
            ((BIConstructor)constructors.get(i)).createDeclaration(src,controller);
    }
    
    /**
     * Gets the conversion method for this element.
     * 
     * <p>
     * This method can be called only when this element
     * declaration is designated as element-value.
     * 
     * @return
     *        If the convert attribute is not specified, this
     *        method returns null.
     */
    public BIConversion getConversion() {
          String cnv = e.attributeValue("convert");
          if(cnv==null)        return null;
          
          return conversion(cnv);
    }

    /**
     * Resolves the conversion name to the conversion declaration.
     * 
     * <p>
     * Element-local declarations are checked first.
     * 
     * @return
     *        A non-null valid BIConversion object.
     */
    public BIConversion conversion( String name ) {
        BIConversion r = (BIConversion)conversions.get(name);
        if(r!=null)     return r;
        
        // check the global conversion declarations
        return parent.conversion(name);
    }
    
    
    /**
     * Iterates all content-property declarations (except 'rest').
     */
    public Iterator iterateContents() { return contents.iterator(); }
    
    /**
     * Gets the attribute-property declaration, if any.
     * 
     * @return
     *      null if attribute declaration was not given by that name.
     */
    public BIAttribute attribute( String name ) {
        return (BIAttribute)attributes.get(name);
    }
    
    /**
     * Gets the 'rest' content-property declaration, if any.
     * @return
     *      if there is no 'rest' declaration, return null.
     */
    public BIContent getRest() { return this.rest; }

    /** Gets the location where this declaration is declared. */
    public Locator getSourceLocation() {
        return DOM4JLocator.getLocationInfo(e);
    }
}
