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

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JType;
import com.sun.msv.grammar.ValueExp;

/**
 * Used to parse a boolean (of the value of the "xsi:nil" attribute
 * of XML Schema) into Java null value.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class NilTransducer extends TransducerImpl {
    
    public NilTransducer( JCodeModel _codeModel ) {
        this.codeModel = _codeModel;
    }
    
    private final JCodeModel codeModel;
    
    public JType getReturnType() {
        return codeModel.NULL;
    }

    public JExpression generateSerializer(JExpression value, SerializerContext context) {
        // this "true" is expected to be used as the value of the xsi:nil attribute.
        return JExpr.lit("true");
    }

    /** Deserializes null from a boolean value. */
    public JExpression generateDeserializer(JExpression literal, DeserializerContext context) {
        // since
        // <foo xsi:nil="false">5</foo> is allowed,
        // 
        // we need to check the value of xsi:nil before we unmarshal @xsi:nil.
        // validation of the attribute value is done at that time, so this doesn't 
        // need to consult the literal at all.
        return JExpr._null();
    }

    public JExpression generateConstant( ValueExp exp ) {
        // nil transducer is only used for xsi:nil, so it will never be 
        // a subject to the constant property customization.
        throw new UnsupportedOperationException();
    }

}
