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
package com.sun.tools.xjc.generator.marshaller;

import javax.xml.bind.Element;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JType;
import com.sun.msv.grammar.ChoiceExp;
import com.sun.msv.grammar.Expression;
import com.sun.tools.xjc.generator.LookupTableUse;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.FieldItem;
import com.sun.tools.xjc.grammar.TypeItem;
import com.sun.tools.xjc.grammar.util.TypeItemCollector;
import com.sun.tools.xjc.runtime.Util;
import com.sun.tools.xjc.runtime.ValidatableObject;
import com.sun.xml.bind.JAXBObject;
import com.sun.xml.bind.ProxyGroup;

/**
 * 
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
final class Inside extends AbstractSideImpl {
    
    public Inside(Context _context) {
        super(_context);
    }
    
    /**
     * Returns true if the type 't' implements {@link Element}.
     */
    private boolean isImplentingElement( TypeItem t ) {
        JType jt = t.getType();
        if( jt.isPrimitive() )  return false;
        JClass jc = (JClass)jt;
        
        return context.codeModel.ref(Element.class).isAssignableFrom(jc);
    }
    
    /**
     * Checks if a {@link ChoiceExp} can be generated into the marshaller
     * of an optimized form. If it can be done, then generates an optimized
     * marshaller and returns true. Otherwise do nothing and return false.
     * 
     * <p>
     * This method looks for a choice pattern typically created from an
     * element substitution (where a choice is of the form
     * (Element1|Element2|Element3|&lt;foo>FooType&lt;/foo>) 
     * 
     * @param   children
     *      aChoiceExp.getChildren()
     * @param   types
     *      TypeItems in each branch of choice.
     */
    private boolean tryOptimizedChoice1( Expression[] children, TypeItem[][] types ) {
        FieldMarshallerGenerator fmg = context.getCurrentFieldMarshaller();
        
        Expression rest = Expression.nullSet;
        int count=0;
        
        for( int i=0; i<children.length; i++ ) {
            if( types[i].length!=1 )
                // for simplicity. don't try to do this optimization
                // when there's more than one type in a branch.
                return false;
                
            if( isImplentingElement(types[i][0]) ) {
                if(!(children[i] instanceof ClassItem))
                    return false; // this optimization doesn't apply
                else {
                    count++;                      
                }
            } else {
                rest = context.pool.createChoice( rest, children[i] );
            }
        }
        
        if(count==0)
            return false;   // not applicable
        
        if( rest==Expression.nullSet ) {
            // [RESULT]
            // marshal();
            onMarshallableObject();
        } else {
            // [RESULT]
            // if( obj instanceof Element ) {
            //     marshal();
            // } else {
            //    ....
            // }
            IfThenElseBlockReference ifb =new IfThenElseBlockReference(
                context, fmg.peek(false)._instanceof(context.codeModel.ref(Element.class)) );
        
            context.pushNewBlock(ifb.createThenProvider());
            onMarshallableObject();
            context.popBlock();
        
            context.pushNewBlock(ifb.createElseProvider());
            context.build(rest);
            context.popBlock();
        }
        
        return true;
    }

    /**
     * Checks if a {@link ChoiceExp} can be generated into the marshaller
     * of an optimized form. If it can be done, then generates an optimized
     * marshaller and returns true. Otherwise do nothing and return false.
     * 
     * <p>
     * This method looks for a choice pattern made of a switch attribute
     * look up.
     */
    private boolean tryOptimizedChoice2( ChoiceExp exp, Expression[] children ) {
        LookupTableUse tableUse = context.genContext.getLookupTableBuilder().buildTable(exp);
        if(tableUse==null)  return false;
        
        NestedIfBlockProvider nib=null;
        FieldMarshallerGenerator fmg = context.getCurrentFieldMarshaller();
        
        if( tableUse.anomaly!=null ) {
            if(!(tableUse.anomaly instanceof ClassItem))
                return false;       // can't handle that.
            
            JClass vo = context.getRuntime(ValidatableObject.class);
            JExpression test = JExpr.cast(vo, 
                context.codeModel.ref(ProxyGroup.class).staticInvoke("blindWrap")
                    .arg(fmg.peek(false))
                    .arg(vo.dotclass())
                    .arg(JExpr._null()))
                .invoke("getPrimaryInterface");
            
            ClassItem ancls = (ClassItem)tableUse.anomaly;
            nib = new NestedIfBlockProvider(context);
            nib.startBlock( test.ne(ancls.getTypeAsDefined().dotclass()) );
        }
        
        // TODO: move this code to Passes.
        // this is just a proof of concept that it works
        if( context.currentPass==context.skipPass ) {
            ;
        } else
        if( context.currentPass==context.uriPass ) {
            getBlock(true).invoke(
                context.$serializer.invoke("getNamespaceContext"),
                "declareNamespace")
                    .arg(JExpr.lit(tableUse.switchAttName.namespaceURI))
                    .arg(JExpr._null())
                    .arg(JExpr.FALSE);
            tableUse.table.declareNamespace(
                context.getCurrentBlock(),
                fmg.peek(false),
                context);
        } else
        if( context.currentPass==context.bodyPass ) {
            ;
        } else
        if( context.currentPass==context.attPass ) {
            JBlock block = getBlock(true);
        
            // generate the invocation of the startAttribute method
            block.invoke( context.$serializer, "startAttribute" )
                .arg(JExpr.lit(tableUse.switchAttName.namespaceURI))
            .arg(JExpr.lit(tableUse.switchAttName.localName));
            
            block.invoke( context.$serializer, "text" ).arg(
                tableUse.table.reverseLookup( fmg.peek(false), context ) )
                .arg( JExpr.lit(fmg.owner().getFieldUse().name) );
        
            block.invoke( context.$serializer, "endAttribute" );
        } else {
            _assert(false);
        }
        /// until here
        
        if( nib!=null )
            nib.end();
        
        onMarshallableObject(); // for the value itself
        
        return true;
    }

    public void onChoice(ChoiceExp exp) {
        // TODO: better optimization (for example, see VendorExtension/substitutionGroup1)
        FieldMarshallerGenerator fmg = context.getCurrentFieldMarshaller();
        
        final Expression[] children = exp.getChildren();
        final TypeItem[][] types = new TypeItem[children.length][]; 

        for( int i=0; i<children.length; i++ )
            types[i] = TypeItemCollector.collect(children[i]);
        
        // look for optimizable forms of choice and handle them smartly.
        if( tryOptimizedChoice1(children,types) )
            return;     // optimization successful
        if( tryOptimizedChoice2(exp,children) )
            return;     // optimization successful
        // TODO: more optimization patterns        
        
        // otherwise generate the default marshaller
        
        NestedIfBlockProvider nib = new NestedIfBlockProvider(context);
        
        for( int i=0; i<children.length; i++ ) {
            if(types[i].length==0) {
                // this branch doesn't have any item in it.
                // take this branch if no item is left in the current marshaller.
                nib.startBlock(fmg.hasMore().not());
            } else {
                // take this branch only if the next item is one of those types
                JExpression testExp=null;
                for( int j=0; j<types[i].length; j++ ) {
                    JType t = types[i][j].getType();
                    JExpression e = instanceOf( fmg.peek(false), t );
                    
                    if(testExp==null)    testExp = e;
                    else                 testExp = testExp.cor(e);
                }
                nib.startBlock(testExp);
            }
            
            // TODO:
            // this code ignores a lot of cases where it should report errors.
            // so this can generate an incorrect marshaller.

            // [RESULT]
            // if( ... ) {
            //     visit this branch;
            // } else ...
            context.build(children[i]);
        }
        
        nib.startElse();
        // [RESULT]
        // } else {
        //     // the type of the object is not what we are expecting. report an error
        //     ...
        // }
        if( getBlock(false)!=null )
            getBlock(false).staticInvoke(context.getRuntime(Util.class),"handleTypeMismatchError")
                .arg(context.$serializer)
                .arg(JExpr._this())
                .arg(JExpr.lit(fmg.owner().getFieldUse().name))
                .arg(fmg.peek(false));
        
        nib.end();
    }

    public void onZeroOrMore(Expression exp) {
        // TODO: this implementation is too naive.
         // for example, it doesn't handle cases when
         // there are more than one FieldItems that share the same name.
            
            
        // start a new while block.
         JExpression expr = context.getCurrentFieldMarshaller().hasMore();
            
         // repeat serializing descendants until we hit a certain condition.
         context.pushNewBlock( createWhileBlock( context.getCurrentBlock(), expr ) );
            
         context.build(exp);
         context.popBlock();
    }

    
    
    
    public void onMarshallableObject() {
        FieldMarshallerGenerator fm = context.getCurrentFieldMarshaller();
        
        if( context.currentPass==context.skipPass ) {
            fm.increment(context.getCurrentBlock());
            return;
        }
        
        // [RESULT]
        // $context.onMarshallableObjectAs<methodName>(
        //     (JAXBObject)fieldIterator.peek());
        
        JClass joRef = context.codeModel.ref(JAXBObject.class);
        
        getBlock(true).invoke(
            context.$serializer,
            "childAs"+context.currentPass.getName())
            .arg(
                JExpr.cast(joRef,fm.peek(true)))
            .arg(
                JExpr.lit(fm.owner().getFieldUse().name));
    }


    public void onField(FieldItem item) {
        _assert(false);
    }

}
