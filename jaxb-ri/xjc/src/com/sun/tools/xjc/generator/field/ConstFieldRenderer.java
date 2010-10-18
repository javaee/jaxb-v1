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

package com.sun.tools.xjc.generator.field;

import java.util.ArrayList;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPrimitiveType;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.msv.grammar.ListExp;
import com.sun.msv.grammar.OtherExp;
import com.sun.msv.grammar.ValueExp;
import com.sun.msv.grammar.util.ExpressionWalker;
import com.sun.tools.xjc.generator.ClassContext;
import com.sun.tools.xjc.generator.marshaller.FieldMarshallerGenerator;
import com.sun.tools.xjc.generator.util.BlockReference;
import com.sun.tools.xjc.grammar.FieldItem;
import com.sun.tools.xjc.grammar.FieldUse;
import com.sun.tools.xjc.grammar.PrimitiveItem;
import com.sun.tools.xjc.grammar.xducer.TypeAdaptedTransducer;

/**
 * Realizes a property as a "public static final" property on the interface.
 * This class can handle both boxed/unboxed types and both
 * single/colllection.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public final class ConstFieldRenderer implements FieldRenderer {
    
    private final JCodeModel codeModel;

    /**
     * Set to true if this renderer needs to generate a
     * collection property.
     * 
     * Note that the count property doesn't imply this.
     * There could be a length-1 array.
     */
    private boolean isCollection = false;
    
    private final FieldUse use;
    
    /** Generated constant property on the interface. */
    private JFieldVar $ref;
    
    /**
     * Number of items in this property.
     * If it's 1, then this is a single property.
     */
    private int count;
    
    /**
     * FieldRendererFactory implementation that returns
     * ConstFieldRenderer.
     */
    public final static FieldRendererFactory theFactory =
        new FieldRendererFactory() {
            public FieldRenderer create(ClassContext context, FieldUse fu) {
                return new ConstFieldRenderer(context,fu);
            }
        };
    
    public ConstFieldRenderer( ClassContext context, FieldUse _use ) {
        this.use = _use;
        this.codeModel = use.codeModel;
        
        JExpression initializer = calcInitializer();
        
        $ref = context.ref.field(JMod.PUBLIC|JMod.STATIC|JMod.FINAL,
            isCollection ? getType().array() : getType(),
            use.name, initializer );
        $ref.javadoc().appendComment(use.getJavadoc());
    }
    
    public void generate() {}
    
    public JBlock getOnSetEventHandler() {
        // since this is a constant field, we will never fire this event.
        // just return a dummy block.
        return JBlock.dummyInstance;
    }
    
    
    public void toArray( JBlock block, JExpression $array ) {
        if(isCollection) {
            block.add(
                codeModel.ref(System.class).staticInvoke("arraycopy")
                    .arg($ref).arg(JExpr.lit(0)).arg($array).arg(JExpr.lit(0))
                    .arg($ref.ref("length")));
        } else {
            block.assign( $array.component(JExpr.lit(0)), $ref );
        }
    }
    public void unsetValues( JBlock body ) {
        ;   // can't unset values
    }
    public JExpression hasSetValue() {
        return null;    // can't generate the isSet/unset methods
    }
    public JExpression getValue() {
        return $ref;
    }
    public JClass getValueType() {
        if( isCollection )      return getType().array();
        if( getType().isPrimitive() )   return ((JPrimitiveType)getType()).getWrapperClass();
        return (JClass)getType();
    }

    
    
    /** Computes the type of the property. Handles unboxing. */
    private JType getType() {
        return use.type;
    }

    
    
    public FieldUse getFieldUse() {
        return use;
    }

    public void setter(JBlock body, JExpression newValue) {
        ; // can't override any value
    }

    public JExpression ifCountEqual( int i ) {
        if(i==count)    return JExpr.TRUE;
        else            return JExpr.FALSE;
    }
    public JExpression ifCountGte( int i ) {
        if(i<=count)    return JExpr.TRUE;
        else            return JExpr.FALSE;
    }
    public JExpression ifCountLte( int i ) {
        if(i>=count)    return JExpr.TRUE;
        else            return JExpr.FALSE;
    }
    public JExpression count() {
        return JExpr.lit(count);
    }
    
    /**
     * Computes the initializer expression for the constant field.
     */
    private JExpression calcInitializer() {
        FieldItem[] items = use.getItems();
        final ArrayList result = new ArrayList();
        
        // assert(items.length==1) since only attributes can become constant properties.
        
        items[0].exp.visit(new ExpressionWalker() {
            public void onList(ListExp exp) {
                // use the existence of <list> as a flag to
                // generate a collection property.
                
                // REVISIT: but this is fragile. an expression like
                // <list><value/></list> might be optimized away
                // to <value/>.
                isCollection = true;
                exp.exp.visit(this);
            }
            public void onOther(OtherExp exp) {
                if(exp instanceof PrimitiveItem) {
                    PrimitiveItem pi = (PrimitiveItem)exp;
                    JExpression init = 
                    TypeAdaptedTransducer.adapt(
                        pi.xducer, ConstFieldRenderer.this
                            ).generateConstant((ValueExp)pi.exp);
                    result.add(init);
                }
            }
        });
        
        count = result.size();
        
        if(!isCollection)
            // single property
            return (JExpression)result.get(0);
        else {
            // collection property
            JInvocation inv = JExpr._new( getType().array() );
            for( int i=0; i<result.size(); i++ )
                inv.arg( (JExpression)result.get(i) );
            return inv;
        }
    }



    /**
     * FieldMarshallerGenerator implementation that will be used
     * if this constant is a single property.
     */
    private final class SingleFMGImpl implements FieldMarshallerGenerator {
        public JExpression hasMore() {
            // hasMore() is true (since this is applicable only when
            // multiplicity is (1,1)
            return JExpr.TRUE;
        }
        public JExpression peek(boolean increment) {
            return $ref;
        }
        public void increment(BlockReference block) {}
        public FieldMarshallerGenerator clone( JBlock block, String uniqueId ) {
            // since this iterator has only one item,
            // there is no need to clone it.
            return this;
        }
        public FieldRenderer owner() { return ConstFieldRenderer.this; }
    }
    
    /**
     * FieldMarshallerGenerator implementation that will be used
     * if this constant is a collection property.
     */
    private class CollectionFMGImpl implements FieldMarshallerGenerator {
        CollectionFMGImpl( JVar _$idx ) {
            $idx=_$idx;
        }
        
        private final JVar $idx;
        
        public JExpression hasMore() {
            // [RESULT] idx!=len (or idx<len)
            return $idx.ne($ref.ref("length"));
        }
        public JExpression peek(boolean increment) {
            // [RESULT] <var>[idx++];
            return $ref.component(increment?$idx.incr():$idx);
        }
        public void increment(BlockReference block) {
            block.get(true).assignPlus($idx,JExpr.lit(1));
        }
        public FieldMarshallerGenerator clone( JBlock block, String uniqueId ) {
            // [RESULT] int idx<newid> = idx<id>;
            JVar $newidx = block.decl(
                codeModel.INT, "idx"+uniqueId, $idx );
            return new CollectionFMGImpl($newidx);
            // we can reuse the same length parameter
        }
        public FieldRenderer owner() { return ConstFieldRenderer.this; }
    }
    
    
    
    public FieldMarshallerGenerator createMarshaller(JBlock block, String uniqueId) {
        if(!isCollection)
            return new SingleFMGImpl();
        else {
            // [RESULT] int idx<id> = 0;
            JVar $idx = block.decl(
                codeModel.INT, "idx"+uniqueId, JExpr.lit(0) );
            return new CollectionFMGImpl($idx);
        }
    }
}
