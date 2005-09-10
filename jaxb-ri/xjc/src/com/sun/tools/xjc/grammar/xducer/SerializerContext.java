/*
 * @(#)$Id: SerializerContext.java,v 1.2 2005-09-10 18:20:22 kohsuke Exp $
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

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JExpression;

/**
 * Interface responsible to generate code that invokes the serializer
 * context.
 * <p>
 * Serializing datatypes into texts sometimes require global coordination,
 * such as declaring new namespaces and etc. This interface abstracts
 * the details of such invocation of contextual operation, thereby
 * allowing {@link Transducer}s to be used with different kinds of
 * marshallers.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface SerializerContext {
    
    /**
     * Generates code to invoke a new namespace association.
     * 
     * @param block
     *      The code shall be appended to this block.
     * @param uri
     *      The String expression that evaluates to the namespace URI to be
     *      declared.
     * @param prefix
     *      The String expression that evaluates to the preferred prefix
     *      for this URI.
     * @param requirePrefix
     *      The boolean expression that evaluates to true if the default
     *      prefix cannot be assigned for this binding.
     */
    void declareNamespace( JBlock block, JExpression uri, JExpression prefix, JExpression requirePrefix );
    
    /**
     * Returns the expression that evaluates to
     * a {@link javax.xml.namespace.NamespaceContext} object.
     * <p>
     * For example, the object returned will be used to resolve
     * namespace prefixes for QName.
     */
    JExpression getNamespaceContext();
    
    /**
     * Returns the expression that evaluates to the ID value.
     * 
     * The evaluated string must be the same string as the value parameter.
     * This method allows the serializer to intercept the ID value
     * to potentially do the consistency check.
     * 
     * @param object
     *      An expression that evaluates to
     *      {@link com.sun.xml.bind.marshaller.IdentifiableObject}.
     *      The value must be assignable to this type.
     * @param value
     *      A {@link String} epxression.
     */
    JExpression onID( JExpression object, JExpression value );
    
    /**
     * Returns the expression that evaluates to the ID value,
     * which the target object is representing.
     * 
     * This is essentially <code>target.____jaxb____getId()</code>
     * but it allows the serializer to do the consistency check
     * over the IDREF value.
     * 
     * @param target
     *      An expression that evaluates to
     *      {@link com.sun.xml.bind.marshaller.IdentifiableObject}.
     */
    JExpression onIDREF( JExpression target );
}
