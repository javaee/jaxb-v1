/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.generator.marshaller;

import com.sun.codemodel.JBlock;
import com.sun.msv.grammar.AttributeExp;
import com.sun.msv.grammar.ElementExp;
import com.sun.msv.grammar.ValueExp;
import com.sun.tools.xjc.generator.XmlNameStoreAlgorithm;
import com.sun.tools.xjc.grammar.ExternalItem;
import com.sun.tools.xjc.grammar.PrimitiveItem;

/**
 * During the attributes pass, marshaller looks for
 * attributes and process them.
 * 
 * @author Kohsuke KAWAGUCHI
 */
class AttributePass extends AbstractPassImpl
{
    AttributePass( Context _context ) {
        super(_context,"Attributes");
    }
    
    public void onElement( ElementExp exp ) {
        // in the attribute pass, elements are ignored
        // skip the body
        if( context.isInside() )
            context.skipPass.build(exp.contentModel);
    }
    
    public void onExternal(ExternalItem item) {
        // external item always consists of an element.
        FieldMarshallerGenerator fmg = context.getCurrentFieldMarshaller();
        fmg.increment(context.getCurrentBlock());
    }
    
    public void onAttribute( AttributeExp exp ) {
        // [RESULT]
        // $context.startAttribute(ns,uri);
        // << visit child >>
        // $context.endAttribute(ns,uri);
        
        JBlock block = getBlock(true);
        
        // generate the invocation of the startAttribute method
        XmlNameStoreAlgorithm algorithm = XmlNameStoreAlgorithm.get(exp);
        block.invoke( context.$serializer, "startAttribute" )
            .arg(algorithm.getNamespaceURI())
            .arg(algorithm.getLocalPart());

        context.bodyPass.build( exp.exp );
        
        block.invoke( context.$serializer, "endAttribute" );
    }
    
    public void onPrimitive( PrimitiveItem exp ) {
        FieldMarshallerGenerator fmg = context.getCurrentFieldMarshaller();
        fmg.increment(context.getCurrentBlock());
    }
    
    // see comment on the onValue method of the base class.
    public void onValue(ValueExp exp) {
    }
    
}
