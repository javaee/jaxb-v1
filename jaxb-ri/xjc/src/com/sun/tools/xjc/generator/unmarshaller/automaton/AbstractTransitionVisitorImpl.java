/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
