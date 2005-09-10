/*
 * @(#)$Id: SkipPass.java,v 1.2 2005-09-10 18:20:09 kohsuke Exp $
 */

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
package com.sun.tools.xjc.generator.marshaller;

import com.sun.msv.grammar.AttributeExp;
import com.sun.msv.grammar.ElementExp;
import com.sun.msv.grammar.ValueExp;
import com.sun.tools.xjc.grammar.ExternalItem;
import com.sun.tools.xjc.grammar.PrimitiveItem;

/**
 * Increments field marshallers appropriately but don't do anything else.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class SkipPass extends AbstractPassImpl {

    SkipPass(Context _context) {
        super(_context,"Skip");
    }

    public void onElement(ElementExp exp) {
        context.skipPass.build( exp.contentModel );
    }

    public void onExternal(ExternalItem item) {
        increment();
    }

    public void onAttribute(AttributeExp exp) {
        context.skipPass.build( exp.exp );
    }

    public void onPrimitive(PrimitiveItem exp) {
        increment();
    }

    public void onValue(ValueExp exp) {
    }
    
    private void increment() {
        FieldMarshallerGenerator fmg = context.getCurrentFieldMarshaller();
        fmg.increment(context.getCurrentBlock());
    }
}
