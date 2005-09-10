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

import com.sun.msv.datatype.xsd.StringType;
import com.sun.msv.grammar.Expression;
import com.sun.tools.xjc.grammar.FieldItem;
import com.sun.tools.xjc.grammar.util.Multiplicity;
import com.sun.tools.xjc.grammar.xducer.IdentityTransducer;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIProperty;
import com.sun.xml.xsom.XSComplexType;

/**
 * Builds a complex type that (1) has mixed content model and 
 * (2) is derived from anyType
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
class MixedComplexTypeBuilder extends AbstractCTBuilder {
    
    public MixedComplexTypeBuilder( ComplexTypeFieldBuilder _builder ) {
        super(_builder);
    }
    
    
    public boolean isApplicable(XSComplexType ct) {
        return ct.getBaseType()==bgmBuilder.schemas.getAnyType()
            &&  ct.isMixed();
    }

    public Expression build(XSComplexType ct) {
        // if mixed, we fallback immediately
        BIProperty prop = BIProperty.getCustomization( bgmBuilder, ct );
                    
                    
        // determine the binding of this complex type.
        builder.recordBindingMode(ct,ComplexTypeBindingMode.FALLBACK_CONTENT);

        FieldItem fi = prop.createFieldItem(
            "Content", false,
            pool.createInterleave(
                pool.createZeroOrMore(
                    bgmBuilder.grammar.createPrimitiveItem(
                        new IdentityTransducer(bgmBuilder.grammar.codeModel),
                        StringType.theInstance,
                        pool.createData(StringType.theInstance),
                        ct.getLocator()) ),
                bgmBuilder.typeBuilder.build(ct.getContentType())),
            ct );
                 
        // forcibly set the multiplicity. Otherwise the optimizer might
        // think that we don't need List for this field.   
        fi.multiplicity = Multiplicity.star;
        fi.collisionExpected = true;
                    
        // adds attributes and we are done.
        return pool.createSequence( bgmBuilder.fieldBuilder.attributeContainer(ct), fi );
    }

}
