/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
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
