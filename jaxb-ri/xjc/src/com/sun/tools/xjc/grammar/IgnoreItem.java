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
package com.sun.tools.xjc.grammar;

import org.xml.sax.Locator;

import com.sun.msv.grammar.Expression;

/**
 * Used to mark the ignored part of the grammar.
 * 
 * @author
 *    <a href="mailto:kohsuke.kawaguchi@sun.com">Kohsuke KAWAGUCHI</a>
 */
public class IgnoreItem extends JavaItem {
    public IgnoreItem(Locator loc) {
        super("$ignore", loc);
    }

    public IgnoreItem(Expression exp, Locator loc) {
        this(loc);
        this.exp = exp;
    }
    public Object visitJI(JavaItemVisitor visitor) {
        return visitor.onIgnore(this);
    }
}
