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
