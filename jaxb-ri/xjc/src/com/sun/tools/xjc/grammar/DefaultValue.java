/*
 * @(#)$Id: DefaultValue.java,v 1.1 2004-06-25 21:14:34 kohsuke Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.grammar;

import com.sun.codemodel.JExpression;
import com.sun.msv.grammar.ValueExp;
import com.sun.tools.xjc.grammar.xducer.Transducer;

/**
 * Stores a default value. A pair of {@link ValueExp}, which holds
 * the actual value defined in the schema, and {@link Transducer},
 * which determines how it's supposed to be mapped to Java.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public final class DefaultValue {
    public final Transducer xducer;
    public final ValueExp value;
    
    public DefaultValue(Transducer _xducer, ValueExp _value) {
        this.xducer = _xducer;
        this.value = _value;
    }
    
    public JExpression generateConstant() {
        return xducer.generateConstant(value);
    }
}
