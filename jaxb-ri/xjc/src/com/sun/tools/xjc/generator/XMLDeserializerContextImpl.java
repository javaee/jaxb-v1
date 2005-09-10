/*
 * @(#)$Id: XMLDeserializerContextImpl.java,v 1.2 2005-09-10 18:20:04 kohsuke Exp $
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
package com.sun.tools.xjc.generator;

import com.sun.codemodel.JExpression;
import com.sun.tools.xjc.grammar.xducer.DeserializerContext;

/**
 * {@link com.sun.tools.xjc.grammar.xducer.DeserializerContext}
 * implemented for {@link com.sun.tools.xjc.runtime.UnmarshallingContext}.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public final class XMLDeserializerContextImpl implements DeserializerContext {
    
    /**
     * A variable reference to {@link com.sun.tools.xjc.runtime.UnmarshallingContext}.
     */
    private final JExpression $context;
    
    public XMLDeserializerContextImpl( JExpression  _$context ) {
        this.$context = _$context;
    }
    
    /**
     * Returns the reference to the context object itself.
     */
    public JExpression ref() {
         return $context;
    }
    
    public JExpression addToIdTable(JExpression literal) {
        return $context.invoke("addToIdTable").arg(literal);
    }

    public JExpression getObjectFromId(JExpression literal) {
        return $context.invoke("getObjectFromId").arg(literal);
    }

    public JExpression getNamespaceContext() {
        return $context;
    }

}
