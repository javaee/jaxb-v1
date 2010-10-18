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

import org.dom4j.Element;
import org.dom4j.QName;
import org.xml.sax.Locator;

class DOM4JLocator
{
    private static final String locationNamespace =
        "http://www.sun.com/xmlns/jaxb/dom4j-location";
    private static final String systemId    = "systemid";
    private static final String column      = "column";
    private static final String line        = "line";
    
    /** Sets the location information to a specified element. */
    public static void setLocationInfo( Element e, Locator loc ) {
        e.addAttribute(
            QName.get(systemId,locationNamespace),
            loc.getSystemId());
        e.addAttribute(
            QName.get(column,  locationNamespace),
            Integer.toString(loc.getLineNumber()));
        e.addAttribute(
            QName.get(line,    locationNamespace),
            Integer.toString(loc.getColumnNumber()));
    }
    
    /**
     * Gets the location information from an element.
     * 
     * <p>
     * For this method to work, the setLocationInfo method has to be
     * called before.
     */
    public static Locator getLocationInfo( final Element e ) {
        if(e.attribute(QName.get(systemId,locationNamespace))==null)
            return null;    // no location information
        
        return new Locator(){
            public int getLineNumber() {
                return Integer.parseInt(e.attributeValue(
                    QName.get(line,locationNamespace)));
            }
            public int getColumnNumber() {
                return Integer.parseInt(e.attributeValue(
                    QName.get(column,locationNamespace)));
            }
            public String getSystemId() {
                return e.attributeValue(
                    QName.get(systemId,locationNamespace));
            }
            // we are not interested in PUBLIC ID.
            public String getPublicId() { return null; }
        };
    }
}
