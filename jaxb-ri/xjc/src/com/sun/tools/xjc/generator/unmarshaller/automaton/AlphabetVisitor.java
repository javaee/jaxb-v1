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
 * Visitor for alphabets.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface AlphabetVisitor {
    void onEnterElement(EnterElement a);
    void onLeaveElement(LeaveElement a);
    void onEnterAttribute(EnterAttribute a);
    void onLeaveAttribute(LeaveAttribute a);
    void onInterleave(Interleave a);
    void onChild(Child a);
    void onSuper(SuperClass a);
    void onDispatch(Alphabet.Dispatch a);
    void onExternal(External a);
    void onBoundText(BoundText a);
    void onIgnoredText(IgnoredText a);
    void onEverythingElse(EverythingElse a);
}
