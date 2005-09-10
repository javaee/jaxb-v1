/*
 * @(#)$Id: DeserializerContext.java,v 1.2 2005-09-10 18:20:21 kohsuke Exp $
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
package com.sun.tools.xjc.grammar.xducer;

import com.sun.codemodel.JExpression;

/**
 * 
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface DeserializerContext {
    /**
     * Registers the literal value as an ID to the object currently
     * being unmarshalled.
     */
    JExpression addToIdTable( JExpression literal );
    
    /**
     * Resolves an object from its ID value.
     */
    JExpression getObjectFromId( JExpression literal );

    
    /**
     * Returns the expression that evaluates to
     * a {@link javax.xml.namespace.NamespaceContext} object.
     * <p>
     * For example, the object returned will be used to declare
     * new namespaces for QName.
     */
    JExpression getNamespaceContext();
}
