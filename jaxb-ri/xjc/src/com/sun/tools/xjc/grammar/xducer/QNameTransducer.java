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
