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
 * A field that can have a JDocComment associated with it
 */

public class JFieldVar extends JVar {

    /**
     * javadoc comments for this JMethod
     */
    private JDocComment jdoc = null;
    
    /**
     * JFieldVar constructor
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
    JFieldVar(JMods mods, JType type, String name, JExpression init) {
        super( mods, type, name, init );
    }
    /**
     * Creates, if necessary, and returns the class javadoc for this
     * JDefinedClass
     *
     * @return JDocComment containing javadocs for this class
     */
    public JDocComment javadoc() {
        if( jdoc == null ) 
                jdoc = new JDocComment();
        return jdoc;
    }

    public void declare(JFormatter f) {
        if( jdoc != null )
            f.g( jdoc );
        super.declare( f );
    }
}
