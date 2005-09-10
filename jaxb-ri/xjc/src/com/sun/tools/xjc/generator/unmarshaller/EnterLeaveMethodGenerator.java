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
package com.sun.tools.xjc.generator.unmarshaller;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JSwitch;
import com.sun.codemodel.JVar;
import com.sun.msv.grammar.NameClass;
import com.sun.tools.xjc.generator.unmarshaller.automaton.*;

/**
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
class EnterLeaveMethodGenerator extends HandlerMethodGenerator {
    
    protected JVar $uri,$local,$qname;
    
    EnterLeaveMethodGenerator( PerClassGenerator parent, String _methodName, Class _alphabetType ) {
        super(parent,_methodName,_alphabetType);
    }
    

    protected void generateAction( Alphabet alpha, Transition tr, JBlock $body ) {
        // can be overrided by the derived class to do something extra.
    }
    
    
    protected boolean performTransition(
        State state, Alphabet alphabet, Transition action ) {
        
        // [RESULT]
        // if( <name matches> ) {
        //     <associated action>;
        //     <perform transition>;
        //     return;
        // }
        JBlock $body = getCase(state);
        
        if( alphabet.isNamed() )
            $body = $body._if(generateNameClassTest(alphabet.asNamed().name))._then();
        
        generateAction(alphabet,action,$body);
        
        if(action==Transition.REVERT_TO_PARENT)
            generateRevertToParent($body);
        else
        if(action.alphabet instanceof Alphabet.Reference)
            generateSpawnChild($body,action);
        else {
            generateGoto($body,action.to);
            $body._return();
        }
        
        return alphabet.isNamed();
    }

    protected JSwitch makeSwitch( JMethod method, JBlock body ) {
        
        declareParameters(method);
        
        if(trace) {
            // [RESULT] tracer.on<event>(uri,local) ");
            body.invoke( $tracer, "on"+capitalize() )
                .arg($uri).arg($local);
        }
                
        // generate the switch case statement
        JSwitch s = super.makeSwitch(method,body);
        
        // [RESULT]
        //     super.<method>(uri,local);
        addParametersToContextSwitch(
            body.invoke( JExpr.ref("super"), method ));
        
        return s;
    }
    
    
    
    protected void declareParameters( JMethod method ) {
        $uri   = method.param(String.class,"___uri");
        $local = method.param(String.class,"___local");
        $qname = method.param(String.class,"___qname");
    }
    
    protected void addParametersToContextSwitch( JInvocation inv ) {
        inv.arg($uri).arg($local).arg($qname);
    }
    
    /**
     * Generates an expression that returns true if
     * ($uri,$local) is accepted by the 'nc'.
     */
    private JExpression generateNameClassTest( NameClass nc ) {
        
        // UGLY CODE: we need to call getSwitch method because
        // sometimes the event handler is still not created at all,
        // and if that is the case, $uri and $local are null.
        // calling the getSwitch method creates the empty event handler.
        getSwitch();
        
        return parent.parent.generateNameClassTest( nc, $uri, $local );
    }
}
