/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
