/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
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
