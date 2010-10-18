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

package com.sun.tools.xjc.generator.marshaller;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JExpr;
import com.sun.msv.grammar.AttributeExp;
import com.sun.msv.grammar.ElementExp;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.NameClassAndExpression;
import com.sun.msv.grammar.ValueExp;
import com.sun.tools.xjc.generator.XmlNameStoreAlgorithm;
import com.sun.tools.xjc.grammar.BGMWalker;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.ExternalItem;
import com.sun.tools.xjc.grammar.FieldItem;
import com.sun.tools.xjc.grammar.IgnoreItem;
import com.sun.tools.xjc.grammar.InterfaceItem;
import com.sun.tools.xjc.grammar.PrimitiveItem;
import com.sun.tools.xjc.grammar.SuperClassItem;
import com.sun.tools.xjc.grammar.TypeItem;
import com.sun.tools.xjc.grammar.xducer.Transducer;
import com.sun.tools.xjc.grammar.xducer.TypeAdaptedTransducer;

/**
 * Generates the primary part of the marshaller.
 * During the primary pass, marshaller serializes elements
 * and characters.
 * 
 * @author Kohsuke KAWAGUCHI
 */
class BodyPass extends AbstractPassImpl
{
    BodyPass( Context _context, String name ) {
        super(_context,name);
    }
    
    
    
    private final class FieldFinder extends BGMWalker {
        /** set of {@link FieldMarshallerGenerator}s.*/
        private final Set result = new HashSet();
        
        private final boolean visitAttributes;
        
        public FieldFinder(boolean _visitAttributes) {
            this.visitAttributes = _visitAttributes;
        }
        
        public Set getResult() { return result; }


        public void onAttribute( AttributeExp exp ) {
            if(visitAttributes)
                super.onAttribute(exp);
        }
        
        public Object onField( FieldItem item ) {
            // memorize the newly detected field item and
            // process further.
            context.pushFieldItem(item);
            item.exp.visit(this);
            context.popFieldItem(item);
            return null;    // return value unused
        }

        public Object onIgnore( IgnoreItem exp ) {
            // IgnoreItem will not contain any reference to objects,
            // so no need to visit it.
            return null;
        }
        
        public Object onSuper( SuperClassItem exp ) {
            // marshalling of the super class should be completely independent
            // from that of this class.
            // unfortunately, there is a potential danger when the fields of
            // the derived class conflicts with those of the super class.
            //
            // For example,
            // <class name="D">
            //   <super ref="B"/>
            //   <field name="foo">
            //     <element name="foo-extended">
            //       <text/>
            //     </element>
            //   </field>
            // </class>
            // <class name="B">
            //   <field name="foo">
            //     <zeroOrMore>
            //       <element name="foo">
            //         <text/>
            //       </element>
            //     </zeroOrMore>
            //   </field>
            // </class>
            
            // TODO: the above situation must be handled
            // I guess we need to produce an error for the above  -kk
            return null;    // return value unused
        }
    
        public Object onInterface( InterfaceItem exp ) {
            return onTypedItem(exp);
        }
        public Object onExternal( ExternalItem exp ) {
            return onTypedItem(exp);
        }
        public Object onClass( ClassItem exp ) {
            return onTypedItem(exp);
        }
        public Object onPrimitive( PrimitiveItem exp ) {
            return onTypedItem(exp);
        }
        private Object onTypedItem( TypeItem ti ) {
            // memorize this field marshaller.
            result.add( context.getCurrentFieldMarshaller() );
            return null;    //return value unused
        }
    }
    
    private class FieldCloner
    {
        private final Set results;
        
        protected FieldCloner( Expression e, boolean visitAttributes ) {
            FieldFinder ff = new FieldFinder(visitAttributes);
            e.visit(ff);
            this.results = ff.getResult();
        }
        
