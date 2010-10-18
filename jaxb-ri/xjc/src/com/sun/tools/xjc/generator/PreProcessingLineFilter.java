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

package com.sun.tools.xjc.generator;

import java.text.ParseException;
import java.util.Stack;

import com.sun.codemodel.fmt.JStaticJavaFile;

/**
 * {@link JStaticJavaFile.LineFilter} that works like a C pre-processor.
 * 
 * <p>
 * This pre-processor looks for the lines like the following:
 * <pre>
 *   ..1..   // META-IF(x|y|z)
 *   ..2..
 *   ..3..   // META-ELSE
 *   ..4..
 *   ..5..   // META-ENDIF
 * </pre>
 * <p>
 * if x or y or z is true, the above will be expanded to:
 * <pre>
 *   ..2..
 * </pre>
 * otherwise it will be:
 * <pre>
 *   ..4..
 * </pre>
 * 
 * <p>
 * This class needs to be overrided to fix the variable assignments.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public abstract class PreProcessingLineFilter implements JStaticJavaFile.LineFilter {

    /**
     * Boolean stack that remembers the results of the evaluation
     * of the in-scope META-IF statements.
     */
    private final Stack conditions = new Stack(); 

    /**
     * Token that signals meta statements.
     */
    private static final String META_TOKEN = "// META-";
    
    /**
     * Returns true if the current line should be included.
     */
    private boolean isOn() {
        for( int i=conditions.size()-1; i>=0; i-- )
            if( !((Boolean)conditions.get(i)).booleanValue() )
                return false;
        return true;
    }
    
    public String process(String line) throws ParseException {
        // look for the pre-processing statements.
        int idx = line.indexOf(META_TOKEN);
        if(idx<0) {
            // normal line
            if(isOn())  return line;
            else        return null;
        }
        
        String cond = line.substring(idx+META_TOKEN.length()).trim();
        if(cond.startsWith("IF(")) {
            // META-IF statement.
            
            idx = cond.indexOf(')');
            if(idx<0)   throw new ParseException("Unable to parse "+cond,-1);
            String exp = cond.substring(3,idx);
            
            conditions.push(eval(exp)?Boolean.TRUE:Boolean.FALSE);
            return null;
        }
        if(cond.equals("ELSE")) {
            // META-ELSE statement
            Boolean b = (Boolean)conditions.pop();
            conditions.push(!b.booleanValue()?Boolean.TRUE:Boolean.FALSE);
            return null;
        }
        if(cond.equals("ENDIF")) {
            // META-ENDIF
            conditions.pop();
            return null;
        }
        
        throw new ParseException(
            "unrecognized meta statement "+line,-1);
    }

    /**
     * Simple boolean expression evaluator.
     * 
     * @param exp
     *      Expression to be evaluated. Strings like "A|B". 
     * @return
     *      the result of the expression evaluation. 
     */
    private boolean eval(String exp) throws ParseException {
        boolean r = getVar(exp.charAt(0));
        int i=1;
        if(i<exp.length()) {
            char op = exp.charAt(i++);
            if(i==exp.length())
                throw new ParseException("Unable to parse "+exp,-1);
            boolean rhs = getVar(exp.charAt(i++));
            switch(op) {
            case '|':   r|=rhs;break;
            case '&':   r&=rhs;break;
            default:
                throw new ParseException("Unable to parse"+exp,-1);
            }
        }
        return r;
    }
    
    /**
     * Returns the value of the variable.
     */
    protected abstract boolean getVar( char variableName ) throws ParseException;
}
