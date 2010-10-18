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

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JOp;
import com.sun.codemodel.JPrimitiveType;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.generator.ClassContext;
import com.sun.tools.xjc.generator.marshaller.FieldMarshallerGenerator;
import com.sun.tools.xjc.generator.util.BlockReference;
import com.sun.tools.xjc.grammar.DefaultValue;
import com.sun.tools.xjc.grammar.FieldUse;
import com.sun.tools.xjc.reader.NameConverter;

/**
 * SingleFieldRenderer with a special boolean flag that indicates
 * whether a property has a set value or not.
 * 
 * <pre>
 * T getXXX();
 * void setXXX( T value );
 * </pre>
 * 
 * This allows the isSet/unset method to be properly created
 * from a primitive type.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class OptionalUnboxedFieldRenderer extends AbstractFieldRendererWithVar {

    /**
     * Boolean flag that remembers if the value exists or not.
     * If this flag is set to false, the value is considered not to exist.
     */
    private JVar $has_flag;
    
    /**
     * Code fragment that gets executed when the set method
     * is called. IOW, this is an event handler of the "onSet" event.
     */
    private JBlock onSetEvent;
    
    
    
    public OptionalUnboxedFieldRenderer( ClassContext context, FieldUse fu ) {
        super(context,fu);
    }
    
    protected JFieldVar generateField() {
        $has_flag = context.implClass.field(JMod.PROTECTED, codeModel.BOOLEAN, "has_"+fu.name);
        return generateField(fu.type);
    }

    public JClass getValueType() {
        return ((JPrimitiveType)fu.type).getWrapperClass();
    }
    
    public JExpression getValue() {
        return ((JPrimitiveType)fu.type).wrap(ref());
    }
    
    public void generateAccessors() {
        JBlock body;
        
        // [RESULT]
        // Type getXXX() {
        // #ifdef default value
        //     if(!has_XXX)
        //         return unbox(defaultValue);
        // #endif
        //     return value;
        // }
        JMethod $get = writer.declareMethod(
            fu.type,
            (fu.type==codeModel.BOOLEAN?"is":"get")+fu.name );
        String javadoc = fu.getJavadoc();
        if(javadoc.length()==0)
            javadoc = Messages.format(
                Messages.DEFAULT_GETTER_JAVADOC,
                NameConverter.standard.toVariableName(fu.name));
        writer.javadoc().appendComment(javadoc);

        DefaultValue[] defaultValues = fu.getDefaultValues();
        if(defaultValues==null) {
            $get.body()._return(ref());
        } else {
            // since this is a single property, it should only have one value.
            _assert( defaultValues.length==1 );
            
            JConditional cond = $get.body()._if($has_flag.not());
            // need to insert a layer that does boxing/unboxing
            cond._then()._return( defaultValues[0].generateConstant() );
            cond._else()._return(ref());
        }
//        JMethod $get = helper.declareMethod( unboxedType, "get" );
//                
//        $get.body()._return(ref());
        
        // [RESULT]
        // void setXXX( Type value ) {
        //     has_XXX = true;
        //     this.value = value;
        //     /*onSetEventHandler*/
        // }
        JMethod $set = writer.declareMethod( codeModel.VOID, "set"+fu.name );
        JVar $value = writer.addParameter( fu.type, "value" );
        body = $set.body();
        body.assign(ref(),$value);
        body.assign($has_flag, JExpr.TRUE);
        onSetEvent = body;
        javadoc = fu.getJavadoc();
        if(javadoc.length()==0)
            javadoc = Messages.format(
                Messages.DEFAULT_SETTER_JAVADOC,
                NameConverter.standard.toVariableName(fu.name));
        writer.javadoc().appendComment(javadoc);
    }

    public void toArray( JBlock block, JExpression $array ) {
        block.assign( $array.component(JExpr.lit(0)), ref() );
    }
    
    
    public void unsetValues( JBlock body ) {
        body.assign( $has_flag, JExpr.FALSE );
    }
    
    public JExpression hasSetValue() {
        return $has_flag;
    }
    
    public JBlock getOnSetEventHandler() {
        return onSetEvent;
    }
    
    
    
    public JExpression ifCountEqual( int i ) {
        switch(i) {
        case 0:     return $has_flag.not();
        case 1:     return $has_flag;
        default:    return JExpr.FALSE;
        }
    }
    public JExpression ifCountGte( int i ) {
        if(i==1)    return $has_flag;
        else        return JExpr.FALSE;
    }
    
    public JExpression ifCountLte( int i ) {
        if(i==0)    return $has_flag.not();
        else        return JExpr.TRUE;
    }

    public JExpression count() {
        return JOp.cond( $has_flag, JExpr.lit(1), JExpr.lit(0) );
    }
    
    public void setter( JBlock block, JExpression newValue ) {
        block.assign(ref(), newValue);
        block.assign($has_flag, JExpr.TRUE);
    }
    
    public FieldMarshallerGenerator createMarshaller(JBlock block, String uniqueId) {
        return new FieldMarshallerGenerator() {
            public JExpression hasMore() {
                // hasMore() is true if the has_flag is true.
                return $has_flag;
            }
            public JExpression peek(boolean increment) {
                return ref();
            }
            public void increment(BlockReference block) {}
            public FieldMarshallerGenerator clone( JBlock block, String uniqueId ) {
                // since this iterator has only one item,
                // there is no need to clone it.
                return this;
            }
            public FieldRenderer owner() { return OptionalUnboxedFieldRenderer.this; }
        };
    }
}
