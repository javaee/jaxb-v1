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

import java.util.ArrayList;

import org.dom4j.Element;

import com.sun.tools.xjc.generator.field.ArrayFieldRenderer;
import com.sun.tools.xjc.generator.field.FieldRendererFactory;
import com.sun.tools.xjc.generator.field.UntypedListFieldRenderer;

/** &lt;attribute> declaration in the binding file. */
public class BIAttribute
{
    /**
     * Wraps a given &lt;attribute> element.
     * <p>
     * Should be created only from {@link BIElement}.
     */
    BIAttribute( BIElement _parent, Element _e ) {
        this.parent = _parent;
        this.element = _e;
    }
    
    private final BIElement parent;
    private final Element element;
    
    /** Gets the name of this attribute-property declaration. */
    public final String name() {
        return element.attributeValue("name");
    }
    
    
    /**
     * Gets the conversion method for this attribute, if any.
     * 
     * @return
     *        If the convert attribute is not specified, this
     *        method returns null.
     */
    public BIConversion getConversion() {
          String cnv = element.attributeValue("convert");
          if(cnv==null)        return null;
          
          return parent.conversion(cnv);
    }

    /**
     * Gets the realization of this particle, if any.
     * 
     * @return
     *      null if the "collection" attribute was not specified.
     */
    public final FieldRendererFactory getRealization() {
        String v = element.attributeValue("collection");
        if(v==null)     return null;
        
        v = v.trim();
        if(v.equals("array"))   return ArrayFieldRenderer.theFactory;
        if(v.equals("list"))
            return new UntypedListFieldRenderer.Factory(
                parent.parent.codeModel.ref(ArrayList.class));
        
        // the correctness of the attribute value must be 
        // checked by the validator.
        throw new InternalError("unexpected collection value: "+v);
    }
    
    /**
     * Gets the property name for this attribute.
     * 
     * @return
     *      always a non-null, valid string.
     */
    public final String getPropertyName() {
        String r = element.attributeValue("property");
        if(r!=null)     return r;
        else            return name();
    }
}
