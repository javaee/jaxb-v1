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

package com.sun.tools.xjc.reader.xmlschema.ct;

import com.sun.msv.grammar.Expression;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSType;

/**
 * Binds a complex type derived from a simple type.
 * When a complex type is derived from a simple type, it is always
 * by extension.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class STDerivedComplexTypeBuilder extends AbstractCTBuilder {

    public STDerivedComplexTypeBuilder(ComplexTypeFieldBuilder _builder) {
        super(_builder);
    }

    public boolean isApplicable(XSComplexType ct) {
        return ct.getBaseType().isSimpleType();
    }

    public Expression build(XSComplexType ct) {
        _assert( ct.getDerivationMethod()==XSType.EXTENSION );
        
        // base type is a simple type
        XSSimpleType baseType = ct.getBaseType().asSimpleType();

        // determine the binding of this complex type.
        builder.recordBindingMode(ct,ComplexTypeBindingMode.NORMAL);
            
        Expression att = bgmBuilder.fieldBuilder.attributeContainer(ct);
            
        // UGLY CODE WARNING:
        // set the referer so that the base type can find this complex type
        // if it's a simple type. See comments to the referer field of
        // SimpleTypeBuilder.
        // bgmBuilder.simpleTypeBuilder.referer = ct;
        bgmBuilder.simpleTypeBuilder.refererStack.push( ct );
            
        // process attributes and the content type, then combine them.
        Expression exp = pool.createSequence( att,
            bgmBuilder.fieldBuilder.simpleType(baseType,ct) );
            
        bgmBuilder.simpleTypeBuilder.refererStack.pop();
        
        return exp;
    }

}
