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

import java.util.Comparator;

/**
 * Compares two alphabets
 * 
 * <p>
 * When used with a TreeSet, this comparator will sort a set
 * in descending order of the order number (i.e., bigger number first.)
 */
public final class OrderComparator implements Comparator {
    
    /** Sole instance of this class (singleton pattern.) */
    public final static Comparator theInstance = new OrderComparator();

    // use the static instance instead of creating a new instance.
    private OrderComparator() {}
    public int compare(Object o1, Object o2) {
        Alphabet a1 = (Alphabet)o1;
        Alphabet a2 = (Alphabet)o2;
        
        return a2.order-a1.order;
    }

}

