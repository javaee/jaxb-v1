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

import java.util.Map;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;

import com.sun.codemodel.JType;
import com.sun.tools.xjc.generator.util.WhitespaceNormalizer;
import com.sun.tools.xjc.grammar.xducer.IdentityTransducer;
import com.sun.tools.xjc.grammar.xducer.Transducer;
import com.sun.tools.xjc.grammar.xducer.UserTransducer;
import com.sun.tools.xjc.grammar.xducer.WhitespaceTransducer;

/**
 * &lt;conversion> declaration in the binding file.
 * This declaration declares a conversion by user-specified methods.
 */
public class BIUserConversion implements BIConversion
{
    /**
     * Wraps a given &lt;conversion> element in the binding file.
     */
    BIUserConversion( BindInfo bi, Element _e ) {
        this.owner = bi;
        this.e = _e;
    }
    
    private static void add( Map m, BIConversion c ) {
        m.put( c.name(), c );
    }
    
    /** Adds all built-in conversions into the given map. */
    static void addBuiltinConversions( BindInfo bi, Map m ) {
        DocumentFactory f = DocumentFactory.getInstance();
        
        add( m, new BIUserConversion( bi,
            f.createElement("conversion")
                .addAttribute("name","boolean")
                .addAttribute("type","java.lang.Boolean")
                .addAttribute("parse","getBoolean") ));
        
        add( m, new BIUserConversion( bi,
            f.createElement("conversion")
                .addAttribute("name","byte")
                .addAttribute("type","java.lang.Byte")
                .addAttribute("parse","parseByte") ));
        
        add( m, new BIUserConversion( bi,
            f.createElement("conversion")
                .addAttribute("name","short")
                .addAttribute("type","java.lang.Short")
                .addAttribute("parse","parseShort") ));
        
        add( m, new BIUserConversion( bi,
            f.createElement("conversion")
                .addAttribute("name","int")
                .addAttribute("type","java.lang.Integer")
                .addAttribute("parse","parseInt") ));
        
        add( m, new BIUserConversion( bi,
            f.createElement("conversion")
                .addAttribute("name","long")
                .addAttribute("type","java.lang.Long")
                .addAttribute("parse","parseLong") ));
        
        add( m, new BIUserConversion( bi,
            f.createElement("conversion")
                .addAttribute("name","float")
                .addAttribute("type","java.lang.Float")
                .addAttribute("parse","parseFloat") ));
        
        add( m, new BIUserConversion( bi,
            f.createElement("conversion")
                .addAttribute("name","double")
                .addAttribute("type","java.lang.Double")
                .addAttribute("parse","parseDouble") ));
    }
    
    
    /** The owner {@link BindInfo} object to which this object belongs. */
    private final BindInfo owner;
    
    /** &lt;conversion> element which this object is wrapping. */
    private final Element e;



    /** Gets the location where this declaration is declared. */
    public Locator getSourceLocation() {
        return DOM4JLocator.getLocationInfo(e);
    }
    
    /**
     * Gets the attribute value from the element.
     * 
     * @param name
     *        Name of the attribute.
     * @param defaultValue
     *        When the attribute is not present, this value is returned.
     */
    private String attValue( String name, String defaultValue ) {
        String r = e.attributeValue(name);
        if(r==null)        return defaultValue;
        else            return r;
    }
    
    /** Gets the conversion name. */
    public String name() { return e.attributeValue("name"); }
    
    /** Gets a transducer for this conversion. */
    public Transducer getTransducer() {
        
        String ws = e.attributeValue("whitespace");
        if(ws==null)    ws = "collapse";
        
        String type = e.attributeValue("type");
        JType t;
        if(type==null) {
            // defaults to the <name> class in the current package.
            t = owner.getTargetPackage().ref(name());
        } else {
            int idx = type.lastIndexOf('.');
            if(idx<0) {
                // no package name is specified.
                try {
                    t = JType.parse(owner.codeModel,type);  // try to parse it as a primitive type
                } catch( IllegalArgumentException e ) {
                    // otherwise treat it as a class name in the current package
                    t = owner.getTargetPackage().ref(type);
                }
            } else {
                try {
                    t = owner.codeModel.ref(type);
                } catch( ClassNotFoundException e ) {
                    // TODO: better error handling
                    throw new NoClassDefFoundError(e.getMessage());
                }
            }
        }
        
        try {
            return WhitespaceTransducer.create( new UserTransducer(
                t,
                attValue("parse","new"),
                attValue("print","toString"),
                false ),
                
                owner.codeModel,
                WhitespaceNormalizer.parse(ws) );
        } catch( IllegalArgumentException e ) {
            // if the type is a primitive type and print/parse methods are incorrect
            owner.errorReceiver.error( new SAXParseException(
                e.getMessage(),getSourceLocation(),e) );
            // recover from this error
            return new IdentityTransducer( owner.codeModel );
        }
    }
}
