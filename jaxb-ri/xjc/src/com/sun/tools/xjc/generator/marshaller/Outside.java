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

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JForLoop;
import com.sun.codemodel.JVar;
import com.sun.msv.grammar.AttributeExp;
import com.sun.msv.grammar.ChoiceExp;
import com.sun.msv.grammar.ElementExp;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.OtherExp;
import com.sun.msv.grammar.util.ExpressionFinder;
import com.sun.tools.xjc.generator.field.FieldRenderer;
import com.sun.tools.xjc.generator.util.BlockReference;
import com.sun.tools.xjc.generator.util.LazyBlockReference;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.ExternalItem;
import com.sun.tools.xjc.grammar.FieldItem;
import com.sun.tools.xjc.grammar.IgnoreItem;
import com.sun.tools.xjc.grammar.InterfaceItem;
import com.sun.tools.xjc.grammar.JavaItem;
import com.sun.tools.xjc.grammar.JavaItemVisitor;
import com.sun.tools.xjc.grammar.PrimitiveItem;
import com.sun.tools.xjc.grammar.SuperClassItem;
import com.sun.tools.xjc.grammar.TypeItem;
import com.sun.tools.xjc.grammar.util.FieldItemCollector;
import com.sun.tools.xjc.grammar.util.FieldMultiplicityCounter;
import com.sun.tools.xjc.grammar.util.Multiplicity;
import com.sun.tools.xjc.runtime.Util;
import com.sun.xml.bind.JAXBAssertionError;
import com.sun.xml.bind.JAXBObject;

