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

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.Iterator;


/**
 * Converts an automaton into gif file by using
 * <a href="http://www.research.att.com/sw/tools/graphviz/">GraphViz</a>.
 * 
 * 
 * @author
 *    <a href="mailto:kohsuke.kawaguchi@sun.com">Kohsuke KAWAGUCHI</a>
 */
public class AutomatonToGraphViz
{
    private static final PrintStream debug = null; /* or System.err */;
    
    /** Gets the display name of a state. */
    private static String getStateName( Automaton a, State s ) {
        return "\"s"+a.getStateNumber(s)+(s.isListState?"*":"")+'"';
    }
    
    /** Gets the hue of the color for an alphabet. */
    private static String getColor( Alphabet a ) {
        // H S V
        if(a instanceof Alphabet.EnterElement)      return "0";
        if(a instanceof Alphabet.EnterAttribute)    return "0.125";
        if(a instanceof Alphabet.LeaveAttribute)    return "0.25";
        if(a instanceof Alphabet.LeaveElement)      return "0.375";
        if(a instanceof Alphabet.Child)             return "0.5";
        if(a instanceof Alphabet.SuperClass)        return "0.625";
        if(a instanceof Alphabet.External)          return "0.625";
        if(a instanceof Alphabet.Dispatch)          return "0.625";
        if(a instanceof Alphabet.EverythingElse)    return "0.625";
        if(a instanceof Alphabet.Text)              return "0.75";
        if(a instanceof Alphabet.Interleave)        return "0.875";
        throw new InternalError(a.getClass().getName());
    }
    
    /**
     * Produces a nice picture of a specified automaton into 
     * a specified file.
     */
    public static void convert( Automaton a, File target ) throws IOException, InterruptedException {
        
        System.err.println("generating a graph to "+target.getPath());
        
//        Process proc = Runtime.getRuntime().exec("dot");
        Process proc = Runtime.getRuntime().exec(
            new String[]{"dot","-Tgif","-o",target.getPath()});
        PrintWriter out = new PrintWriter(
            new BufferedOutputStream(proc.getOutputStream()));
    
        out.println("digraph G {");
        out.println("node [shape=\"circle\"];");

        Iterator itr = a.states();
        while( itr.hasNext() ) {
            State s = (State)itr.next();
            if(s.isFinalState())
                out.println(getStateName(a,s)+" [shape=\"doublecircle\"];");
            
            if( s.getDelegatedState()!=null ) {
                out.println(MessageFormat.format("{0} -> {1} [style=dotted];",
                    new Object[]{
                        getStateName(a,s),
                        getStateName(a,s.getDelegatedState()) }));
            }
            
            Iterator jtr = s.transitions();
            while(jtr.hasNext() ) {
                Transition t = (Transition)jtr.next();
                
                String str = MessageFormat.format(
                        "{0} -> {1} [ label=\"{2}\",color=\"{3} 1 .5\",fontcolor=\"{3} 1 .3\" ];",
                        new Object[]{
                            getStateName(a,s),
                            getStateName(a,t.to),
                            getAlphabetName(a,t.alphabet),
                            getColor(t.alphabet) });
                out.println(str);
                if(debug!=null) debug.println(str);
            }
        }
        
        out.println("}");
        out.flush();
        out.close();
        
        BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        while(true) {
            String s = in.readLine();
            if(s==null)     break;
            System.out.println(s);
        }
        in.close();
        
        proc.waitFor();
        
        
    }

    private static String getAlphabetName(Automaton a, Alphabet alphabet) {
        String s = alphabet.toString();
        if( alphabet instanceof Alphabet.Interleave ) {
            s += " ->";
            Alphabet.Interleave ia = (Alphabet.Interleave)alphabet;
            for( int i=0; i<ia.branches.length; i++ )
                s += " "+ a.getStateNumber(ia.branches[i].initialState);
        }
        return s;
    }
}
