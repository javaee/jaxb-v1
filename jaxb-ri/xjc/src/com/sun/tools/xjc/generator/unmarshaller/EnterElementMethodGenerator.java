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
