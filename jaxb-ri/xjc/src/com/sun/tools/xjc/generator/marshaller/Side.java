/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.generator.marshaller;
import com.sun.msv.grammar.ChoiceExp;
import com.sun.msv.grammar.Expression;
import com.sun.tools.xjc.grammar.FieldItem;
/**
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
interface Side {
    
    void onChoice(ChoiceExp exp);
    
    /**
     * Generates a marshaller in a loop.
     * 
     * This is made as a separate method so that we can reuse it
     * for ZeroOrMore, which is represented as choice(epsilon,zeroOrMore(...)).
     * 
     * @param itemExp
     *      expression to be repeated.
     */
    void onZeroOrMore(Expression itemExp);
    
    /**
     * This method is called when a ClassItem/InterfaceItem is found.
     */
    void onMarshallableObject();
    
    void onField(FieldItem item);
}