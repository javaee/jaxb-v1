/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.grammar.util;

import com.sun.msv.grammar.Expression;

/**
 * represents a possible number of occurence.
 * 
 * Usually, denoted by a pair of integers like (1,1) or (5,10).
 * A special value "unbounded" is allowed as the upper bound.
 * 
 * <p>
 * For example, (0,unbounded) corresponds to the '*' occurence of DTD.
 * (0,1) corresponds to the '?' occurence of DTD.
 * 
 * @author
 *    <a href="mailto:kohsuke.kawaguchi@sun.com">Kohsuke KAWAGUCHI</a>
 */
public final class Multiplicity {
    public final int min;
    public final Integer max;    // null is used to represent "unbounded".
            
    public Multiplicity( int min, Integer max ) {
        this.min = min; this.max = max;
    }
    public Multiplicity( int min, int max ) {
        this.min = min; this.max = new Integer(max);
    }
    
    /** returns true if the multiplicity is (1,1). */
    public boolean isUnique() {
        if(max==null)    return false;
        return min==1 && max.intValue()==1;
    }
    
        /** returns true if the multiplicity is (0,1) */
        public boolean isOptional() {
            if(max==null) return false;
            return min==0 && max.intValue()==1;
        }
        
    /** returns true if the multiplicity is (0,1) or (1,1). */
    public boolean isAtMostOnce() {
        if(max==null)    return false;
        return max.intValue()<=1;
    }
    
    /** returns true if the multiplicity is (0,0). */
    public boolean isZero() {
        if(max==null)    return false;
        return max.intValue()==0;
    }
    
    /**
     * Returns true if the multiplicity represented by this object
     * completely includes the multiplicity represented by the
     * other object. For example, we say [1,3] includes [1,2] but
     * [2,4] doesn't include [1,3].
     */
    public boolean includes( Multiplicity rhs ) {
        if( rhs.min<min )   return false;
        if( max==null )     return true;
        if( rhs.max==null ) return false;
        return rhs.max.intValue() <= max.intValue();
    }
    
    /**
     * Returns the string representation of the 'max' property.
     * Either a number or a token "unbounded".
     */
    public String getMaxString() {
        if(max==null)       return "unbounded";
        else                return max.toString();
    }
    
    /** gets the string representation.
     * mainly debug purpose.
     */
    public String toString() {
        return "("+min+","+getMaxString()+")";
    }
    
    /** the constant representing the (0,0) multiplicity. */
    public static final Multiplicity zero = new Multiplicity(0,0);
    
    /** the constant representing the (1,1) multiplicity. */
    public static final Multiplicity one = new Multiplicity(1,1);
    
    /** the constant representing the (0,unbounded) multiplicity. */
    public static final Multiplicity star = new Multiplicity(0,null);

    public static Multiplicity calc( Expression exp, MultiplicityCounter calc ) {
        return (Multiplicity)exp.visit(calc);
    }

// arithmetic methods
//============================
    public static Multiplicity choice( Multiplicity lhs, Multiplicity rhs ) {
        return new Multiplicity(
            Math.min(lhs.min,rhs.min),
            (lhs.max==null||rhs.max==null)?
                null:
                new Integer(Math.max(lhs.max.intValue(),rhs.max.intValue())) );
    }
    public static Multiplicity group( Multiplicity lhs, Multiplicity rhs ) {
        return new Multiplicity( lhs.min+rhs.min,
            (lhs.max==null||rhs.max==null)?
                null:
                new Integer(lhs.max.intValue()+rhs.max.intValue()) );
    }
    public static Multiplicity oneOrMore( Multiplicity c ) {
        if(c.max==null)                return c; // (x,*) => (x,*)
        if(c.max.intValue()==0 )    return c; // (0,0) => (0,0)
        else        return new Multiplicity( c.min, null );    // (x,y) => (x,*)
    }
}
    
