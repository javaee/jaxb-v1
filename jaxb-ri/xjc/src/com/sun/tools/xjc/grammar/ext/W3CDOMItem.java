/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.grammar.ext;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.xml.sax.Locator;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCatchBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JTryBlock;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.msv.grammar.NameClass;
import com.sun.tools.xjc.generator.GeneratorContext;
import com.sun.tools.xjc.generator.marshaller.FieldMarshallerGenerator;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.ExternalItem;
import com.sun.tools.xjc.runtime.ContentHandlerAdaptor;
import com.sun.tools.xjc.runtime.W3CDOMUnmarshallingEventHandler;
import com.sun.xml.bind.unmarshaller.DOMScanner;

/**
 * {@link com.sun.tools.xjc.grammar.ExternalItem} that maps
 * a sub-tree into a W3C DOM tree. 
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
class W3CDOMItem extends AbstractDOMItem {
    
    public static DOMItemFactory factory = new DOMItemFactory() {
        public ExternalItem create(NameClass _elementName, AnnotatedGrammar grammar, Locator loc) {
            return new W3CDOMItem(_elementName, grammar, loc);
        }
    };
    
    public W3CDOMItem(NameClass _elementName, AnnotatedGrammar grammar, Locator loc) {
        super(_elementName, grammar, loc);
    }

    public void generateMarshaller(GeneratorContext context, JBlock block, FieldMarshallerGenerator fmg, JExpression $context) {
        // [RESULT]
        // new DOMScanner().parse( (Element)<nextObj>, new ContentHandlerAdaptor($context) );
        
        block.invoke( JExpr._new(codeModel.ref(DOMScanner.class)), "parse" )
            .arg(
                JExpr.cast(codeModel.ref(Element.class), fmg.peek(true)))
            .arg(
                JExpr._new(context.getRuntime(ContentHandlerAdaptor.class))
                .arg($context));
    }

    public JExpression generateUnmarshaller( GeneratorContext context,
        JExpression $context, JBlock block, JExpression memento,
        JVar $uri, JVar $local, JVar $qname, JVar $atts) {
            
        // [RESULT]
        // Element s2d_r = null;
        // try {
        //     u = new W3CDOMUnmarshallingEventHandler(context);
        //     context.pushContentHandler( u, memento );
        //     context.getCurrentHandler().enterElement(uri,local,atts);
        //     s2d_r = u.getOwner();
        // } catch( ParserConfigurationException e ) {
        //     handleGenericException(e);
        // }
        
        JVar $v = block.decl(codeModel.ref(Element.class),"ur", JExpr._null());
        JClass handlerClass = context.getRuntime(W3CDOMUnmarshallingEventHandler.class);
        
        JTryBlock tryBlock = block._try();
        
        block = tryBlock.body();
        JVar $u = block.decl(handlerClass,"u",JExpr._new(handlerClass).arg($context));
        block.invoke($context,"pushContentHandler").arg($u).arg(memento);
        block.invoke($context.invoke("getCurrentHandler"),"enterElement")
            .arg($uri).arg($local).arg($qname).arg($atts);
        block.assign($v, $u.invoke("getOwner"));
        
        JCatchBlock catchBlock = tryBlock._catch(
            codeModel.ref(ParserConfigurationException.class));
        
        catchBlock.body()
            .invoke("handleGenericException").arg(catchBlock.param("e"));
        
        return $v;
    }

    public JType getType() {
        return codeModel.ref(Element.class);
    }

    public JExpression createRootUnmarshaller( GeneratorContext context, JVar $unmarshallingContext ) {
        JClass handlerClass = context.getRuntime(W3CDOMUnmarshallingEventHandler.class);
        
        // [RESULT]
        // new W3CDOMUnmarshallingEventHandler(context);
        return JExpr._new(handlerClass).arg($unmarshallingContext); 
    }
}
