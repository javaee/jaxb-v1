/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.generator.field;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpression;
import com.sun.tools.xjc.generator.ClassContext;
import com.sun.tools.xjc.generator.MethodWriter;
import com.sun.tools.xjc.generator.marshaller.FieldMarshallerGenerator;
import com.sun.tools.xjc.grammar.FieldUse;

/**
 * FieldRenderer that wraps another field generator
 * and produces isSetXXX unsetXXX methods.
 * 
 * <p>
 * This follows the decorator design pattern so that
 * the caller of FieldRenderer can forget about details
 * of the method generation.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class IsSetFieldRenderer implements FieldRenderer {
    
    /**
     * Creates a FR factory that wraps another FR factory.
     * 
     * @param core
     *      FRFactory to be wrapped.
     */
    public static FieldRendererFactory createFactory(
        final FieldRendererFactory core,
        final boolean generateUnSetMethod, final boolean generateIsSetMethod ) {
        
        return new FieldRendererFactory() {
            public FieldRenderer create(ClassContext context, FieldUse fu) {
                return new IsSetFieldRenderer(
                    context, fu, core.create(context,fu),
                    generateUnSetMethod, generateIsSetMethod );
            }
        };
    }

    public static FieldRendererFactory createFactory( FieldRendererFactory core ) {
        return createFactory(core,true,true);
    }

    /** Another FieldRenderer that this object is wrapping. */
    private final FieldRenderer core;
    
    private final ClassContext context;
    private final FieldUse use;
    private final boolean generateUnSetMethod;
    private final boolean generateIsSetMethod;
    
    public IsSetFieldRenderer( ClassContext _context,
        FieldUse _use, FieldRenderer _core,
        boolean generateUnSetMethod, boolean generateIsSetMethod ) {
        
        this.core = _core;
        this.context = _context;
        this.use = _use;
        this.generateUnSetMethod = generateUnSetMethod;
        this.generateIsSetMethod = generateIsSetMethod;
    }
    
    public void generate() {
        core.generate();
        
        // add isSetXXX and unsetXXX.
        MethodWriter writer = context.createMethodWriter();
        
        JCodeModel codeModel = context.parent.getCodeModel();
        
        if( generateIsSetMethod ) {
            // [RESULT] boolean isSetXXX()
            JExpression hasSetValue = core.hasSetValue();
            if( hasSetValue==null ) {
                // this field renderer doesn't support the isSet/unset methods generation.
                // issue an error
                
            }
            writer.declareMethod(codeModel.BOOLEAN,"isSet"+use.name)
                .body()._return( hasSetValue );
        }
        
        if( generateUnSetMethod ) {
            // [RESULT] void unsetXXX()
            core.unsetValues(
                writer.declareMethod(codeModel.VOID,"unset"+use.name).body() );
        }
    }
    
    public JBlock getOnSetEventHandler() {
        return core.getOnSetEventHandler();
    }
    

    public void unsetValues( JBlock body ) {
        core.unsetValues(body);
    }
    public void toArray( JBlock block, JExpression $array ) {
        core.toArray(block,$array);
    }
    public JExpression hasSetValue() {
        return core.hasSetValue();
    }
    public JExpression getValue() {
        return core.getValue();
    }
    public JClass getValueType() {
        return core.getValueType();
    }



    public FieldUse getFieldUse() {
        return core.getFieldUse();
    }

    public void setter(JBlock body, JExpression newValue) {
        core.setter(body,newValue);
    }

    public JExpression ifCountEqual(int i) {
        return core.ifCountEqual(i);
    }

    public JExpression ifCountGte(int i) {
        return core.ifCountGte(i);
    }

    public JExpression ifCountLte(int i) {
        return core.ifCountLte(i);
    }
    public JExpression count() {
        return core.count();
    }

    public FieldMarshallerGenerator createMarshaller(JBlock block, String uniqueId) {
        return core.createMarshaller(block,uniqueId);
    }

}
