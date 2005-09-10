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
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.visitor.XSContentTypeFunction;

/**
 * Builds a complex type that inherits from the anyType complex type.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
class FreshComplexTypeBuilder extends AbstractCTBuilder {

    public FreshComplexTypeBuilder(ComplexTypeFieldBuilder _builder) {
        super(_builder);
    }

    public boolean isApplicable(XSComplexType ct) {
        return ct.getBaseType()==bgmBuilder.schemas.getAnyType()
            &&  !ct.isMixed();  // not mixed
    }

    public Expression build(final XSComplexType ct) {
        XSContentType contentType = ct.getContentType();
        
        Expression exp = (Expression)contentType.apply(new XSContentTypeFunction() {
            public Object simpleType(XSSimpleType st) {
                builder.recordBindingMode(ct,ComplexTypeBindingMode.NORMAL);
                return bgmBuilder.fieldBuilder.simpleType(st);
            }

            public Object particle(XSParticle p) {
                // determine the binding of this complex type.
                builder.recordBindingMode(ct,
                    bgmBuilder.particleBinder.checkFallback(p,null)
                        ?ComplexTypeBindingMode.FALLBACK_CONTENT
                        :ComplexTypeBindingMode.NORMAL);
                            
                return bgmBuilder.particleBinder.build(p,null);
            }

            public Object empty(XSContentType e) {
                builder.recordBindingMode(ct,ComplexTypeBindingMode.NORMAL);
                return bgmBuilder.fieldBuilder.empty(e);
            }
        });
                    
        // adds attributes and we are through.
        return pool.createSequence( bgmBuilder.fieldBuilder.attributeContainer(ct), exp );
    }

}
