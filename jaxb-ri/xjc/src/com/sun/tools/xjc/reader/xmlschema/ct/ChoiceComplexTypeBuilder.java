/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
