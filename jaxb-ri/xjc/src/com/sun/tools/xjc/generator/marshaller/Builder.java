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

import org.xml.sax.SAXException;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JExpr;
import com.sun.msv.grammar.AttributeExp;
import com.sun.msv.grammar.ChoiceExp;
import com.sun.msv.grammar.ConcurExp;
import com.sun.msv.grammar.DataExp;
import com.sun.msv.grammar.ElementExp;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.MixedExp;
import com.sun.msv.grammar.OneOrMoreExp;
import com.sun.msv.grammar.OtherExp;
import com.sun.msv.grammar.ValueExp;
import com.sun.msv.grammar.xmlschema.OccurrenceExp;
import com.sun.tools.xjc.grammar.BGMWalker;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.ExternalItem;
import com.sun.tools.xjc.grammar.FieldItem;
import com.sun.tools.xjc.grammar.IgnoreItem;
import com.sun.tools.xjc.grammar.InterfaceItem;
import com.sun.tools.xjc.grammar.PrimitiveItem;
import com.sun.tools.xjc.grammar.SuperClassItem;
import com.sun.xml.bind.JAXBAssertionError;

/**
 * Walks BGM and dispatches nodes to appropriate side or pass. 
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
final class Builder extends BGMWalker {
    
    private final Context context;
    
    
    protected Builder( Context _context ) {
        this.context = _context;
    }

    public final void onChoice( ChoiceExp exp ) {
        // optimize for zero-or-more.
        // without this optimization, XJC generates
        // if(AAA) {
        //     do {
        //         ....
        //     } while(AAA);
        // }
        // which looks pretty stupid.
        
        if(exp.exp1==Expression.epsilon && exp.exp2 instanceof OneOrMoreExp) {
            onOneOrMore( (OneOrMoreExp)exp.exp2 );
            return;
        }
        if(exp.exp2==Expression.epsilon && exp.exp1 instanceof OneOrMoreExp) {
            onOneOrMore( (OneOrMoreExp)exp.exp1 );
            return;
        }
        
        context.currentSide.onChoice(exp);
    }
    
    
    
    public final void onOneOrMore( OneOrMoreExp exp ) {
        _onOneOrMore( exp.exp );
    }
    
    private void _onOneOrMore( Expression itemExp ) {
        // let's be generous and treat one or more as zero or more,
        // so that some degree of invalidness is allowed.
        //
        // this allows us to always use while(...) { ... }
        // regardless of <oneOrMore> or <zeroOrMore>.
        
        final boolean oldOOM = context.inOneOrMore;
        context.inOneOrMore = true;

        context.currentSide.onZeroOrMore( itemExp );

        context.inOneOrMore = oldOOM;
    }

    
    public final void onNullSet() {
        // this can happen only when schema contains <rng:notAllowed/> or
        // empty <xsd:choice/>. This is a very corner case, so allow me
        // to hard code an error message in the generated code.
        getBlock(true)._throw(
            JExpr._new(context.codeModel.ref(SAXException.class))
            .arg(JExpr.lit("this object doesn't have any XML representation"))
        );
    }
    
    public Object onIgnore( IgnoreItem exp ) {
        // TODO: we might need a special pass/builder for the ignored part,
        // in which we try to minimize the generated tree.
        // if we do so, we need three additional passes:
        // one for attBody, another for att, and the other for
        // element.
        
        // for now, we do some ad-hoc optimizations ...
        if( exp.exp.isEpsilonReducible() )
            return null;    // return value unused
         
        // and otherwise we'll substitute it by the current pass.
        exp.exp.visit(this);
        return null;    // return value unused
    }

    // these methods can never be called,
    // because those had to be removed from BGM
    public void onConcur(ConcurExp exp) { throw new JAXBAssertionError(); }
    public void onMixed(MixedExp exp)   { throw new JAXBAssertionError(); }
    public void onData(DataExp exp)     { throw new JAXBAssertionError(); }
    
    /*
        We see bare-naked anyString only if we are visiting inside
        an IgnoreItem. In this case, our goal is to produce something
        that satisifes the schema constraint. Hence it's OK to output
        and empty text, which is still a legal value for anyString.
     */
    public void onAnyString() {
        // TODO: assert if we are really inside an IgnoreItem.
        
        // do nothing. This is equal to writing an empty string.
        ;
    }

    /*
        Same situation as in the onAnyString method. However,
        now we are dealing with a bit more difficult problem,
        since we need to marshal actual value sometime.
     */
    public final void onValue(ValueExp exp) {
        context.currentPass.onValue(exp);
    }

    
    public Object onSuper( SuperClassItem exp ) {
        if( context.currentPass==context.skipPass ) {
            return null;
        }
        
        // [RESULT]
        // super.serialize<marshalMethod>($context);
        getBlock(true).invoke(JExpr._super(),
            "serialize"+context.currentPass.getName())
            .arg(context.$serializer);
        return null;    // return value unused
    }
    
    
    public void onAttribute(AttributeExp exp) {
        context.currentPass.onAttribute(exp);
    }

    public void onElement(ElementExp exp) {
        context.currentPass.onElement(exp);
    }
    
    public final Object onInterface( InterfaceItem exp ) {
        context.currentSide.onMarshallableObject();
        return null;    // return value unused
    }
    
    public final Object onClass( ClassItem exp ) {
        context.currentSide.onMarshallableObject();
        return null;    // return value unused
    }
    
    public Object onField(FieldItem item) {
        context.currentSide.onField(item);
        return null;
    }

    public final Object onExternal(ExternalItem item) {
        context.currentPass.onExternal(item);
        return null;
    }

    public final Object onPrimitive(PrimitiveItem item) {
        // wrap the conversion method by a try-catch block so that
        // exceptions will be caught and handled.
        context.pushNewBlock(new PrintExceptionTryCatchBlockReference(context));
        context.currentPass.onPrimitive(item);
        context.popBlock();
        return null;
    }
    
    public void onOther(OtherExp exp) {
        if( exp instanceof OccurrenceExp )
            onOccurence( (OccurrenceExp)exp );
        else
            super.onOther(exp);
    }
    
    public void onOccurence( OccurrenceExp exp ) {
//        System.err.println("!"+exp.toString());     // eureka!
        _onOneOrMore( exp.itemExp );
    }

    
    
    
    
    
    
    /**
     * Code should be generated into the returned block.  
     */
    protected final JBlock getBlock(boolean create) {
        return context.getCurrentBlock().get(create);
    }

}
