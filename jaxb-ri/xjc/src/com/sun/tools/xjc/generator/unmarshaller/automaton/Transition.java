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
