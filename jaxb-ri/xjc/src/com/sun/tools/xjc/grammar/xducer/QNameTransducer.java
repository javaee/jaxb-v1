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

import javax.xml.bind.DatatypeConverter;
import javax.xml.namespace.QName;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JType;
import com.sun.msv.datatype.xsd.QnameValueType;
import com.sun.msv.grammar.ValueExp;
import com.sun.tools.xjc.generator.util.BlockReference;
import com.sun.tools.xjc.generator.util.WhitespaceNormalizer;

public class QNameTransducer extends TransducerImpl
{
    public QNameTransducer( JCodeModel cm ) {
        this.codeModel = cm;
    }
    
    private final JCodeModel codeModel;
    
    public void declareNamespace( BlockReference body, JExpression value, SerializerContext context ) {
        context.declareNamespace(
                body.get(true),
                value.invoke("getNamespaceURI"),
                value.invoke("getPrefix"),
                JExpr.FALSE);
    }
    
    public JExpression generateSerializer( JExpression value, SerializerContext context ) {
        // [RESULT]
        //   DatatypeConverter.printQName( value, context );
        return codeModel.ref(DatatypeConverter.class).staticInvoke("printQName")
            .arg(value).arg(context.getNamespaceContext());
    }
    
    public JExpression generateDeserializer( JExpression lexical, DeserializerContext context ) {
        // [RESULT]
        //   DatatypeConverter.parseQName( lexical, context );
        return codeModel.ref(DatatypeConverter.class).staticInvoke("parseQName")
            .arg(
                WhitespaceNormalizer.COLLAPSE.generate(codeModel,lexical)
            ).arg(context.getNamespaceContext());
    }
    
    public JType getReturnType() {
        return codeModel.ref(QName.class);
    }
    
    public JExpression generateConstant( ValueExp exp ) {
        // since this xducer is only used for QNames,
        // 
        QnameValueType data = (QnameValueType)exp.value;
        
        return JExpr._new(codeModel.ref(QName.class))
            .arg(JExpr.lit(data.namespaceURI)).arg(JExpr.lit(data.localPart));
            
    }
    
}
