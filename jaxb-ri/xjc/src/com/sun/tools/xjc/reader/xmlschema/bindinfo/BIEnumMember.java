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

import javax.xml.namespace.QName;

import org.xml.sax.Locator;

import com.sun.tools.xjc.grammar.xducer.EnumerationXducer;
import com.sun.tools.xjc.reader.Const;

/**
 * Enumeration member customization.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class BIEnumMember extends AbstractDeclarationImpl {
    
    public BIEnumMember( Locator loc, String _memberName, String _javadoc ) {
        super(loc);
        this.memberName = _memberName;
        this.javadoc = _javadoc;
    }
    
    private final String memberName;
    /** Gets the specified class name, or null if not specified. */
    public String getMemberName() {
        if( memberName==null)   return null;
        
        BIGlobalBinding gb = getBuilder().getGlobalBinding();
        
        if( gb.isJavaNamingConventionEnabled() )
            // apply XML->Java conversion
            return gb.getNameConverter().toConstantName(memberName);
        else
            return memberName;    // ... or don't change the value
    }
    
    private final String javadoc;
    /**
     * Gets the javadoc comment specified in the customization.
     * Can be null if none is specified.
     */
    public String getJavadoc() { return javadoc; }
    
    /**
     * Creates the equivalent {@link EnumerationXducer.MemberInfo} object.
     */
    public EnumerationXducer.MemberInfo createMemberInfo() {
        return new EnumerationXducer.MemberInfo(getMemberName(),javadoc);
    }
    
    public QName getName() { return NAME; }

    /** Name of this declaration. */
    public static final QName NAME = new QName(
        Const.JAXB_NSURI, "typesafeEnumMember" );
}
