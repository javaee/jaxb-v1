/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.codemodel;

/**
 * String literal.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class JStringLiteral extends JExpressionImpl {

    public final String str;

    JStringLiteral(String what) {
        this.str = what;
    }

    public void generate(JFormatter f) {
        f.p(JExpr.quotify('"', str));
    }
}
