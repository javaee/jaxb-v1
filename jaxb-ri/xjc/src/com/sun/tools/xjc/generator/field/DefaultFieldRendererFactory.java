/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.generator.field;

import java.util.ArrayList;

import com.sun.codemodel.JCodeModel;
import com.sun.tools.xjc.generator.ClassContext;
import com.sun.tools.xjc.grammar.FieldUse;

/**
 * Default implementation of the FieldRendererFactory
 * that faithfully implements the semantics demanded by the JAXB spec.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class DefaultFieldRendererFactory implements FieldRendererFactory {
    
    public DefaultFieldRendererFactory( JCodeModel codeModel) {
        this( new UntypedListFieldRenderer.Factory(
            codeModel.ref(ArrayList.class)) );
    }

    public DefaultFieldRendererFactory( FieldRendererFactory defaultCollectionFieldRenderer ) {
        this.defaultCollectionFieldRenderer = defaultCollectionFieldRenderer;
    }
    
    private FieldRendererFactory defaultCollectionFieldRenderer;

    public FieldRenderer create(ClassContext context, FieldUse fu) {
        if(fu.multiplicity.isAtMostOnce()) {
            // non-collection field
            
            // if the field item is at most one (hence not a collection) and
            // its type is a boxed type (a type that wraps a primitive type),
            // for example "java.lang.Integer", use one of the
            // derived clases of UnboxedFieldImpl
            //
            // TODO: check for bidning info for optionalPrimitiveType=boxed or
            // noHasMethod=false and noDeletedMethod=false
            if(fu.isUnboxable())
                return new OptionalUnboxedFieldRenderer(context,fu);
            else
                // otherwise use the default non-collection field
                return new SingleFieldRenderer(context,fu);
        } else {
            // this field is a collection field.
            // use untyped list as the default. This is consistent
            // to the JAXB spec.
            return defaultCollectionFieldRenderer.create(context,fu);
        }
    }

}
