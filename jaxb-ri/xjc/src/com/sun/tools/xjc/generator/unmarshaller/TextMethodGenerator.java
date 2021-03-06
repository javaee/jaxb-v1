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

package com.sun.tools.xjc.generator.unmarshaller;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.StringWriter;

import org.relaxng.datatype.Datatype;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCatchBlock;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JSwitch;
import com.sun.codemodel.JTryBlock;
import com.sun.codemodel.JVar;
import com.sun.msv.datatype.DatabindableDatatype;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Alphabet;
import com.sun.tools.xjc.generator.unmarshaller.automaton.State;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Transition;
import com.sun.tools.xjc.generator.validator.StringOutputStream;
import com.sun.tools.xjc.runtime.ValidationContextAdaptor;
import com.sun.xml.bind.unmarshaller.DatatypeDeserializer;

/**
 * Generates the "text" method that handles text events.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
class TextMethodGenerator extends HandlerMethodGenerator {
    
    private JVar $value;
    
    TextMethodGenerator( PerClassGenerator parent) {
        super(parent,"text",Alphabet.Text.class);
    }
    
    
    private boolean needsGuard( State state ) {
        // when we have more than one transitions by text.
        // we need to generate something like
        // if( the text matches datatype X ) {
        //     unmarshal something;
        //     return;
        // }
        // if( the text matches datatype Y ) {
        //     unmarshal something;
        //     return;
        // }
        
        int count=0;
        TransitionTable.Entry[] e = table.list(state);
        for( int i=0; i<e.length; i++ )
            if( e[i].alphabet.isText() )
                count++;
        return count>1;
    }
    


    /**
     * Generates an expression that evaluates to true only if
     * The text pointed by $value variable can be accepted by
     * this {@link Alphabet.Text} alphabet.
     */
    private JExpression guardClause( Alphabet a ) {
        if(a instanceof Alphabet.IgnoredText
        || a instanceof Alphabet.SuperClass
        || a instanceof Alphabet.Child
        || a instanceof Alphabet.EverythingElse)
            // ignored text can accept anything
            return JExpr.TRUE;
        
        // this datatype validates the string
        _assert( a instanceof Alphabet.BoundText );
        DatabindableDatatype guard = ((Alphabet.BoundText)a).item.guard;

        // "freeze dry" a datatype into a string
        StringWriter sw = new StringWriter();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new StringOutputStream(sw));
            oos.writeObject(guard);
            oos.close();
        } catch( IOException e ) {
            e.printStackTrace();
            throw new InternalError("unserializable datatype:"+guard);
        }
        
        // add the following static datatype declaration.
        // ideally we'd like to add them to the unmarshaller itself,
        // but we can't have a static field inside non-static nested class.
        // [RESULT] private static final Datatype dt<NNN> = 
        //              DatatypeDeserializer.deserialize("....");
        JVar $dt = parent.context.implClass.field(JMod.PRIVATE|JMod.STATIC|JMod.FINAL,
            Datatype.class,
            "___dt"+(datatypeId++),
            codeModel.ref(DatatypeDeserializer.class).staticInvoke("deserialize")
                .arg( JExpr.lit(sw.getBuffer().toString()) )
        );

        JExpression con;
        if( guard.isContextDependent() )
            // wrap it into ValidationContext.
            con = JExpr._new( 
                parent.parent.context.getRuntime(ValidationContextAdaptor.class))
                .arg(parent.$context);
        else
            con = JExpr._null();
        
        return $dt.invoke("isValid").arg($value).arg(con);
    }
    
    /** Used to assign an unique name to the freeze-dry datatype. */
    private int datatypeId = 0;
    

    protected boolean performTransition(State state, Alphabet alphabet, Transition action ) {
        
        JBlock block = getCase(state);
        
        boolean needsGuard = needsGuard(state);
        
        if( needsGuard )
            block = block._if(guardClause(alphabet))._then();
                
        if(action==Transition.REVERT_TO_PARENT) {
            generateRevertToParent(block);
            return needsGuard;
        }
        if(action.alphabet instanceof Alphabet.Reference) {
            generateSpawnChild(block,action);
            return needsGuard;
        }
        
        // [RESULT]
        //     <goto state N>;
        //     return;
        generateGoto(block,action.to);
        
        if(action.alphabet instanceof Alphabet.BoundText) {
            // eat the text after changing the state so that
            // we can recover from errors more gracefully.
            parent.eatText( block, action.alphabet.asBoundText(), $value );
        }
        
        block._return();
        return needsGuard;
    }
    
    protected String getNameOfMethodDecl() {
        return "handleText";
    }

    protected JSwitch makeSwitch( JMethod method, JBlock body ) {
        // it needs to be final for it could be accessed from an anonymous class
        $value = method.param( JMod.FINAL, String.class, "value" );
            
        if(trace) {
            // [RESULT] tracer.onText(value);
            body.invoke( $tracer, "onText" ).arg($value);
        }
        
        // wrap the entire switch-case statement with a try-catch block
        // so that we can catch any conversion failure
        
        JTryBlock tryBlock = body._try();
        JSwitch s = super.makeSwitch( method, tryBlock.body() );
        // [RESULT]
        // } catch ( RuntimeException e ) {
        //     handleUnexpectedTextException(value,e);
        // }
        JCatchBlock c = tryBlock._catch( codeModel.ref(RuntimeException.class) );
        JVar $e = c.param("e");
        c.body().invoke("handleUnexpectedTextException")
                    .arg($value).arg($e);
  
                // because of the way we handle texts, sometimes
        // unnecessary text events can be fired. For example, when
        // the schema is
        // <element name="foo"><empty/></element>
        // and the document is
        // <foo>      </foo>
        // then the runtime just fires a text event with "".
        // so we need to ignore them.
/*
                // [RESULT]
                //     super.text(value);
                body.invoke( JExpr.ref("super"), "text" )
                    .arg($value);
*/                
        return s;
    }
         
    protected void addParametersToContextSwitch( JInvocation inv ) {
        inv.arg($value);
    }
}
