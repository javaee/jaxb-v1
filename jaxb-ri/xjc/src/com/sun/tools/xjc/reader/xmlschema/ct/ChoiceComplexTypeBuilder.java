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
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSParticle;

/**
 * Binds a complex type whose immediate child is a choice
 * model group to a choice content interface.
 * This works in a model group binding model.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class ChoiceComplexTypeBuilder extends AbstractCTBuilder {
    public ChoiceComplexTypeBuilder(ComplexTypeFieldBuilder _builder) {
        super(_builder);
    }

    public boolean isApplicable(XSComplexType ct) {
        if(!bgmBuilder.getGlobalBinding().isModelGroupBinding())
            return false;
        
        if( ct.getBaseType()!=bgmBuilder.schemas.getAnyType() )
            // My reading of the spec is that if a complex type is
            // derived from another complex type by extension,
            // its top level model group is always a sequence
            // that combines the base type content model and
            // the extension defined in the new complex type.
            return false;
        
        XSParticle p = ct.getContentType().asParticle();
        if(p==null)
            return false;
        
        XSModelGroup mg = getTopLevelModelGroup(p);
        
        if( mg.getCompositor()!=XSModelGroup.CHOICE )
            return false;
        
        if( p.getMaxOccurs()>1 || p.getMaxOccurs()==XSParticle.UNBOUNDED )
            return false;   // repeating particle.
        
        return true;
    }
    
    private XSModelGroup getTopLevelModelGroup(XSParticle p) {
        XSModelGroup mg = p.getTerm().asModelGroup();
        if( p.getTerm().isModelGroupDecl() )
            mg = p.getTerm().asModelGroupDecl().getModelGroup();
        return mg;
    }
    
    public Expression build(XSComplexType ct) {
        XSModelGroup choice = getTopLevelModelGroup(ct.getContentType().asParticle());
        
        Expression body = bgmBuilder.fieldBuilder.build(choice);

        // adds attributes and we are through.
        return pool.createSequence( bgmBuilder.fieldBuilder.attributeContainer(ct), body );
    }


}
