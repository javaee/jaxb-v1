/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
