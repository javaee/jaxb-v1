/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.codemodel;


/**
 * Java built-in primitive types.
 */
public final class JPrimitiveType extends JType {

    private final String typeName;
    private final JCodeModel owner;
    /**
     * Corresponding wrapper class.
     * For example, this would be "java.lang.Short" for short.
     */
    private final JClass wrapperClass;
    
    JPrimitiveType(JCodeModel owner, String typeName, Class wrapper ) {
        this.owner = owner;
        this.typeName = typeName;
        this.wrapperClass = owner.ref(wrapper);
    }

    public JCodeModel owner() { return owner; }

    public String fullName() {
        return typeName;
    }
        
    public String name() {
        return fullName();
    }

    public boolean isPrimitive() {
        return true;
    }
    
    public JClass array() {
        return new JArrayClass(owner,this);
    }
    
    /**
     * Obtains the wrapper class for this primitive type.
     * For example, this method returns a reference to java.lang.Integer
     * if this object represents int.
     */
    public JClass getWrapperClass() {
        return wrapperClass;
    }
    
    /**
     * Wraps an expression of this type to the corresponding wrapper class.
     * For example, if this class represents "float", this method will return
     * the expression <code>new Float(x)</code> for the paramter x.
     * 
     * REVISIT: it's not clear how this method works for VOID.
     */
    public JExpression wrap( JExpression exp ) {
        return JExpr._new(getWrapperClass()).arg(exp);
    }
    
    /**
     * Do the opposite of the wrap method.
     * 
     * REVISIT: it's not clear how this method works for VOID.
     */
    public JExpression unwrap( JExpression exp ) {
        // it just so happens that the unwrap method is always
        // things like "intValue" or "booleanValue".
        return exp.invoke(typeName+"Value");
    }

    public void generate(JFormatter f) {
        f.p(typeName);
    }
}
