/*
 * @(#)$Id: PreProcessingLineFilter.java,v 1.1 2004-06-25 21:14:15 kohsuke Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
