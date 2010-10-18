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

package com.sun.tools.xjc.reader.xmlschema;

import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.relaxng.datatype.DatatypeException;
import org.xml.sax.Locator;

import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JJavaName;
import com.sun.codemodel.util.JavadocEscapeWriter;
import com.sun.msv.datatype.xsd.DatatypeFactory;
import com.sun.msv.datatype.xsd.XSDatatype;
import com.sun.msv.grammar.Expression;
import com.sun.tools.xjc.grammar.xducer.BuiltinDatatypeTransducerFactory;
import com.sun.tools.xjc.grammar.xducer.EnumerationXducer;
import com.sun.tools.xjc.grammar.xducer.IdentityTransducer;
import com.sun.tools.xjc.grammar.xducer.Transducer;
import com.sun.tools.xjc.reader.*;
import com.sun.tools.xjc.reader.NameConverter;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIConversion;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIEnum;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIEnumMember;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BindInfo;
import com.sun.xml.bind.JAXBAssertionError;
import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSListSimpleType;
import com.sun.xml.xsom.XSRestrictionSimpleType;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSUnionSimpleType;
import com.sun.xml.xsom.impl.util.SchemaWriter;
import com.sun.xml.xsom.visitor.XSSimpleTypeFunction;
import com.sun.xml.xsom.visitor.XSVisitor;

