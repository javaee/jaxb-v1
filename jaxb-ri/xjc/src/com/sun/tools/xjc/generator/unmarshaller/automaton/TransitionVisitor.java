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
 * Visitor for {@link Transition}.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface TransitionVisitor {
    void onEnterElement(EnterElement a, State to);
    void onLeaveElement(LeaveElement a, State to);
    void onEnterAttribute(EnterAttribute a, State to);
    void onLeaveAttribute(LeaveAttribute a, State to);
    void onInterleave(Interleave a, State to);
    void onChild(Child a, State to);
    void onDispatch(Alphabet.Dispatch a, State to);
    void onSuper(SuperClass a, State to);
    void onExternal(External a, State to);
    void onBoundText(BoundText a, State to);
    void onIgnoredText(IgnoredText a, State to);
    void onEverythingElse(EverythingElse a, State to);
}
