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
import com.sun.tools.xjc.generator.XmlNameStoreAlgorithm;
import com.sun.tools.xjc.generator.unmarshaller.automaton.*;

/**
 * 
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
class EnterAttributeMethodGenerator extends EnterLeaveMethodGenerator {
    public EnterAttributeMethodGenerator(PerClassGenerator parent) {
        super( parent, "enterAttribute", Alphabet.EnterAttribute.class );
    }

    protected void generateAction( Alphabet alpha, Transition tr, JBlock $body ) {
        if( tr.alphabet==alpha ) {
            Alphabet.EnterAttribute ea = (Alphabet.EnterAttribute)alpha;
            
            // if this is the consumption of this attribute, store it.
            XmlNameStoreAlgorithm.get(ea.name).onNameUnmarshalled(
                codeModel, $body, $uri, $local );
        }
    }
}
