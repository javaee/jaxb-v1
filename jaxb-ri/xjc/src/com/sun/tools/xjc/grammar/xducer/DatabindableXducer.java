/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.grammar.xducer;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JType;
import com.sun.msv.datatype.DatabindableDatatype;
import com.sun.msv.grammar.ValueExp;

/**
 * Transducer that uses {@link DatabindableDatatype}.
 * 
 * This transducer uses <code>DatabindableDatatype</code> interface
 * to perform the actual conversion.
 * 
 * @author
 *    <a href="mailto:kohsuke.kawaguchi@sun.com">Kohsuke KAWAGUCHI</a>
 */
public class DatabindableXducer extends TransducerImpl
{
    private final DatabindableDatatype dt;
    
    public DatabindableXducer( JCodeModel writer, DatabindableDatatype _dt ) {
        this.dt = _dt;
        
        String name = dt.getJavaObjectType().getName();
        int idx = name.lastIndexOf(".");
        if(idx<0)
            returnType = writer._package("").ref(name);
        else
            returnType = writer._package(name.substring(0,idx))
                .ref(name.substring(idx+1));
    }
    
    private final JClass returnType;
    public JType getReturnType() { return returnType; }
    
    public JExpression generateSerializer( JExpression value, SerializerContext context ) {
        // TODO:
        throw new UnsupportedOperationException("TODO");
    }
    
    public JExpression generateDeserializer( JExpression value, DeserializerContext context ) {
        // TODO:
        throw new UnsupportedOperationException("TODO");
    }

    public JExpression generateConstant( ValueExp exp ) {
        // TODO:
        throw new UnsupportedOperationException("TODO");
    }
    
}
