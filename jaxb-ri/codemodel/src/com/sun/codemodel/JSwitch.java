/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.codemodel;

import java.util.Vector;
import java.util.Iterator;

/**
 * Switch statement
 */
public final class JSwitch implements JStatement {

    /**
     * Test part of switch statement.
     */
    private JExpression test;

    /**
     * vector of JCases.
     */
    private Vector cases = new Vector();
    
    /**
     * a single default case
     */
    private JCase defaultCase = null;

    /**
     * Construct a While statment
     */
    JSwitch(JExpression test) {
        this.test = test;
    }

    public JExpression test() { return test; }

    public Iterator cases() { return cases.iterator(); }

    public JCase _case( JExpression label ) {
        JCase c = new JCase( label );
        cases.add(c);
        return c;
    }

    public JCase _default() {
        // what if (default != null) ???
        
        // default cases statements don't have a label
        defaultCase = new JCase(null, true);
        return defaultCase;
    }
    
    public void state(JFormatter f) {
        if (JOp.hasTopOp(test)) {
            f.p("switch ").g(test).p(" {").nl();
        } else {
            f.p("switch (").g(test).p(')').p(" {").nl();
        }
        Iterator itr = cases();
        while(itr.hasNext())
            f.s((JCase)itr.next());
        if( defaultCase != null )
            f.s( defaultCase );
        f.p('}').nl();
    }

}
