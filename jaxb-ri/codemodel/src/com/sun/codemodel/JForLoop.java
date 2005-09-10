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

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;


/**
 * For statement
 */

public class JForLoop implements JStatement {

    private List inits = new ArrayList();
    private JExpression test = null;
    private List updates = new ArrayList();
    private JBlock body = null;

    public JVar init(int mods, JType type, String var, JExpression e) {
	JVar v = new JVar(JMods.forVar(mods), type, var, e);
	inits.add(v);
	return v;
    }

    public JVar init(JType type, String var, JExpression e) {
	return init(JMod.NONE, type, var, e);
    }

    public void init(JVar v, JExpression e) {
	inits.add(JExpr.assign(v, e));
    }

    public void test(JExpression e) {
	this.test = e;
    }

    public void update(JExpression e) {
	updates.add(e);
    }

    public JBlock body() {
	if (body == null) body = new JBlock();
	return body;
    }

    public void state(JFormatter f) {
	f.p("for (");
	boolean first = true;
	for (Iterator i = inits.iterator(); i.hasNext();) {
	    if (!first) f.p(',');
	    Object o = i.next();
	    if (o instanceof JVar)
		f.b((JVar)o);
	    else
		f.g((JExpression)o);
	    first = false;
	}
	f.p(';').g(test).p(';');
	first = true;
	for (Iterator i = updates.iterator(); i.hasNext();) {
	    if (!first) f.p(',');
	    f.g((JExpression)(i.next()));
	    first = false;
	}
	f.p(')');
	if (body != null)
	    f.g(body).nl();
	else
	    f.p(';').nl();
    }

}
