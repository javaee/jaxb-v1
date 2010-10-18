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
