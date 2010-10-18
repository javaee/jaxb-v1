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

import org.xml.sax.Attributes;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.generator.XmlNameStoreAlgorithm;
import com.sun.tools.xjc.generator.unmarshaller.automaton.*;

/**
 * Generates the enterElement callback method to an unmarshaller.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
class EnterElementMethodGenerator extends EnterLeaveMethodGenerator {
    EnterElementMethodGenerator( PerClassGenerator parent ) {
        super(parent,"enterElement",Alphabet.EnterElement.class);
    }
    
    /** additional argument unique to this method. */
    private JVar $atts;
    
    /**
     * If this transition is in fact a consumption of an
     * enterElement event, push the attributes to the context.
     */
    protected void generateAction( Alphabet alpha, Transition tr, JBlock body ) {
        if( tr.alphabet==alpha ) {
            Alphabet.EnterElement ee = (Alphabet.EnterElement)alpha;
        
            XmlNameStoreAlgorithm.get(ee.name).onNameUnmarshalled(
                codeModel, body, $uri, $local );
            body.invoke(parent.$context,"pushAttributes")
                .arg($atts).arg(ee.isDataElement?JExpr.TRUE:JExpr.FALSE);
        }
    }

    protected void declareParameters( JMethod method ) {
        super.declareParameters(method);
        $atts = method.param( Attributes.class, "__atts");
    }

    protected void addParametersToContextSwitch( JInvocation inv ) {
        super.addParametersToContextSwitch(inv);
        inv.arg($atts);
    }


    protected void generateSpawnChildFromExternal(
        JBlock $body, Transition tr, JExpression memento ) {
            
        if(trace) {
            // TODO: ideally we should rename "onSpawnWildcard" to
            // "onSpawnExternal", but I just didn't feel like breaking
            // a backward compatibility just because of this change.
            
            // [RESULT]
            // tracer.onSpawnWildcard()\n");
            // tracer.suspend();
            $body.invoke( $tracer, "onSpawnWildcard" );
            $body.invoke( $tracer, "suspend" );
        }
        
        Alphabet.External ae = (Alphabet.External)tr.alphabet;
        
        JExpression co = ae.owner.generateUnmarshaller(
            parent.parent.context, parent.$context,
            $body,memento,$uri,$local,$qname,$atts);
        
        JBlock then = $body._if( co.ne(JExpr._null()) )._then();
        
        ae.field.setter( then, co );
            
        $body._return();
    }
}
