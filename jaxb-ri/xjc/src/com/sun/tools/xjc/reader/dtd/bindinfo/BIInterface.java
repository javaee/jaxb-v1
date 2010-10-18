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

import java.util.StringTokenizer;

import org.dom4j.Element;
import org.xml.sax.Locator;

import com.sun.xml.bind.JAXBAssertionError;

/**
 * &lt;interface> declaration in the binding file.
 */
public final class BIInterface
{
    BIInterface( Element e ) {
        this.dom = e;
        name = e.attributeValue("name");
        members = parseTokens(e.attributeValue("members"));
        
        if(e.attribute("properties")!=null) {
            fields = parseTokens(e.attributeValue("properties"));
            throw new JAXBAssertionError("//interface/@properties is not supported");
        } else    // no property was specified
            fields = new String[0];
    }
    
    /** &lt;interface> element in the binding file. */
    private final Element dom;
    
    /** Name of the generated Java interface. */
    private final String name;
    
    /**
     * Gets the name of this interface.
     * This name should also used as the class name.
     */
    public String name() { return name; }
    
    
    private final String[] members;
    
    /**
     * Gets the names of interfaces/classes that implement
     * this interface.
     */
    public String[] members() { return members; }
    
    
    private final String[] fields;
    
    /** Gets the names of fields in this interface. */
    public String[] fields() { return fields; }
    
    
    /** Gets the location where this declaration is declared. */
    public Locator getSourceLocation() {
        return DOM4JLocator.getLocationInfo(dom);
    }
    
    
    
    /** splits a list into an array of strings. */
    private static String[] parseTokens( String value ) {
        StringTokenizer tokens = new StringTokenizer(value);
        
        String[] r = new String[tokens.countTokens()];
        int i=0;
        while(tokens.hasMoreTokens())
            r[i++] = tokens.nextToken();
        
        return r;
    }
}
