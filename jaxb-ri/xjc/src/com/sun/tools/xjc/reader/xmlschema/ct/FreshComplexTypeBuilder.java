/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
