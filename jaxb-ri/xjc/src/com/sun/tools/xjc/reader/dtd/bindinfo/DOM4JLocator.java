/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
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