/**
 * Finds {@link Transdcuer} object that is attached to the nearest
 * ancestor datatype. 
 * 
 * A transducer specified in a type is inherited by
 * types derived from that type, unless overwritten.
 * 
 * <p>
 * JAXB spec defines the default transducers that will be applied,
 * and users can also change them by applying customizations.
 * This method takes care of those details.
 * 
 * <p>
 * Note that since one transducer can apply to
 * many datatypes with different whitespace normalization requirement,
 * it is the caller's responsiblility to perform the correct whitespace
 * normalization to the transducer returned from this method.
 * 
 * <p>
 * If none is found, which can only happen to unions and lists,
 * null will be returned.
 * 
 * <p>
 * Since type-safe enums are handled as conversions, this class
 * also builds type-safe enum classes if necesasry.
 * 
 * <p>
 * This is a function object that works on {@link XSSimpleType}
 * and returns {@link Transducer}.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
final class ConversionFinder {
    
    
    /** read-only empty HashMap. */
    private final static HashMap emptyHashMap = new HashMap();
        
    private final BGMBuilder builder;

    /** Transducers for the built-in types. Read-only. */
    private final Map builtinConversions = new Hashtable();
    
    
    ConversionFinder( BGMBuilder _builder ) {
        this.builder = _builder;
        
        // list of datatypes which have built-in conversions.
        // note that although xs:token and xs:normalizedString are not
        // specified in the spec, they need to be here because they
        // have different whitespace normalization semantics.
        String[] names = {
            "anySimpleType", "ID", "IDREF", "boolean", "base64Binary", "hexBinary",
            "float", "decimal", "integer", "long", "unsignedInt", "int",
            "unsignedShort", "short", "unsignedByte", "byte", "double", "QName",
            "token", "normalizedString", "date", "dateTime", "time" };
        
        try {
            for( int i=0; i<names.length; i++ ) {
                builtinConversions.put(
                    names[i],
                    BuiltinDatatypeTransducerFactory.getWithoutWhitespaceNormalization(
                        builder.grammar,
                        DatatypeFactory.getTypeByName(names[i])));
            }
        } catch( DatatypeException e ) {
            e.printStackTrace();
            throw new JAXBAssertionError();
        }
        
        // TODO: handling dateTime, time, and date type
        // TODO: conversion of numeric types (such as short) can be
        // possibly done by using JDK built-in classes. (such as java.lang.Short)
    }

    
    
    
    /** Public entry point. */
    public Transducer find( XSSimpleType type ) {
        return (Transducer)type.apply(functor);
    }
    
    // functor returns a Transducer
    private final XSSimpleTypeFunction functor = new XSSimpleTypeFunction() {
        public Object listSimpleType(XSListSimpleType type) {
            return lookup(type);
        }
    
        public Object unionSimpleType(XSUnionSimpleType type) {
            return lookup(type);
        }
    
        public Object restrictionSimpleType(XSRestrictionSimpleType type) {
            // if none is found on this type, check the base type.
            Transducer xducer = lookup(type);
            if(xducer!=null)  return xducer;
    
    
            // see if this type should be mapped to a type-safe enumeration by default.
            // if so, built a EnumXDucer from it and return it.
            if( shouldBeMappedToTypeSafeEnumByDefault(type) )
                // if a type is global and with enumeration facets
                return bindToTypeSafeEnum(type,null,null,emptyHashMap,null);
    
            return type.getSimpleBaseType().apply(this);
        }
    };
    
    /**
     * Returns true if a type-safe enum should be created from
     * the given simple type by default.
     */
    private boolean shouldBeMappedToTypeSafeEnumByDefault( XSRestrictionSimpleType type ) {
        
        // if not, there will be a problem wrt the class name of this type safe enum type.
        if( type.isLocal() )    return false;
        
        if( !canBeMappedToTypeSafeEnum(type) )
            // we simply can't map this to an enumeration
            return false;
        
        if( type.getDeclaredFacet(XSFacet.FACET_ENUMERATION)==null )
            // if the type itself doesn't have the enumeration facet,
            // it won't be mapped to a type-safe enum.
            return false;
        
        // return true only when this type is derived from NCName.
        XSSimpleType t = type;
        do {
            if( t.isGlobal() && builder.getGlobalBinding().canBeMappedToTypeSafeEnum(t) )
                return true;
            
            t = t.getSimpleBaseType();
        } while( t!=null );
        
        return false;
    }
    
    
    private static final Set builtinTypeSafeEnumCapableTypes;
    
    static {
        Set s = new HashSet();
        
        // see a bullet of 6.5.1 of the spec.
        String[] typeNames = new String[] {
            "string", "boolean", "float", "decimal", "double", "anyURI"
        };
        
        for( int i=0; i<typeNames.length; i++ )
            s.add(typeNames[i]);
            
        builtinTypeSafeEnumCapableTypes = Collections.unmodifiableSet(s);    
    }
    
    
    /**
     * Returns true if the given simple type can be mapped to a
     * type-safe enum class.
     * 
     * <p>
     * JAXB spec places a restrictrion as to what type can be
     * mapped to a type-safe enum. This method enforces this
     * constraint.
     */
    private boolean canBeMappedToTypeSafeEnum( XSSimpleType type ) {
        do {
            if( Const.XMLSchemaNSURI.equals(type.getTargetNamespace()) ) {
                // type must be derived from one of these types
                String localName = type.getName();
                if( localName!=null ) {
                    if( localName.equals("anySimpleType") )
                        return false;   // catch all case
                    if( localName.equals("ID") || localName.equals("IDREF") )
                        return false;   // not ID/IDREF
                    
                    // other allowed list
                    if( builtinTypeSafeEnumCapableTypes.contains(localName) )
                        return true;
                }
            }
            
            type = type.getSimpleBaseType();
        } while( type!=null );
        
        return false;
    }



    /**
     * Builds a type-safe enum conversion from a simple type
     * with enumeration facets.
     * 
     * @param className
     *      The class name of the type-safe enum. Or null to
     *      create a default name.
     * @param javadoc
     *      Additional javadoc that will be added at the beginning of the
     *      class, or null if none is necessary.
     * @param members
     *      A map from enumeration values (as String) to BIEnumMember objects.
     *      if some of the value names need to be overrided.
     *      Cannot be null, but the map may not contain entries
     *      for all enumeration values.
     * @param loc
     *      The source location where the above customizations are
     *      specified, or null if none is available.
     */
    private Transducer bindToTypeSafeEnum( XSRestrictionSimpleType type,
        String className, String javadoc, HashMap members, Locator loc ) {
        
        if( loc==null )  // use the location of the simple type as the default
            loc = type.getLocator();
        
        if( className==null ) {
            // infer the class name. For this to be possible,
            // the simple type must be a global one.
            if( !type.isGlobal() ) {
                builder.errorReporter.error( loc, Messages.ERR_NO_ENUM_NAME_AVAILABLE );
                // recover by returning a meaningless conversion
                return new IdentityTransducer(builder.grammar.codeModel);
            }
            className = type.getName();
        }
        // we apply name conversion in any case
        className = builder.getNameConverter().toClassName(className);
        

        JDefinedClass clazz = builder.selector.codeModelClassFactory.createClass(
                builder.selector.getPackage(type.getTargetNamespace()),
                className, type.getLocator() );
        
        {// set Javadoc
            StringWriter out = new StringWriter();
            SchemaWriter sw = new SchemaWriter(new JavadocEscapeWriter(out));
            type.visit((XSVisitor)sw);
            
            JDocComment jdoc = clazz.javadoc();
            jdoc.appendComment( javadoc!=null? javadoc+"\n\n" : "" );
            jdoc.appendComment( 
                Messages.format( Messages.JAVADOC_HEADING, type.getName() ) );
            jdoc.appendComment( "\n<p>\n<pre>\n" );
            jdoc.appendComment(out.getBuffer().toString());
            jdoc.appendComment( "</pre>" );
            
        }
        
        boolean needsToGenerateMemberName =
            checkIfMemberNamesNeedToBeGenerated( type, members );
        
        HashMap memberMap = new HashMap();
        int idx=1;
        
        Expression exp = Expression.nullSet;
        {// list enumeration items and build expression
            // build base type
            XSDatatype baseDt = 
                builder.simpleTypeBuilder.datatypeBuilder.build(type.getSimpleBaseType());
            
            Iterator itr = type.iterateDeclaredFacets();
            while(itr.hasNext()) {
                XSFacet facet = (XSFacet)itr.next();
                if(!facet.getName().equals(XSFacet.FACET_ENUMERATION))
                    continue;
                
                Expression vexp = builder.pool.createValue(baseDt,
                    baseDt.createValue(facet.getValue(),facet.getContext()));
                
                if( needsToGenerateMemberName ) {
                    // generate names for all member names.
                    // this will even override names specified by the user
                    memberMap.put( vexp,
                        new EnumerationXducer.MemberInfo("value"+(idx++),null));
                } else {
                    BIEnumMember mem = (BIEnumMember)members.get(facet.getValue());
                    if( mem==null )
                        // look at the one attached to the facet object
                        mem = (BIEnumMember)builder.getBindInfo(facet).get(BIEnumMember.NAME);
                    
                    if( mem!=null )
                        memberMap.put( vexp, mem.createMemberInfo() );
                }
                
                exp = builder.pool.createChoice(exp,vexp);
            }
        }
        
        if( memberMap.isEmpty() ) // if the map is empty, no reason to use a separate instance. share it.
            memberMap = emptyHashMap;
        
        // use the name of the simple type as the name of the class.
        BIConversion conv = new BIConversion( type.getLocator(),
            new EnumerationXducer( NameConverter.standard, clazz, exp, memberMap, loc ));
        conv.markAsAcknowledged();
        
        // attach this new conversion object to this simple type
        // so that successive look up will use the same object.
        builder.getOrCreateBindInfo(type).addDecl(conv);
        
        return conv.getTransducer();
    }
    
         
    /**
     * Checks if a type-safe enum class needs to use generated member names.
     * 
     * @param members
     *      Overrided names.
     */
    private boolean checkIfMemberNamesNeedToBeGenerated( XSRestrictionSimpleType type, HashMap members ) {
        
        Iterator itr = type.iterateDeclaredFacets();
        while(itr.hasNext()) {
            XSFacet facet = (XSFacet)itr.next();
            if(!facet.getName().equals(XSFacet.FACET_ENUMERATION))
                continue;
            
            String value = facet.getValue();
            
            if( members.containsKey(value) )
                continue;   // this name is overrided.
                
            if( !JJavaName.isJavaIdentifier(
                    builder.getNameConverter().toConstantName(facet.getValue()) )) {
                // this enum value isn't overrided by the customization
                // and it will not produce a correct Java identifier.
                //
                // we needs to generate member names if the global bindings says so.
                return builder.getGlobalBinding().needsToGenerateEnumMemberName();
            }
        }
        
        // enumeration values don't have any problem.
        // no need to use generated names.
        return false;
    }
    
    

    /**
     * Looks for the {@link Transducer} that should apply to
     * the given type without considering its base types.
     * 
     * @return null if not found.
     */
    private Transducer lookup( XSSimpleType type ) {
        
        BindInfo info = builder.getBindInfo(type);
        BIConversion conv = (BIConversion)info.get(BIConversion.NAME);

        if( conv!=null ) {
            conv.markAsAcknowledged();
            return conv.getTransducer();    // a conversion was found
        }
        
        // look for enum customization
        BIEnum en = (BIEnum)info.get(BIEnum.NAME);
        if( en!=null ) {
            en.markAsAcknowledged();
            
            // if an enum customization is specified, make sure
            // the type is OK
            if( !canBeMappedToTypeSafeEnum(type) ) {
                builder.errorReporter.error( en.getLocation(),
                    Messages.ERR_CANNOT_BE_TYPE_SAFE_ENUM );
                builder.errorReporter.error( type.getLocator(),
                    Messages.ERR_CANNOT_BE_TYPE_SAFE_ENUM_LOCATION );
                // recover by ignoring this customization
                return null;
            }
            // list and union cannot be mapped to a type-safe enum,
            // so in this stage we can safely cast it to XSRestrictionSimpleType
            return bindToTypeSafeEnum( (XSRestrictionSimpleType)type,
                en.getClassName(), en.getJavadoc(), en.getMembers(), en.getLocation() );
        }
        

        // lastly if the type is built in, look for the default binding
        if(type.getTargetNamespace().equals(Const.XMLSchemaNSURI)) {
            String name = type.getName();
            if(name!=null)
                return lookupBuiltin(name);
        }
        
        return null;
    }
    
    private Transducer lookupBuiltin( String typeName ) {
        return (Transducer)builtinConversions.get(typeName);
    }
}

