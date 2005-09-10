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
