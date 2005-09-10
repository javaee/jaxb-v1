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
 * Variables and fields.
 */

public class JVar extends JExpressionImpl implements JDeclaration, JAssignmentTarget {

    /**
     * Modifiers.
     */
    private JMods mods;

    /**
     * JType of the variable
     */
    JType type;

    /**
     * Name of the variable
     */
    String name;

    /**
     * Initialization of the variable in its declaration
     */
    JExpression init;

    /**
     * JVar constructor
     *
     * @param type
     *        Datatype of this variable
     *
     * @param name
     *        Name of this variable
     *
     * @param init
     *        Value to initialize this variable to
     */
    JVar(JMods mods, JType type, String name, JExpression init) {
        this.mods = mods;
        this.type = type;
        this.name = name;
        this.init = init;
    }

    /**
     * Initialize this variable
     *
     * @param init
     *        JExpression to be used to initialize this field
     */
    public JVar init(JExpression init) {
        this.init = init;
        return this;
    }

    /**
     * Get the name of this variable
     *
     * @return Name of the variable
     */
    public String name() {
        return name;
    }

    /**
     * Return the type of this variable.
     */
    public JType type() {
        return type;
    }

    public void bind(JFormatter f) {
        f.g(mods).g(type).p(name);
        if (init != null)
            f.p('=').g(init);
    }

    public void declare(JFormatter f) {
        f.b(this).p(';').nl();
    }

    public void generate(JFormatter f) {
        f.p(name);
    }

	
	
	


    public JExpression assign(JExpression rhs) {
		return JExpr.assign(this,rhs);
    }
    public JExpression assignPlus(JExpression rhs) {
		return JExpr.assignPlus(this,rhs);
    }
	
}
