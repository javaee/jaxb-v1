/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.codemodel;


/**
 * JType constants and type factories.
 */

public abstract class JType implements JGenerable {

    /**
     * Obtains a reference to the primitive type object from a type name.
     */
    public static JPrimitiveType parse(JCodeModel codeModel, String typeName) {
        if (typeName.equals("void"))
            return codeModel.VOID;
        else if (typeName.equals("boolean"))
            return codeModel.BOOLEAN;
        else if (typeName.equals("byte"))
            return codeModel.BYTE;
        else if (typeName.equals("short"))
            return codeModel.SHORT;
        else if (typeName.equals("char"))
            return codeModel.CHAR;
        else if (typeName.equals("int"))
            return codeModel.INT;
        else if (typeName.equals("float"))
            return codeModel.FLOAT;
        else if (typeName.equals("long"))
            return codeModel.LONG;
        else if (typeName.equals("double"))
            return codeModel.DOUBLE;
        else
            throw new IllegalArgumentException("Not a primitive type: " + typeName);
    }

    /** Gets the owner code model object. */
    public abstract JCodeModel owner();
    
    /**
     * Gets the full name of the type
     */
    public abstract String fullName();

    /**
     * Gets the name of this type.
     *
     * @return
     *     Names like "int", "void", "BigInteger".
     */
    public abstract String name();
    
    /**
     * Create an array type from this type.
     * 
     * This method is undefined for primitive void type, which
     * doesn't have any corresponding array representation.
     *
     * @return A <tt>JType</tt> representing the one-dimensional array type
     *         whose element type is this type
     */
    public abstract JClass array();

    /** Tell whether or not this is an array type. */
    public boolean isArray() {
        return false;
    }

    /** Tell whether or not this is a built-in primitive type, such as int or void. */
    public boolean isPrimitive() {
        return false;
    }
    
    /**
     * Returns true if this is a referenced type.
     */
    public final boolean isReference() {
    	return !isPrimitive();
    }
    
    public JType elementType() {
        throw new IllegalArgumentException("Not an array type");
    }

    public String toString() {
        return this.getClass().getName()
                + "(" + fullName() + ")";
    }
}
