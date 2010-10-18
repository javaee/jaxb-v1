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
