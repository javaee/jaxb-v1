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
package com.sun.tools.xjc.grammar.id;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.tools.xjc.grammar.xducer.DeserializerContext;
import com.sun.tools.xjc.grammar.xducer.IdentityTransducer;
import com.sun.tools.xjc.grammar.xducer.SerializerContext;

/**
 * {@link Transducer} for the ID datatype.
 * 
 * @author
 *    <a href="mailto:kohsuke.kawaguchi@sun.com">Kohsuke KAWAGUCHI</a>
 */
public class IDTransducer extends IdentityTransducer
{
    public IDTransducer( JCodeModel _codeModel, SymbolSpace _symbolSpace ) {
        super(_codeModel);
        this.symbolSpace = _symbolSpace;
    }
    
    private final SymbolSpace symbolSpace;
    
    public boolean isID() { return true; }
    public SymbolSpace getIDSymbolSpace() { return symbolSpace; }
    
    public JExpression generateDeserializer( JExpression literal, DeserializerContext context ) {
        return context.addToIdTable(literal);
    }

    public JExpression generateSerializer( JExpression value, SerializerContext context ) {
        return context.onID(JExpr._this(),value);
    }
}
