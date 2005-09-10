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
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.SuperClassItem;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSType;

/**
 * Binds a complex type derived from another complex type
 * by restriction.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
class RestrictedComplexTypeBuilder extends AbstractCTBuilder {


    public RestrictedComplexTypeBuilder(ComplexTypeFieldBuilder _builder) {
        super(_builder);
    }

    public boolean isApplicable(XSComplexType ct) {
        XSType baseType = ct.getBaseType();
        return baseType!=bgmBuilder.schemas.getAnyType()
            &&  baseType.isComplexType()
            &&  ct.getDerivationMethod()==XSType.RESTRICTION;
    }

    public Expression build(XSComplexType ct) {
        XSComplexType baseType = ct.getBaseType().asComplexType();
        
        // build the base type
        ClassItem baseClass = bgmBuilder.selector.bindToType(baseType);
        _assert(baseClass!=null);   // global complex type must map to a class

        // determine the binding of this complex type.
        builder.recordBindingMode(ct,builder.getBindingMode(baseType));

        return new SuperClassItem(baseClass,ct.getLocator());
    }
}
