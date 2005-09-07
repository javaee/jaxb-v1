/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.codemodel;


/**
 * A Java class nested inside another class definition.
 */

class JNestedClass
    extends JDefinedClass
    implements JDeclaration
{

    /** JClass in which this one is nested */
    private JDefinedClass outer = null;

	// TODO: revisit
	public JPackage _package() { return outer._package(); }
	
    /**
     * JClass constructor
     *
     * @param outer
     *        Outer class containing this class
     *
     * @param mods
     *        Modifiers for this class declaration
     *
     * @param name
     *        Name of this class
     */
    JNestedClass(JDefinedClass outer, int mods, String name) {
	this(outer, mods, name, false);
    }

    /**
     * JClass/interface constructor
     *
     * @param outer
     *        Outer class containing this class or interface
     *
     * @param mods
     *        Modifiers for this class/interface declaration
     *
     * @param name
     *        Name of this class/interface
     *
     * @param isInterface
     *        <tt>true</tt> if, and only if, this is to be an interface
     */
    JNestedClass(JDefinedClass outer, int mods, String name,
		boolean isInterface)
    {
        super(mods, name, isInterface, outer.owner() );
        this.outer = outer;
    }


    public String fullName() {
        return outer.fullName()+'.'+name();
    }
    
    public String binaryName() {
        return outer.binaryName()+'$'+name();
    }
    
    /**
     * Returns the class in which this class is nested, or <tt>null</tt> if
     * this is a top-level class.
     */
    public JClass outer() {
        return outer;
    }

    public JClassContainer parentContainer() {
        return outer;
    }

}