        /**
         * Replaces the current {@link FieldMarshallerGenerator}s by
         * their clones.
         */
        public void push() {
            Iterator itr = results.iterator();
            while(itr.hasNext()) {
                FieldMarshallerGenerator fmg = (FieldMarshallerGenerator)itr.next();
            
                FieldMarshallerGenerator cloned =
                    fmg.clone( getBlock(true), context.createIdentifier() );
            
                context.pushNewFieldMarshallerMapping(fmg,cloned);
            }
        }
        
        /**
         * Kills clones and restore the original {@link FieldMarshallerGenerator}s. 
         */
        public void pop() {
            int cnt= results.size();
            for( int i=0; i<cnt; i++ )
                context.popFieldMarshallerMapping();
        }
    }
    
    
    
    
    public void onElement( ElementExp exp ) {
        _onElement(exp);
    }

//    public void onSkippableElement(SkippableElementExp item) {
//        // [RESULT]
//        // if( <obj> instanceof Element )
//        //    marshal(<obj>);
//        // else
//        //    <<process as element>>
//        JBlock block = getBlock();
//        
//        FieldMarshallerGenerator fmg = context.getCurrentFieldMarshaller();
//        
//        JConditional cond = block._if(
//            fmg.peek(false,false)._instanceof(
//                context.codeModel.ref(Element.class)) );
//        
//        context.pushNewBlock( new ExistingBlockReference(cond._then()) );
//        context.build(item.getContentModel());
//        context.popBlock();
//        
//        context.pushNewBlock( new ExistingBlockReference(cond._else()) );
//        _onElement(item);
//        context.popBlock();
//    } 
    
    private void _onElement( NameClassAndExpression exp ) {
        Expression contentModel = exp.getContentModel();
        
        // [RESULT]
        // $context.startElement($ns,$local);
        // << uri pass visit >>
        // $context.endNamespaceDecls();
        // << attribute pass visit >>
        // $context.endAttributes();
        // << primary pass visit >>
        // $context.endElement();
        JBlock block = getBlock(true);
                
       
        FieldCloner fc;
        
        // generate the invocation of the startElement method
        XmlNameStoreAlgorithm algorithm = XmlNameStoreAlgorithm.get(exp.getNameClass());
        block.invoke( context.$serializer, "startElement" )
            .arg(algorithm.getNamespaceURI())
            .arg(algorithm.getLocalPart());
        
        // 1st pass
        fc = new FieldCloner( contentModel, true );
        fc.push();
        context.uriPass.build( contentModel );
        block.invoke( context.$serializer, "endNamespaceDecls" );
        fc.pop();
        
        // 2nd pass
        fc = new FieldCloner( contentModel, false );
        fc.push();
        context.attPass.build( contentModel );
        block.invoke( context.$serializer, "endAttributes" );
        fc.pop();
        
        // 3rd pass
        context.bodyPass.build( contentModel );
        
        block.invoke( context.$serializer, "endElement" );
    }

    
    public void onExternal(ExternalItem item) {
        // external item always consists of an element.
        item.generateMarshaller(
            context.genContext,
            getBlock(true),    // current block
            context.getCurrentFieldMarshaller(),
            context.$serializer           // marshaller object
            );
    }
    
    public void onAttribute( AttributeExp exp ) {
        // in the elements pass, attributes are ignored
        ;
    }
    
    public void onPrimitive( PrimitiveItem exp ) {
        // [RESULT]
        // $context.text( <serializer>( $obj, $context ), <fieldName> );
        
        FieldMarshallerGenerator fmg = context.getCurrentFieldMarshaller();
        
        // adapt the transducer if necessary
        Transducer xducer = TypeAdaptedTransducer.adapt(
            exp.xducer,
            fmg.owner().getFieldUse().type );
        
        getBlock(true).invoke( context.$serializer, "text" )
            .arg(xducer.generateSerializer(
                // fmg.peek(true,true) will only cast an object to the type of the field.
                // but the type expected by the xducer could be more specific
                JExpr.cast( xducer.getReturnType(), fmg.peek(true)),
                context))
            .arg(JExpr.lit(fmg.owner().getFieldUse().name));
    }

    // see the comment on the base class
    public void onValue(ValueExp exp) {
        // marshal this value
        marshalValue(exp);
    }
}
