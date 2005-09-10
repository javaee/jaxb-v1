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

import java.io.PrintStream;
import java.util.Iterator;

public class AutomatonPrinter
{
    public static void print( Automaton a, PrintStream out ) {
        
        Iterator itr = a.states();
        while(itr.hasNext()) {
            State s = (State)itr.next();
            
            out.print("#"+a.getStateNumber(s)+" :=");
            
            boolean first = true;
            Iterator jtr = s.transitions();
            while(jtr.hasNext()) {
                Transition t = (Transition)jtr.next();
                
                if(!first)   out.print(" |");
                first=false;
                
                out.print(" "+t.alphabet+" #"+a.getStateNumber(t.to));
            }
            
            if(s.isFinalState()) {
                if(!first)   out.print(" |");
                out.print(" *final*");
            }
            
            out.println();
        }
    }
}
