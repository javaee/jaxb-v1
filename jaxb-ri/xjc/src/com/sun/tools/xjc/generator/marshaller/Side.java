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