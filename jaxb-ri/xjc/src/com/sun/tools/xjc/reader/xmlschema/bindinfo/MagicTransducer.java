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

package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JPrimitiveType;
import com.sun.codemodel.JType;
import com.sun.msv.datatype.xsd.ByteType;
import com.sun.msv.datatype.xsd.DoubleType;
import com.sun.msv.datatype.xsd.FloatType;
import com.sun.msv.datatype.xsd.IntType;
import com.sun.msv.datatype.xsd.LongType;
import com.sun.msv.datatype.xsd.ShortType;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.id.SymbolSpace;
import com.sun.tools.xjc.grammar.xducer.BuiltinDatatypeTransducerFactory;
import com.sun.tools.xjc.grammar.xducer.CastTranducer;
import com.sun.tools.xjc.grammar.xducer.DelayedTransducer;
import com.sun.tools.xjc.grammar.xducer.IdentityTransducer;
import com.sun.tools.xjc.grammar.xducer.Transducer;
import com.sun.tools.xjc.grammar.xducer.TypeAdaptedTransducer;
import com.sun.tools.xjc.reader.Const;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSSimpleType;

/**
 * A transducer that changes its conversion logic depending on
 * the parent of the BIConversion object.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class MagicTransducer extends DelayedTransducer {
    
    private final JType targetType;
    private BIConversion parent;
    
    public MagicTransducer( JType _targetType ) {
        this.targetType = _targetType;
    }
    
    /**
     * Set the parent BIConversion customization.
     * This method should be called immediately after the construction
     * of the object.
     */
    public void setParent( BIConversion conv ) {
        this.parent = conv;
    }
    
    
    protected Transducer create() {
        if( !targetType.isPrimitive() ) {
            JPrimitiveType unboxed = ((JClass)targetType).getPrimitiveType();
            if( unboxed==null )
                return error(); // not a wrapper class
            return TypeAdaptedTransducer.adapt(
                new CastTranducer( (JPrimitiveType)unboxed, createCore() ),
                targetType );
        } else
            return new CastTranducer( (JPrimitiveType)targetType, createCore() );
    }

    public boolean isID() { return false; }
    public SymbolSpace getIDSymbolSpace() { return null; }
    
    protected Transducer createCore() {
        // TODO: is this cast really safe?
        // what does it mean for this cast to fail?
        XSSimpleType owner = findOwner();
        
        AnnotatedGrammar grammar = parent.getBuilder().grammar;
        
        // find the base simple type mapping.
        for( XSSimpleType st=owner; st!=null; st = st.getSimpleBaseType() ) {
            if( !Const.XMLSchemaNSURI.equals(st.getTargetNamespace()) )
                continue;   // user-defined type
                
            String name = st.getName().intern();
            if( name=="float" )
                return BuiltinDatatypeTransducerFactory.get(grammar,FloatType.theInstance);
            if( name=="double" )
                return BuiltinDatatypeTransducerFactory.get(grammar,DoubleType.theInstance);
            if( name=="byte" )
                return BuiltinDatatypeTransducerFactory.get(grammar,ByteType.theInstance);
            if( name=="short" )
                return BuiltinDatatypeTransducerFactory.get(grammar,ShortType.theInstance);
            if( name=="int" )
                return BuiltinDatatypeTransducerFactory.get(grammar,IntType.theInstance);
            if( name=="long" )
                return BuiltinDatatypeTransducerFactory.get(grammar,LongType.theInstance);
        }
        
        // unable to find the correct base type.
        // with this owner type, parse/print methods cannot be omitted.
        return error();
    }
    
    private XSSimpleType findOwner() {
        XSComponent c = parent.getOwner();
        if( c instanceof XSSimpleType )
            return (XSSimpleType)c;
        if( c instanceof XSComplexType )
            return ((XSComplexType)c).getContentType().asSimpleType();
        if( c instanceof XSElementDecl )
            return ((XSElementDecl)c).getType().asSimpleType();
        if( c instanceof XSAttributeDecl )
            return ((XSAttributeDecl)c).getType();
        
        // TODO: error handling?
        return null;
    }
    
    private Transducer error() {
        parent.getBuilder().errorReceiver.error(
            parent.getLocation(),
            Messages.format(ERR_ATTRIBUTE_REQUIRED));
        
        // recover from this error
        return new IdentityTransducer(parent.getBuilder().grammar.codeModel);
    }

    protected static final String ERR_ATTRIBUTE_REQUIRED =
        "MagicTransducer.AttributeRequired";
}
