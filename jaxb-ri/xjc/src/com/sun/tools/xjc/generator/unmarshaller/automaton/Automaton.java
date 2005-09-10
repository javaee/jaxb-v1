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

import java.util.Iterator;
import java.util.Map;

import com.sun.msv.grammar.ExpressionPool;
import com.sun.tools.xjc.generator.ClassContext;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Alphabet.Interleave;
import com.sun.xml.bind.JAXBAssertionError;

public final class Automaton
{
    private final ClassContext owner;
    
    /** The initial state of the automaton. */    
    private State initial;

    /**
     * If the automaton accepts the empty input, this will be set to true,
     * otherwise false.
     */
    private Boolean nullable = null;

    /** Map from State to Integer (state #). */
    private final Map states = new java.util.HashMap();
    private int iota = 0;
    
        
    /**
     * Creates an empty automaton. The body shall be added later
     * via the setInitialState method.
     * 
     * @param _owner
     *      The ClassContext object for which this automaton is created.
     */
    public Automaton( ClassContext _owner ) {
        this.owner = _owner;
    }
    
    /**
     * Attached the constructed automaton to this object.
     */
    public void setInitialState( State _initialState ) {
        if(this.initial!=null)
            // assertion failure. this method can't be called twice.
            throw new JAXBAssertionError();
            
        this.initial = _initialState;
        
        // enumerate all states
        new StateEnumerator().visit(initial);
//        visitState(initial);
    }
    
    /** Visits all the reachable states and records them. */ 
    private class StateEnumerator extends AbstractTransitionVisitorImpl {
        protected void visit( State s ) {
            if( s==null || states.containsKey(s) )     return;
            states.put(s,new Integer(iota++));
            
            s.acceptForEachTransition(this);
            visit(s.getDelegatedState());
        }
        
        protected void onAlphabet(Alphabet a, State to) {
            visit(to);
        }

        public void onInterleave(Interleave a, State to) {
            for( int i=0; i<a.branches.length; i++ )
                visit(a.branches[i].initialState);
            visit(to);
        }
    }
    
    /** Gets the start state of the automaton. */
    public State getInitialState() { return initial; }
    
    public int getStateNumber( State s ) {
        return ((Integer)states.get(s)).intValue();
    }
    
    /** Gets the number of states. */
    public int getStateSize() { return states.size(); }
    
    /** Iterates all states in this automaton. */
    public Iterator states() { return states.keySet().iterator(); }
    
    /** Getsthe ClassContext object for which this automaton is created. */
    public ClassContext getOwner() { return owner; }
    
    
    /** Returns true if this automaton can accept the empty sequence. */
    public boolean isNullable() {
        if(nullable==null) {
            // we need to expand the expression to strip away all ReferenceExps.
            ExpressionPool pool = new ExpressionPool();
            if(owner.target.exp.getExpandedExp(pool).isEpsilonReducible())
                nullable = Boolean.TRUE;
            else
                nullable = Boolean.FALSE;
        }
        
        return nullable.booleanValue();
    }
}
