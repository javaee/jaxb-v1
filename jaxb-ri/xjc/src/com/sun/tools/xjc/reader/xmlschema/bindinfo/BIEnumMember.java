/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