/**
 * Builds marshaller for expressions outside {@link FieldItem}.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
class Outside extends AbstractSideImpl {
    
    protected Outside(Context _context) {
        super(_context);
    }
    
    

    /**
     * Returns true if a field item is not-optimizable
     */
    private static class NonOptimizabilityChecker
        extends ExpressionFinder implements JavaItemVisitor {
        
        public boolean onElement(ElementExp e)         { return true; }
        public boolean onAttribute(AttributeExp a)     { return true; }
        public boolean onOther(OtherExp exp) {
            if(exp instanceof JavaItem)
                return ((Boolean)((JavaItem)exp).visitJI(this)).booleanValue();
            else
                return exp.exp.visit(this);
        }
        
        // TypeItems
        public Object onClass(ClassItem c)          { return Boolean.FALSE; }
        public Object onInterface(InterfaceItem i)  { return Boolean.FALSE; }
        public Object onPrimitive(PrimitiveItem p)  { return Boolean.FALSE; }
        public Object onExternal(ExternalItem p)    { return Boolean.FALSE; }
        
        public Object onIgnore(IgnoreItem i)        { return Boolean.TRUE; }
        
        // because of the restriction of BGM, we won't see them.
        public Object onField(FieldItem f)          { throw new JAXBAssertionError(); }
        public Object onSuper(SuperClassItem s)     { throw new JAXBAssertionError(); }
    };

    // since this object doesn't have any state, one instance is enough.
    private static final ExpressionFinder isNonOptimizable = new NonOptimizabilityChecker();
    
    
    
    

    
    
    
    

    public void onChoice(ChoiceExp exp) {
        final Expression[] children = exp.getChildren();
        
        NestedIfBlockProvider nib = new NestedIfBlockProvider(context);
            
        // we can have at most one "default" branch which doesn't have
        // any FieldItem (that means we don't have any positive clue that
        // we should take the branch.) Such a branch will be taken only if all
        // other branches fail. So that's why we can have at most one.
        Expression defaultBranch = null;    // null to indicate there is no default branch.
            
        // when we are inside <oneOrMore>, we can't generally test
        // item count strongly, because the same field might be marshalled
        // later. For example, content models like (A,B)+ and (A+,B,A+).
        // 
        // determine if we can run a strong test, or we need to rely on
        // weak test.
        boolean strong = true;
        if(context.inOneOrMore) strong = false;
        // TODO: this doesn't identify the (A+,B,A+) case.
            
        FieldItem[] allFi = FieldItemCollector.collect(exp);
            
        for( int i=0; i<children.length; i++ ) {
            Expression e = children[i];
            FieldItem[] fi = FieldItemCollector.collect(e);
            if(fi.length==0) {
                // this is a default branch
                if(defaultBranch==null) {
                    defaultBranch = children[i];
                    continue;
                } else {
                    // there are more than one default branch.
                    // we can ignore this branch, since
                    // taking this branch doesn't result in marshaling
                    // of any object. (if we have child objects in this branch,
                    // we should have FieldItem in this branch)
                        
                    // TODO: we might still want to issue a warning
                        
                    // ignore the branch
                    continue;
                }
            }
                
            // [RESULT]
            // if( ... ) {
            //     visit this branch;
            // } else ...
                
            nib.startBlock(strong?
                createStrongTest(children[i],allFi) :
                createWeakTest(fi));
            context.build(children[i]);
        }
            
        if(defaultBranch!=null) {
            nib.startElse();
            context.build(defaultBranch);
        }
            
        nib.end();
    }

    public void onZeroOrMore(Expression exp) {
        // if we are not in the field, we need to collect
        // FieldItems under this expression.
        // then compute the test clause from it.
        JExpression expr = createWeakTest(FieldItemCollector.collect(exp));
            
        if(expr==null) {
            // if there is no child item, treat this <oneOrMore>
            // as <group>
            context.build(exp);
            return;
        }
            
        // repeat serializing descendants until we hit a certain condition.
        context.pushNewBlock( createWhileBlock( context.getCurrentBlock(), expr ) );
            
        context.build(exp);
        context.popBlock();
    }

    public void onMarshallableObject() {
        // we don't see those TypeItems outside FieldItem
        _assert(false);
    }


    public void onField( final FieldItem item ) {
        
        final FieldMarshallerGenerator fmg = context.getMarshaller(item);
        if(fmg==null)
            // if this field is not a subject to the marshalling, we don't need
            // to do anything in this method.
            return;
        
        context.pushFieldItem(item);

        
        // if a field item doesn't have any element or attribute
        // until InterfaceItem/ClassItem, then we can optimize it
        // by just writing them out in a proper order
        
        if(item.exp.visit(isNonOptimizable)) {
            // non-optimizable:  there are elements, attributes, and/or primitive items.
            
            // memorize the newly detected field item and
            // process further.
            context.build(item.exp);
        } else {
            // optimizable: we can just marshal child class items.
            
            
            if(item.multiplicity.max==null) {
                // marshal all
                context.pushNewBlock( createWhileBlock(
                    context.getCurrentBlock(), fmg.hasMore() ));
                onTypeItem(item);
                context.popBlock();
            } else {
                // for a given multiplicity (l,u)
                // marshal objects at least l times
                for( int i=0; i<item.multiplicity.min; i++ )
                    onTypeItem(item);
            
                // then marshal the rest of objects at most (u-l) times
                // or until no object is available.
                // 
                // of course this is 
                final int repeatCount = item.multiplicity.max.intValue()-item.multiplicity.min;
                
                if(repeatCount>0) {
                    
                    final BlockReference parent = context.getCurrentBlock();
                    context.pushNewBlock( new LazyBlockReference() {
                        public JBlock create() {
                            JCodeModel codeModel = context.codeModel;
                            
                            // [RESULT]
                            // for( int $tmp=u-l; $tmp>0 && hasMore(); $tmp-- )
                            JForLoop _for = parent.get(true)._for();
                            
                            JVar $tmp = _for.init(codeModel.INT,
                                context.createIdentifier(),JExpr.lit(repeatCount));
                            _for.test( $tmp.gt(JExpr.lit(0)).cand(fmg.hasMore()));
                            _for.update( $tmp.decr() );
                            
                            return _for.body();
                        }
                    });
                    onTypeItem(item);
                    context.popBlock();
                }
            }
        }
        
        context.popFieldItem(item);
    }


    /**
     * Generates code that marshals one object from the specified field.
     */
    private void onTypeItem( FieldItem parent ) {
        // get TypeItems that appear under this FieldItem
        TypeItem[] types = parent.listTypes();
        TypeItem.sort(types);
        
        boolean haveSerializableObject = false;
        boolean haveOtherObject = false;
        for(int i=0; i<types.length; i++ ) {
            if(types[i] instanceof PrimitiveItem) haveOtherObject=true;
            else
            if(types[i] instanceof InterfaceItem) haveSerializableObject=true;
            else
            if(types[i] instanceof ClassItem)     haveSerializableObject=true;
            else
            if(types[i] instanceof ExternalItem)  haveOtherObject=true;
            else
                throw new JAXBAssertionError();  // there are only four.
        }
        
        if(haveSerializableObject && !haveOtherObject) {
            // we only have marshallble objects.
            context.currentSide.onMarshallableObject();
            return;
        }
        if(!haveSerializableObject && types.length==1) {
            context.build(types[0]);
            return;
        }
        
        // create a local scope.
        JBlock block = getBlock(true).block();
        context.pushNewBlock(block);
        
        JCodeModel codeModel = context.codeModel;
        
        // [RESULT]
        //    Object o = <next item>;
        FieldMarshallerGenerator fmg = context.getCurrentFieldMarshaller();
        
        JVar $o = block.decl(
            codeModel.ref(Object.class),
            "o",    // is this name safe?
            fmg.peek(false));
        
        NestedIfBlockProvider nib = new NestedIfBlockProvider(context);
        
        if(haveSerializableObject) {
            // [RESULT]
            // if( o instanceof JAXBObject) {
            //     <marshal>(o);
            // } else {
            //     ....
            // }
            nib.startBlock( $o._instanceof(codeModel.ref(JAXBObject.class)) );
            context.currentSide.onMarshallableObject();
        }
        // process other PrimitiveItems
        for( int i=0; i<types.length; i++ ) {
            if( types[i] instanceof PrimitiveItem || types[i] instanceof ExternalItem ) {
                // [RESULT]
                // if( o instanceof <JavaType>) {
                //    <marshal>( o );
                // } else {
                //    ....
                // }
                nib.startBlock( instanceOf($o,types[i].getType()) );
                context.build(types[i]);
            }
        }
        
        // if this Java object doesn't match any of the type,
        // we need to throw a marshalling exception.
        nib.startElse();
        
        // TODO: which type of exception shall we throw?
        if( getBlock(false)!=null )
            getBlock(false).staticInvoke(context.getRuntime(Util.class),"handleTypeMismatchError")
                .arg(context.$serializer)
                .arg(JExpr._this())
                .arg(JExpr.lit(fmg.owner().getFieldUse().name))
                .arg($o);
        
        nib.end();
        context.popBlock();
    }




    /**
     * Given {fi1,fi2, ... , fin }, this method computes
     * an expression "fi1.hasNext() || fi2.hasNext() || ... || fin.hasNext()"
     * 
     * <p>
     * Do NOT generate an expression that causes a side-effect. Optimization
     * of the generated code relies on an assumption that code like
     * <pre>
     * if(...) ; else ;
     * </pre>
     * or
     * <pre>
     * while(...) ;
     * </pre>
     * can be removed safely. If the test expression has a side-effect,
     * this assumption does not hold.
     */
    private JExpression createWeakTest( FieldItem fi[] ) {
        JExpression expr = JExpr.FALSE;
        for( int i=0; i<fi.length; i++ ) {
            FieldMarshallerGenerator fmg = context.getMarshaller(fi[i]);
            if(fmg!=null)
                expr = expr.cor( fmg.hasMore() );
            // if fmg==null then we shouldn't marshal it, so hasNext doesn't
            // contribute to the test clause.
        }
        
        return expr;
    }
    
    /**
     * Creates a stronger test clause by actually placing a restriction
     * on the multiplicity of FieldItems.
     * 
     * <p>
     * All the constraints in the createWeakTest method applies.
     * 
     * @param branch
     *      A branch of the choice to which we want to count items.
     * @param fi
     *      All FieldItems that belong to this &lt;choice>
     *      (Not just this branch.)
     * 
     * @return
     *      null if fi.length==0
     */
    private JExpression createStrongTest( Expression branch, FieldItem fi[] ) {
        JExpression expr = JExpr.TRUE;
        for( int i=0; i<fi.length; i++ ) {
            FieldRenderer fr = context.getMarshaller(fi[i]).owner();
            
            Multiplicity m =
                FieldMultiplicityCounter.count( branch, fi[i] );
            
            JExpression e=JExpr.TRUE, f=JExpr.TRUE;
            
            if(m.max!=null && m.min==m.max.intValue()) {
                // if v == item#
                e = fr.ifCountEqual(m.min);
            } else {
                // [RESULT]
                // min <= item# && item# <= max
                if( m.min!=0 )
                    e = fr.ifCountGte(m.min);
                
                if(m.max!=null)
                    f = fr.ifCountLte(m.max.intValue());
            }
            
            expr = expr.cand(e).cand(f);
        }
        
        return expr;
    }
}
