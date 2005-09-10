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
 * Anonymous class quick hack.
 * 
 * @author
 * 	Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
class JAnonymousClass extends JDefinedClass {

    /**
     * Base interface/class from which this anonymous class is built.
     */
    private final JClass base;
    
    JAnonymousClass( JClass _base, JCodeModel owner ) {
        super(0, _base.name(), false, owner);
        this.base = _base;
    }

    public JPackage _package() {
        // this is another quick hack but this makes 
        // JInvocation happy.
        return base._package();
    }

    public JClassContainer parentContainer() {
        // unimplemented
        throw new InternalError();
    }

}
