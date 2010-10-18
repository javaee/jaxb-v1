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

package com.sun.tools.xjc.generator.unmarshaller.automaton;

import com.sun.tools.xjc.generator.unmarshaller.automaton.Alphabet.BoundText;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Alphabet.Child;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Alphabet.EnterAttribute;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Alphabet.EnterElement;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Alphabet.EverythingElse;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Alphabet.External;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Alphabet.IgnoredText;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Alphabet.Interleave;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Alphabet.LeaveAttribute;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Alphabet.LeaveElement;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Alphabet.SuperClass;

/**
 * Partial default implementation of {@link TransitionVisitor}.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class AbstractTransitionVisitorImpl implements TransitionVisitor {

    public void onEnterElement(EnterElement a, State to) {
        onNamed(a,to);
    }

    public void onLeaveElement(LeaveElement a, State to) {
        onNamed(a,to);
    }

    public void onEnterAttribute(EnterAttribute a, State to) {
        onNamed(a,to);
    }

    public void onLeaveAttribute(LeaveAttribute a, State to) {
        onNamed(a,to);
    }
    
    protected void onNamed( Alphabet.Named a, State to ) {
        onAlphabet(a,to);
    }

    public void onInterleave(Interleave a, State to) {
        onRef(a,to);
    }

    public void onChild(Child a, State to) {
        onRef(a,to);
    }

    public void onDispatch(Alphabet.Dispatch a, State to) {
        onAlphabet(a,to);
    }

    public void onSuper(SuperClass a, State to) {
        onRef(a,to);
    }

    public void onExternal(External a, State to) {
        onRef(a,to);
    }
    
    protected void onRef( Alphabet.Reference a, State to ) {
        onAlphabet(a,to);
    }

    public void onBoundText(BoundText a, State to) {
        onText(a,to);
    }

    public void onIgnoredText(IgnoredText a, State to) {
        onText(a,to);
    }
    
    protected void onText( Alphabet.Text a, State to ) {
        onAlphabet(a,to);
    }

    public void onEverythingElse(EverythingElse a, State to) {
        onAlphabet(a,to);
    }

    protected void onAlphabet(Alphabet a, State to) {
        ;
    }

}
