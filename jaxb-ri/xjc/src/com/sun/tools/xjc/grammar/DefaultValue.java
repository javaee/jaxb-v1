/*
 * @(#)$Id: DefaultValue.java,v 1.2 2005-09-10 18:20:14 kohsuke Exp $
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
