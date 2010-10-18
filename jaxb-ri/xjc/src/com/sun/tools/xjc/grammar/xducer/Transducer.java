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

import com.sun.codemodel.JExpression;
import com.sun.codemodel.JType;
import com.sun.msv.grammar.ValueExp;
import com.sun.tools.xjc.generator.GeneratorContext;
import com.sun.tools.xjc.generator.util.BlockReference;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.id.SymbolSpace;

/**
 * Converter from a string into a Java object.
 * 
 * <p>
 * Transducers are the first step in the data binding. All literals
 * in an XML document will be eventually converted to objects by using
 * transducers.
 * 
 * <p>
 * This class should be rather considered as a "meta" transducer
 * because it is not used at run-time. Instead, it is used at compile-time
 * to generate actual transducers.
 * 
 * @author
 *    <a href="mailto:kohsuke.kawaguchi@sun.com">Kohsuke KAWAGUCHI</a>
 */
public interface Transducer
{
    // TODO: see if we can introduce "boolean isContextDependent()"
    // for JAX-FAST optimization.
    
    /**
     * Gets the type of the object created by this transducer.
     */
    JType getReturnType();
    
    /**
     * Populates the code model and fill in necessary support code.
     * 
     * <p>
     * This method is called after BGM is completely generated and
     * stabilized. If the callee needs to generate auxiliary classes,
     * it should do so in this method.
     * 
     * @param context
     *      Object that provides access to various other information.
     */
    public void populate( AnnotatedGrammar grammar, GeneratorContext context );
    
    /**
     * Generates a serializer.
     * 
     * <p>
     * This method produces a serializer that converts the "value"
     * parameter, which at run-time evaluates to a Java object
     * whose type is the type designated
     * by the {@link #getReturnType()} method into a String, which can
     * be obtained by evaluating the returned expression.
     * 
     * @param   value
     *      an expression that evaluates to a Java object, which is
     *      to be converted into a string.
     * @param   serializer
     *      an expression that evaulates to a
     *      {@link com.sun.tools.xjc.runtime.XMLSerializer}. This object
     *      can be used to obtain prefixes for namespace URIs.
     * 
     * <p>
     * TODO: this method is complex, so please rewrite the above comment
     * so that it makes sense.
     */
    JExpression generateSerializer( JExpression value, SerializerContext serializer );
    
    /**
     * Generates code that declares necessary namespace bindings for
     * this data.
     * 
     * @param   body
     *      The code should be generated in this block.
     * @param   value
     *      an expression that evaluates to QName. It has to be
     *      typed as {@link javax.xml.namespace.QName}.
     * @param   serializer
     *      A java expression that evaluates to 
     *      {@link com.sun.tools.xjc.runtime.XMLSerializer}.
     *      Call the declareNamespace method of this context to
     *      declare namespaces.
     */
    void declareNamespace( BlockReference body, JExpression value, SerializerContext serializer );
    
    /**
     * Generaets a de-serializer.
     * 
     * <p>
     * This method produces a de-serializer that converts a string
     * found in XML into a Java object.
     * 
     * @param    literal
     *        A java expression that evaluates to <code>java.lang.String</code>,
     *      which is to be converted into an object.
     * @param   context
     *      A java expression that evaluates to
     *      {@link com.sun.tools.xjc.runtime.UnmarshallingContext}.
     * 
     * @return
     *        A java expression that evaluates to the converted object,
     *        whose type is the type returned by the
     *        {@link #getReturnType()} method.
     */
    JExpression generateDeserializer( JExpression literal, DeserializerContext context );
    
    /**
     * Returns true if the transducer needs to perform deferred
     * de-serialization.
     */
    boolean needsDelayedDeserialization();
    
    /**
     * Returns true if this transducer is for ID.
     */
    boolean isID();
    
    /**
     * If the {@link #isID()} method returns true, this method
     * must return the corresponding symbol space. Otherwise return null.
     */
    SymbolSpace getIDSymbolSpace();
    
    /**
     * Returns true if this transducer behaves as the default
     * transducer specified in the JAXB spec.
     * <p>
     * This can be used as an optimization hint.
     */
    boolean isBuiltin();
    
    /**
     * Generates a Java expression that evaluates to a de-serialized
     * value that can be obtained by unmarshalling the given expression.
     * 
     * <p>
     * For example, a built-in transducer for xs:int can return
     * <code>JExpr.lit(5)</code> if the expression is &lt;value>5&lt;/value>.
     * 
     * <p>
     * This method is called after a transducer is populated.
     * 
     * @param exp
     *      This expression will contain &lt;value> patterns. The callee
     *      should extract value information from this method.
     */
    JExpression generateConstant( ValueExp exp );
}
