/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.codemodel;

import java.util.Iterator;

/**
 * A Java class.
 */
public abstract class JClass
    extends JType
//    implements JExpression /*, Comparable*/
{
    protected JClass( JCodeModel _owner ) {
        this._owner = _owner;
    }

    /**
     * Gets the name of this class.
     *
     * @return
     *	name of this class, without any qualification.
     *	For example, this method returns "String" for
     *  <code>java.lang.String</code>.
     */
    abstract public String name();
    
    /**
     * Gets the fully qualified name of this class.
     */
    public String fullName() {
        JPackage p = _package();
        if(p.isUnnamed())   return name();
        else                return p.name()+'.'+name();
    }
	
	/** Gets the package to which this class belongs. */
    abstract public JPackage _package();
	
    private final JCodeModel _owner;
    /** Gets the JCodeModel object to which this object belongs. */
    public final JCodeModel owner() { return _owner; }
    
    /**
     * Gets the super class of this class.
     * 
     * @return
     *      Returns the JClass representing the superclass of the
     *      entity (class or interface) represented by this JClass.
     *      If this JClass represents either the Object class,
     *      or an interface, then null is returned.
     */
    abstract public JClass _extends();
    
    /**
     * Iterates all interfaces directly implemented by this class/interface.
     * 
     * @return
     *		A non-null valid iterator that iterates all
     *		{@link JClass} objects that represents those interfaces
     *		implemented by this object.
     */
    abstract public Iterator _implements();
    
    /**
     * Checks if this object represents an interface.
     */
    abstract public boolean isInterface();
	
    /**
     * If this class represents one of the wrapper classes
     * defined in the java.lang package, return the corresponding
     * primitive type. Otherwise null.
     */
    public JPrimitiveType getPrimitiveType() { return null; }
    
    /**
     * Checks the relationship between two classes.
     * <p>
     * This method works in the same way as {@link Class#isAssignableFrom(java.lang.Class)}
     * works. For example, baseClass.isAssignableFrom(derivedClass)==true.
     */
    public final boolean isAssignableFrom( JClass derived ) {
        // to avoid the confusion, always use "this" explicitly in this method.
        
        // null can be assigned to any type.
        if( derived instanceof JNullType )  return true;
        
        if( this==derived )     return true;
        
        // the only class that is assignable from an interface is
        // java.lang.Object
        if( this==_package().owner().ref(Object.class) )  return true;
        
        JClass b = derived._extends();
        if( b!=null && this.isAssignableFrom(b) )
            return true;
        
        if( this.isInterface() ) {
            Iterator itfs = derived._implements();
            while( itfs.hasNext() )
                if( this.isAssignableFrom((JClass)itfs.next()) )
                    return true;
        }
        
        return false;
    }
    
    public JClass array() {
        return new JArrayClass(owner(),this);
    }

    public String toString() {
        return this.getClass().getName() + "(" + name() + ")";
    }


    public final JExpression dotclass() {
        return JExpr.dotclass(this);
    }

    /** Generates a static method invocation. */
    public final JInvocation staticInvoke(JMethod method) {
        return staticInvoke(method.name());
    }
    
    /** Generates a static method invocation. */
    public final JInvocation staticInvoke(String method) {
        return new JInvocation(this,method);
    }
    
    /** Static field reference. */
    public final JFieldRef staticRef(String field) {
        return new JFieldRef(this, field);
    }

    /** Static field reference. */
    public final JFieldRef staticRef(JVar field) {
        return staticRef(field.name());
    }
}
