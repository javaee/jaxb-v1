/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
