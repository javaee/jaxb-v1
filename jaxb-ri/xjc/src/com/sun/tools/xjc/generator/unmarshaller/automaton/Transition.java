/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.xjc.generator.unmarshaller.automaton;

import java.util.HashSet;
import java.util.Set;

public final class Transition
{
    /** Special transition that represents "revertToParent" action. */
    public static final Transition REVERT_TO_PARENT = new Transition(null,null);

    public final Alphabet alphabet;
    
    /** Destination state. */
    public final State to;
    
    public Transition( Alphabet _alphabet, State _to ) {
        this.alphabet = _alphabet;
        this.to = _to;
    }
    
    /**
     * Gets the HEAD alphabet set of this transition.
     * 
     * @param sourceState
     *      The source state of this transition. A {@link Transition}
     *      object could be shared across multiple states, but this
     *      information is necessary to correctly detect a nullable
     *      cycle.
     */
    public Set head( State sourceState ) {
        HashSet s = new HashSet();
        
        HashSet visited = new HashSet();
        visited.add(sourceState);
        
        head(s,visited,true);
        return s;
    }
    
    /**
     * Computes the HEAD alphabet set of this transition
     * and adds them to the result set.
     */
    void head( Set result, Set visitedStates, boolean includeEE ) {

        result.add(alphabet);
        
        if(!(alphabet instanceof Alphabet.Reference)) {
            // this is not nullable, so it's simple
            return;
        }

        Alphabet.Reference ref = alphabet.asReference();
        
        if(ref.isNullable())
            to.head( result, visitedStates, includeEE );
    }
    
    public void accept( TransitionVisitor visitor ) {
        alphabet.accept(visitor,this);
    }
}
