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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * A state in automata.
 * 
 * Invariant:
 *      if delegateState!=null then
 *          isFinalState => delegatedState.isFinalState
 */
public final class State
{
    /**
     * If true, text transition from this state must be
     * processed as a list.
     */
    public boolean isListState = false;
    
    /**
     * If true this state is a final state.
     */
    private boolean isFinalState = false;
    
    private final Set transitions = new HashSet();
    
    private State delegatedState;
    


    public State getDelegatedState() {
        return delegatedState;
    }

    /**
     * Sets the delegatedState.
     */
    public void setDelegatedState(State _delegatedState) {
        // make sure this doesn't form a cycle
        State s = _delegatedState;
        while(s!=null) {
            if(s==this) {
                absorb(_delegatedState);
                return;
            }
            s = s.delegatedState;
        }
        
        // if this is a final state but the delegated state is not,
        // we can't make delegation
        if( isFinalState && !_delegatedState.isFinalState ) {
            absorb(_delegatedState);
            return;
        }
        
        
        if( this.delegatedState==null ) {
            this.delegatedState = _delegatedState;
            this.isListState |= delegatedState.isListState;
        } else {
            // we already have a delegated state.
            // can't have two delegated states.
            absorb(_delegatedState);
        }
    }
    
    public void addTransition( Transition t ) {
        transitions.add(t);
    }
    
    public Iterator transitions() { return transitions.iterator(); }
    
    public Transition[] listTransitions() {
        return (Transition[]) transitions.toArray(new Transition[transitions.size()]);
    }
    
    /**
     * Lets the visitor visit all the transitions leaving from this state. 
     */
    public void acceptForEachTransition( TransitionVisitor visitor ) {
        for( Iterator itr=transitions.iterator(); itr.hasNext(); )
            ((Transition)itr.next()).accept(visitor);
    }
    
//    private class TypeIterator extends FilterIterator {
//        TypeIterator( Iterator core, Class _type ) {
//            super(core);
//            this.type=_type;
//        }
//        
//        private final Class type;
//        protected boolean test(Object o) {
//            return type.isInstance( ((Transition)o).alphabet );
//        }
//    }
    
//    public Iterator refTransitions() {
//        return new TypeIterator(transitions(),Alphabet.Reference.class);
//    }
//    
//    public Iterator enterElementTransitions() {
//        return new TypeIterator(transitions(),Alphabet.EnterElement.class);
//    }
//    
//    public Iterator leaveElementTransitions() {
//        return new TypeIterator(transitions(),Alphabet.LeaveElement.class);
//    }
//    
//    public Iterator enterAttributeTransitions() {
//        return new TypeIterator(transitions(),Alphabet.EnterAttribute.class);
//    }
//    
//    public Iterator textTransitions() {
//        return new TypeIterator(transitions(),Alphabet.Text.class);
//    }
    
    /**
     * Absorbs the specified state into this state.
     * <p>
     * Copies all the transitions, etc.
     */
    public void absorb( State rhs ) {
        this.transitions.addAll( rhs.transitions );
        this.isListState |= rhs.isListState;
        if( rhs.isFinalState )
            markAsFinalState();
        
        if( rhs.delegatedState!=null )
            setDelegatedState(rhs.delegatedState);
    }


    public Set head() {
        HashSet s = new HashSet();
        head(s,new HashSet(),true);
        return s;
    }

    /**
     * Computes the HEAD alphabet set of this state
     * and adds them to the result set.
     */
    void head( Set result, Set visitedStates, boolean includeEE ) {
        
        if( !visitedStates.add(this) ) {
            // we've already visited this state
            return;
        }
        
        if( isFinalState && includeEE )
            result.add(Alphabet.EverythingElse.theInstance);
        
        for (Iterator itr = transitions.iterator(); itr.hasNext();) {
            Transition t = (Transition) itr.next();
            t.head(result,visitedStates,includeEE);
        }
        
        if( delegatedState!=null )
            delegatedState.head( result, visitedStates, includeEE );
    }
    
    /**
     * Returns true if this state has any transition at all.
     */
    public boolean hasTransition() {
       return !transitions.isEmpty(); 
    }
    

    public boolean isFinalState() {
        return isFinalState;
    }

    public void markAsFinalState() {
        isFinalState = true;
        
        // if this is a final state but the delegated state is not,
        // we can't make a delegation
        if( delegatedState!=null && !delegatedState.isFinalState ) {
            State p = delegatedState;
            delegatedState = null;
            absorb(p);
        }
    }

}
