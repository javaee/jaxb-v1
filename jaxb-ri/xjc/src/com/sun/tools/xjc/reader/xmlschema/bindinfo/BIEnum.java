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
package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.QName;

import org.xml.sax.Locator;

import com.sun.tools.xjc.reader.Const;

/**
 * Enumeration customization.
 * <p>
 * This customization binds a simple type to a type-safe enum class.
 * The actual binding process takes place in the ConversionFinder.
 * 
 * <p>
 * This customization is acknowledged by the ConversionFinder.
 * 
 * @author
 *  Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public final class BIEnum extends AbstractDeclarationImpl {
    
    public BIEnum( Locator loc, String _className, String _javadoc, HashMap _members ) {
        super(loc);
        this.className = _className;
        this.javadoc = _javadoc;
        this.members = _members;
    }
    
    private final String className;
    /** Gets the specified class name, or null if not specified. */
    public String getClassName() { return className; }
    
    private final String javadoc;
    /**
     * Gets the javadoc comment specified in the customization.
     * Can be null if none is specified.
     */
    public String getJavadoc() { return javadoc; }
    
    private final HashMap members;
    /**
     * Gets the map that contains XML value->BIEnumMember pairs.
     * This table is built from &lt;enumMember> customizations.
     * 
     * @return Always return non-null.
     */
    public HashMap getMembers() { return members; }
    
    public QName getName() { return NAME; }
    
    public void setParent(BindInfo p) {
        super.setParent(p);
        
        Iterator itr = members.entrySet().iterator();
        while(itr.hasNext()) {
            BIEnumMember mem = (BIEnumMember)((Map.Entry)itr.next()).getValue();
            mem.setParent(p);
        }
    }
    
    /** Name of this declaration. */
    public static final QName NAME = new QName(
        Const.JAXB_NSURI, "enum" );
}

