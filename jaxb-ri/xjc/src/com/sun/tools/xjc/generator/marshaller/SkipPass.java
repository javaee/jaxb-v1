/*
 * @(#)$Id: SkipPass.java,v 1.1 2004-06-25 21:14:25 kohsuke Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
