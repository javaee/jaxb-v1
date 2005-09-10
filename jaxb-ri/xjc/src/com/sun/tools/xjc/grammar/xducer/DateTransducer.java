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

import java.util.Calendar;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JType;
import com.sun.msv.datatype.xsd.datetime.IDateTimeValueType;
import com.sun.msv.grammar.ValueExp;
import com.sun.xml.bind.util.CalendarConv;

/**
 * Transducer for the xs:date/xs:time/xs:dateTime type.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class DateTransducer extends TransducerImpl {

    /**
     * @param datatypeImpl
     *      JClass object that represents
     *      <code>com.sun.msv.datatype.xsd.DateType</code>,
     *      TimeType, or DateTimeType.
     */
    public DateTransducer( JCodeModel cm, JClass datatypeImpl ) {
        this.codeModel = cm;
        this.datatypeImpl = datatypeImpl;
    }
    
    private final JCodeModel codeModel;
    private final JClass datatypeImpl;
    
    public JExpression generateConstant(ValueExp exp) {
        // this is used only for xs:date, so exp.exp is guaranteed to be of DateType
        Calendar data = ((IDateTimeValueType)exp.value).toCalendar();
        
        String str = CalendarConv.formatter.format(data.getTime());
        
        return codeModel.ref(CalendarConv.class).staticInvoke("createCalendar")
            .arg(JExpr.lit(str));
    }

    public JExpression generateDeserializer(JExpression literal, DeserializerContext context) {
        // [RESULT] DateType.theInstance.createJavaObject(str,context)
        return JExpr.cast( getReturnType(), datatypeImpl.staticRef("theInstance")
            .invoke("createJavaObject").arg(literal).arg(JExpr._null()) );
    }

    public JExpression generateSerializer(JExpression value, SerializerContext context) {
        // [RESULT] DateType.theInstance.serializeJavaObject(obj,null);
        return datatypeImpl.staticRef("theInstance")
            .invoke("serializeJavaObject").arg(value).arg(JExpr._null());
    }

    public JType getReturnType() {
        return codeModel.ref(Calendar.class);
    }

}
