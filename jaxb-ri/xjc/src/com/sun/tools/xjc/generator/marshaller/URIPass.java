/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.generator.marshaller;

import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JStringLiteral;
import com.sun.msv.grammar.AttributeExp;
import com.sun.msv.grammar.ElementExp;
import com.sun.msv.grammar.ValueExp;
import com.sun.tools.xjc.generator.XmlNameStoreAlgorithm;
import com.sun.tools.xjc.grammar.ExternalItem;
import com.sun.tools.xjc.grammar.PrimitiveItem;
import com.sun.tools.xjc.grammar.xducer.Transducer;
import com.sun.tools.xjc.grammar.xducer.TypeAdaptedTransducer;

/**
 * During the URI pass, the marshaller looks for data
 * that needs to produce 
 * 
 * @author Kohsuke KAWAGUCHI
 */
class URIPass extends AbstractPassImpl
{
    URIPass( Context _context ) {
        super(_context,"URIs");
    }
    
    public void onElement( ElementExp exp ) {
        // skip the body
        if( context.isInside() )
            context.skipPass.build(exp.contentModel);
    }
    
    public void onExternal(ExternalItem item) {
        // external item always consists of an element.
        // so again don't go into it.
        FieldMarshallerGenerator fmg = context.getCurrentFieldMarshaller();
        fmg.increment(context.getCurrentBlock());
    }
    
    public void onAttribute( AttributeExp exp ) {
        // URI of the attribute name needs to be declared.
        XmlNameStoreAlgorithm algorithm = XmlNameStoreAlgorithm.get(exp);
        JExpression namespaceURI = algorithm.getNamespaceURI();
        
        if((namespaceURI instanceof JStringLiteral)
        && ((JStringLiteral)namespaceURI).str.equals(""))
            ; // optimization. no need to declare.
        else {
            getBlock(true).invoke(
                context.$serializer.invoke("getNamespaceContext"),
                "declareNamespace")
                    .arg(namespaceURI)
                    .arg(JExpr._null())
                    .arg(JExpr.TRUE);
        }

        // look inside.
        context.uriPass.build( exp.exp );
    }
    
    public void onPrimitive( PrimitiveItem exp ) {
        
        FieldMarshallerGenerator fmg = context.getCurrentFieldMarshaller();
        
        // adapt the transducer if necessary
        Transducer xducer = TypeAdaptedTransducer.adapt(
            exp.xducer,
            fmg.owner().getFieldUse().type );
        
        // allow it to declare namespace if it's necessary
        xducer.declareNamespace( context.getCurrentBlock(),
            JExpr.cast( xducer.getReturnType(), fmg.peek(false)), context );
        
        fmg.increment(context.getCurrentBlock());
    }
    
    // see the comment on the base class
    public void onValue(ValueExp exp) {
        // TODO: collect namespace declaration from this value.

/*
        // we know that XSDLib uses QNameValueType for QName,
        // so we handle this as a special case.
        // it would be nice if we can come up with an abstraction
        // to avoid hard-coding dependencies like this.
        if(exp.value instanceof QNameValueType) {
            context.getCurrentBlock().invoke(
                $context, "declareNamespace" )
                .arg( JExpr.lit(((QnameValueType)exp.value).namespaceUri) );
        }
*/
    }

}
