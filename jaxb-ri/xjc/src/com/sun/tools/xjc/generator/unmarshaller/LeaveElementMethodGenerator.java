/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.generator.unmarshaller;

import com.sun.codemodel.JBlock;
import com.sun.tools.xjc.generator.unmarshaller.automaton.*;

/**
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
class LeaveElementMethodGenerator extends EnterLeaveMethodGenerator {
    LeaveElementMethodGenerator( PerClassGenerator parent ) {
        super(parent,"leaveElement",Alphabet.LeaveElement.class);
    }
    
    protected void generateAction( Alphabet alpha, Transition tr, JBlock body ) {
        if( tr.alphabet==alpha )
            body.invoke(parent.$context,"popAttributes");
    }
}
