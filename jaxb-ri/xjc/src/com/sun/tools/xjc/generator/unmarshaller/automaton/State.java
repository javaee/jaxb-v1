/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
