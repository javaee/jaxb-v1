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
