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
package com.sun.tools.xjc.grammar.ext;

import org.xml.sax.Locator;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.msv.grammar.NameClass;
import com.sun.tools.xjc.generator.GeneratorContext;
import com.sun.tools.xjc.generator.marshaller.FieldMarshallerGenerator;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.ExternalItem;
import com.sun.tools.xjc.runtime.ContentHandlerAdaptor;
import com.sun.tools.xjc.runtime.Dom4jUnmarshallingEventHandler;

/**
 * {@link com.sun.tools.xjc.grammar.ExternalItem} that maps
 * a sub-tree into a dom4j tree.
 * 
 * <p>
 * A care has been taken not to refer to any of the dom4j class,
 * so that the compilation doesn't require dom4j in the classpath.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
class Dom4jItem extends AbstractDOMItem {
    
    private final JType elementType;

    public static DOMItemFactory factory = new DOMItemFactory() {
        public ExternalItem create(NameClass _elementName, AnnotatedGrammar grammar, Locator loc) {
            return new Dom4jItem(_elementName, grammar, loc);
        }
    };
    
    public Dom4jItem(NameClass _elementName, AnnotatedGrammar grammar, Locator loc) {
        super(_elementName, grammar, loc);
        
        this.elementType = createPhantomType("org.dom4j.Element");
    }

    public void generateMarshaller(GeneratorContext context, JBlock block, FieldMarshallerGenerator fmg, JExpression $context) {
        // [RESULT]
        // SAXWriter w = new SAXWriter();
        // w.setContentHanler(new ContentHandlerAdaptor($context));
        // w.write( (Element)<nextObj> );
        
        block = block.block();
        block.directStatement("org.dom4j.io.SAXWriter w = new org.dom4j.io.SAXWriter();");
        JExpression $w = JExpr.direct("w");
        block.invoke($w,"setContentHandler").arg(
            JExpr._new(context.getRuntime(ContentHandlerAdaptor.class))
            .arg($context));
        block.invoke($w,"write").arg(
            JExpr.cast(elementType, fmg.peek(true)));
    }

    public JExpression generateUnmarshaller(GeneratorContext context, JExpression $context, JBlock block, JExpression memento, JVar $uri, JVar $local, JVar $qname, JVar $atts) {
        // [RESULT]
        // Dom4jUnmarshallingEventHandler u = new Dom4jUnmarshallingEventHandler(context);
        // context.pushContentHandler( u, memento );
        
        JClass handlerClass = context.getRuntime(Dom4jUnmarshallingEventHandler.class);
        JVar $u = block.decl(handlerClass,"u",JExpr._new(handlerClass).arg($context));
        
        block.invoke($context,"pushContentHandler").arg($u).arg(memento);
        block.invoke($context.invoke("getCurrentHandler"),"enterElement")
            .arg($uri).arg($local).arg($qname).arg($atts);
        
        // [RESULT] u.getOwner()
        return $u.invoke("getOwner");
    }

    public JType getType() {
        return elementType;
    }

    public JExpression createRootUnmarshaller(GeneratorContext context, JVar $unmarshallingContext) {
        JClass handlerClass = context.getRuntime(Dom4jUnmarshallingEventHandler.class);
        
        // [RESULT]
        // new Dom4jUnmarshallingEventHandler(context);
        return JExpr._new(handlerClass).arg($unmarshallingContext); 
    }

}
