/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.generator.field;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMod;
import com.sun.tools.xjc.generator.ClassContext;
import com.sun.tools.xjc.generator.marshaller.FieldMarshallerGenerator;
import com.sun.tools.xjc.generator.util.BlockReference;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.FieldUse;

/**
 * Used to generate the getValueObject/setValueObject fields.
 * 
 * @author Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class XsiTypeFieldRenderer extends SingleFieldRenderer {

    /**
     * FieldRendererFactory implementation that returns
     * XsiNilFieldRenderer.
     */
    public final static class Factory implements FieldRendererFactory {
        private final ClassItem defaultObjectType;
        public Factory( ClassItem _defaultObjectType ) {
            this.defaultObjectType = _defaultObjectType;
        }
        public FieldRenderer create(ClassContext context, FieldUse fu) {
            return new XsiTypeFieldRenderer(context,fu,
                context.parent.getClassContext(defaultObjectType).implRef);
        }
    }

    
    private final JClass defaultObject;
    
    public XsiTypeFieldRenderer( ClassContext context, FieldUse fu, JClass _defaultObject ) {
        super(context,fu);
        this.defaultObject = _defaultObject;
    }

    protected JFieldVar generateField() {
        return context.implClass.field( JMod.PROTECTED, fu.type, "_"+fu.name,
            JExpr._new(defaultObject));
    }
    
    public JExpression ifCountEqual( int i ) {
        if(i==1)    return JExpr.TRUE;
        else        return JExpr.FALSE;
    }
    public JExpression ifCountGte( int i ) {
        if(i<=1)    return JExpr.TRUE;
        else        return JExpr.FALSE;
    }
    
    public JExpression ifCountLte( int i ) {
        if(i==0)    return JExpr.FALSE;
        else        return JExpr.TRUE;
    }

    public JExpression count() {
        return JExpr.lit(1);
    }
    
    
    public FieldMarshallerGenerator createMarshaller(JBlock block, String uniqueId) {
        return new FieldMarshallerGenerator() {
            public JExpression hasMore() {
                // hasMore() is true when there is an object
                return JExpr.TRUE;
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
            public FieldRenderer owner() { return XsiTypeFieldRenderer.this; }
        };
    }

}
