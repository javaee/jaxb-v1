/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.generator.field;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JOp;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.generator.ClassContext;
import com.sun.tools.xjc.generator.JavadocBuilder;
import com.sun.tools.xjc.generator.marshaller.FieldMarshallerGenerator;
import com.sun.tools.xjc.generator.util.BlockReference;
import com.sun.tools.xjc.grammar.DefaultValue;
import com.sun.tools.xjc.grammar.FieldUse;
import com.sun.tools.xjc.reader.NameConverter;

/**
 * Realizes a property through one getter and one setter.
 * This rendered can be used only with a reference type
 * 
 * <pre>
 * T getXXX();
 * void setXXX(T value);
 * </pre>
 * 
 * This realization is only applicable to fields with (1,1)
 * or (0,1) multiplicity.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class SingleFieldRenderer extends AbstractFieldRendererWithVar {
    
    /**
     * Code fragment that gets executed when the set method
     * is called. IOW, this is an event handler of the "onSet" event.
     */
    private JBlock onSetEvent;
    
    public SingleFieldRenderer( ClassContext context, FieldUse fu ) {
        super(context,fu);
        _assert( !fu.type.isPrimitive() );
    }
    
    protected JFieldVar generateField() {
        return generateField(fu.type);
    }

    public JClass getValueType() {
        return (JClass)fu.type;
    }
    
    /**
     * Generates the following get/set methods.
     * <pre>
     * T getXXX();
     * void setXXX(T value);
     * </pre>
     */
    public void generateAccessors() {
        // [RESULT]
        // Type getXXX() {
        // #ifdef default value
        //     if(value==null)
        //         return defaultValue;
        // #endif
        //     return value;
        // }
        JMethod $get = writer.declareMethod( fu.type,
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
            
            JConditional cond = $get.body()._if(ref().eq(JExpr._null()));
            cond._then()._return( defaultValues[0].generateConstant() );
            cond._else()._return(ref());
        }

        writer.javadoc().addReturn(
            "possible object is\n"+JavadocBuilder.listPossibleTypes(fu));
         
        // [RESULT]
        // void setXXX(Type newVal) {
        //     this.value = newVal;
        //     
        //     /*onSetEventHandler*/
        // }       
        JMethod $set = writer.declareMethod( codeModel.VOID, "set"+fu.name );
        JVar $value = writer.addParameter( fu.type, "value" );
        JBlock body = $set.body();
        body.assign(ref(),$value);
        onSetEvent = body;
        
        javadoc = fu.getJavadoc();
        if(javadoc.length()==0)
            javadoc = Messages.format(
                Messages.DEFAULT_SETTER_JAVADOC,
                NameConverter.standard.toVariableName(fu.name));
        
        writer.javadoc().appendComment(javadoc);
        writer.javadoc().addParam( $value,
            "allowed object is\n"+JavadocBuilder.listPossibleTypes(fu));
    }
    
    public JBlock getOnSetEventHandler() {
        return onSetEvent;
    }
    
    public void setter( JBlock block, JExpression newValue ) {
        block.assign(ref(),newValue);               
    }

    public void toArray( JBlock block, JExpression $array ) {
        block.assign( $array.component(JExpr.lit(0)), ref() );
    }
    
    public void unsetValues( JBlock body ) {
        body.assign( ref(), JExpr._null() );
    }
    public JExpression hasSetValue() {
        return ref().ne( JExpr._null() );
    }
    public JExpression getValue() {
        return ref();
    }
    
    
    
    public JExpression ifCountEqual( int i ) {
        switch(i) {
        case 0:     return ref().eq(JExpr._null());
        case 1:     return ref().ne(JExpr._null());
        default:    return JExpr.FALSE;
        }
    }
    public JExpression ifCountGte( int i ) {
        if(i==1)    return ref().ne(JExpr._null());
        else        return JExpr.FALSE;
    }
    
    public JExpression ifCountLte( int i ) {
        if(i==0)    return ref().eq(JExpr._null());
        else        return JExpr.TRUE;
    }

    public JExpression count() {
        return JOp.cond(ref().ne(JExpr._null()),JExpr.lit(1),JExpr.lit(0));
    }
    
    
    public FieldMarshallerGenerator createMarshaller(JBlock block, String uniqueId) {
        return new FieldMarshallerGenerator() {
            public JExpression hasMore() {
                // hasMore() is true when there is an object
                return ref().ne(JExpr._null());
            }
            public JExpression peek(boolean increment) {
                return ref();
            }
            public void increment(BlockReference block) {}
            public FieldMarshallerGenerator clone( JBlock block, String uniqueId ) {
                // since this iterator has only one item (or zero),
                // there is no need to clone it.
                return this;
            }
            public FieldRenderer owner() { return SingleFieldRenderer.this; }
        };
    }

}
