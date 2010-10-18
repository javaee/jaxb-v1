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

package com.sun.tools.xjc.generator.unmarshaller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.sun.tools.xjc.generator.unmarshaller.automaton.*;

/**
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
class TransitionTable {
    
    static class Entry {
        /** When the input matches this alphabet, */
        final Alphabet alphabet;
        /** Then take this transition. */
        final Transition transition;
        
        private Entry(Alphabet _alphabet, Transition _transition) {
            this.alphabet = _alphabet;
            this.transition = _transition;
        }
    }
    
    /**
     * Builds a transition from a given automaton.
     */
    TransitionTable( Automaton a ) {
        Iterator itr = a.states();
        while(itr.hasNext()) {
            State state = (State)itr.next();
            
            // sort them in the order of alphabets
            TreeMap tm = new TreeMap(OrderComparator.theInstance);
            for( Iterator jtr = state.transitions(); jtr.hasNext(); ) {
                Transition t = (Transition)jtr.next();
                tm.put( t.alphabet, t );
            }

            ArrayList r = new ArrayList();
            
            for( Iterator jtr=tm.entrySet().iterator(); jtr.hasNext(); ) {
                Map.Entry e = (Map.Entry)jtr.next();
                buildList( r, (Alphabet)e.getKey(), (Transition)e.getValue() ); 
            }

            // if a state is a final state, the default EE action is
            // to revert to the parent
            if(state.isFinalState())
                r.add( new Entry(
                    Alphabet.EverythingElse.theInstance,
                    Transition.REVERT_TO_PARENT) );
            
            // remove conflicting transitions
            // TODO: we could issue a warning for conflicts
            Set alphabetsSeen = new HashSet();
            for( int i=0; i<r.size(); ) {
                if( !alphabetsSeen.add( ((Entry)r.get(i)).alphabet ) ) {
                    // this alphabet is already in the list
                    r.remove(i);
                } else
                    i++;    // OK. advance
            }
            
            table.put( state, (Entry[]) r.toArray(new Entry[r.size()]));
        }
    }
            
    /** compute the head set and fill the transition table */
    private void buildList(ArrayList r, Alphabet alphabet, Transition transition) {
        if( alphabet.isReference() ) {
            Iterator itr = alphabet.asReference().head(true).iterator();
            while( itr.hasNext() )
                buildList( r, (Alphabet)itr.next(), transition );
        } else {
            r.add( new Entry( alphabet, transition ) );
        }
    }
    
    private final Map table = new HashMap();
    
    /**
     * Lists all entries of the transition table
     * in the order they need to be checked.
     */
    public Entry[] list( State s ) {
        Entry[] r = (Entry[])table.get(s);
        if(r==null)
            return empty;
        else
            return r;
    }
    
//    /**
//     * Gets a particular cell entry.
//     * 
//     * @return
//     *      null if the cell is empty.
//     */
//    public Transition get( State s, Alphabet alphabet ) {
//        Map m = (Map)table.get(s);
//        if(m==null)
//            return null;
//        else
//            return (Transition)m.get(alphabet);
//    }
    
    private static final Entry[] empty = new Entry[0];    
//    private static final Map emptyMap = new HashMap();
//    private static final Iterator emptyIterator = new Iterator() {
//        public boolean hasNext() { return false; }
//        public Object next() { return null; }
//        public void remove() { throw new UnsupportedOperationException(); }
//    };
}
