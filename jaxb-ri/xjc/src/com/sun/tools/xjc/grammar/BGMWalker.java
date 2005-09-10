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

import com.sun.msv.grammar.OtherExp;
import com.sun.msv.grammar.util.ExpressionWalker;

/**
 * A base class for visitor classes that can recognize BGM constructs.
 */
public abstract class BGMWalker extends ExpressionWalker implements JavaItemVisitor
{
    public void onOther( OtherExp exp ) {
        if(exp instanceof JavaItem)
            ((JavaItem)exp).visitJI(this);
        else
            exp.exp.visit(this);
    }
    
    public Object onClass(ClassItem item) {
        item.exp.visit(this);
        return null;
    }

    public Object onField(FieldItem item) {
        item.exp.visit(this);
        return null;
    }

    public Object onIgnore(IgnoreItem item) {
        item.exp.visit(this);
        return null;
    }

    public Object onInterface(InterfaceItem item) {
        item.exp.visit(this);
        return null;
    }

    public Object onPrimitive(PrimitiveItem item) {
        item.exp.visit(this);
        return null;
    }

    public Object onSuper(SuperClassItem item) {
        item.exp.visit(this);
        return null;
    }

    public Object onExternal(ExternalItem item) {
        item.exp.visit(this);
        return null;
    }
}
