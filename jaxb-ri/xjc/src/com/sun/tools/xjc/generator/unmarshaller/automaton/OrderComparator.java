/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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

