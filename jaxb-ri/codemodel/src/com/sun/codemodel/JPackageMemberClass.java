/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.codemodel;

/**
 * A Java class.
 */
class JPackageMemberClass extends JDefinedClass implements JDeclaration {

    /** JPackage containing this class */
    private JPackage pkg;
    public final JPackage _package() {
        return pkg;
    }
    public JClassContainer parentContainer() {
        return pkg;
    }

    /**
     * JClass constructor
     *
     * @param pkg
     *        JPackage containing this class
     *
     * @param mods
     *        Modifiers for this class declaration
     *
     * @param name
     *        Name of this class
     */
    JPackageMemberClass(JPackage pkg, int mods, String name) {
        this(pkg, mods, name, false);
    }

    /**
     * JClass/interface constructor
     *
     * @param pkg
     *        JPackage containing this class or interface
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
    JPackageMemberClass(JPackage pkg, int mods, String name, boolean isInterface) {
        super(mods, name, isInterface, pkg.owner());
        this.pkg = pkg;
    }

    public void declare(JFormatter f) {
        if (!pkg.isUnnamed()) {
            f.nl().d(pkg);
            f.nl();
        }
        super.declare(f);
    }

}
