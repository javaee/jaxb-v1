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

package com.sun.tools.xjc.grammar.id;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JType;
import com.sun.msv.grammar.ValueExp;
import com.sun.tools.xjc.generator.util.WhitespaceNormalizer;
import com.sun.tools.xjc.grammar.xducer.DeserializerContext;
import com.sun.tools.xjc.grammar.xducer.SerializerContext;
import com.sun.tools.xjc.grammar.xducer.TransducerImpl;
import com.sun.xml.bind.marshaller.IdentifiableObject;

/**
 * {@link Transducer} for the ID datatype.
 * 
 * @author
 *    <a href="mailto:kohsuke.kawaguchi@sun.com">Kohsuke KAWAGUCHI</a>
 */
public class IDREFTransducer extends TransducerImpl
{
    public IDREFTransducer( JCodeModel _codeModel, SymbolSpace _symbolSpace, boolean _whitespaceNormalization ) {
        this.codeModel = _codeModel;
        this.symbolSpace = _symbolSpace;
        this.whitespaceNormalization = _whitespaceNormalization;
    }
    
    private final JCodeModel codeModel;
    private final SymbolSpace symbolSpace;
    private final boolean whitespaceNormalization;
    
    /** Gets the symbol space to which this IDREF belongs to. */
    public SymbolSpace getIDSymbolSpace() {
        return symbolSpace;
    }
    
    public JType getReturnType() {
        return symbolSpace.getType();
    }
    
    public JExpression generateSerializer( JExpression literal, SerializerContext context ) {
        // [RESULT]
        //   context.onIDREF((IdentifiableObject)literal)
        return context.onIDREF(JExpr.cast(codeModel.ref(IdentifiableObject.class),literal));
    }
    
    public JExpression generateDeserializer( JExpression literal, DeserializerContext context ) {
        // [RESULT]
        //  (TYPE)context.getId(literal);
        return JExpr.cast(symbolSpace.getType(),
            context.getObjectFromId( whitespaceNormalization
                ?WhitespaceNormalizer.COLLAPSE.generate(codeModel,literal)
                :literal));
    }

    public boolean needsDelayedDeserialization() {
        // do a delayed de-serialization so that
        // we can correctly handle forward reference to IDs.
        return true;
    }

    public String toString() {
        return "IDREFTransducer:"+symbolSpace.toString();
    }
    
    
    public JExpression generateConstant( ValueExp exp ) {
        // waiting for a clarification from the spec
        // but most likelily this would be prohibited.
        throw new UnsupportedOperationException(
            Messages.format( Messages.CONSTANT_IDREF_ERROR ) );
    }
    
}
