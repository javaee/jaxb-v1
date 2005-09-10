/*
 * @(#)$Id: FieldCollisionChecker.java,v 1.4 2004/01/06 00:17:46 kk122374 Exp
 $
 */

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
package com.sun.tools.xjc.reader.annotator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xml.sax.Locator;

import com.sun.msv.grammar.BinaryExp;
import com.sun.msv.grammar.ChoiceExp;
import com.sun.msv.grammar.InterleaveExp;
import com.sun.msv.grammar.OtherExp;
import com.sun.msv.grammar.SequenceExp;
import com.sun.msv.grammar.xmlschema.OccurrenceExp;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.BGMWalker;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.FieldItem;
import com.sun.tools.xjc.grammar.SuperClassItem;
import com.sun.tools.xjc.util.*;

/**
 * Looks for field name collisions.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class FieldCollisionChecker extends BGMWalker {
    
    public static void check( AnnotatedGrammar grammar, AnnotatorController controller ) {
        FieldCollisionChecker checker = new FieldCollisionChecker(controller);
        
        // a part of a collision check is a check between fields of a derived class
        // and those of a base class.
        // Thus it is suffice to do this for every non-base class C
        // (IOW, if C is not used as the base class of another class, then
        // C has to be checked)
        Set baseClasses = new HashSet();
        ClassItem[] cls = grammar.getClasses();
        for( int i=0; i<cls.length; i++ )
            baseClasses.add( cls[i].getSuperClass() );
        
        for( int i=0; i<cls.length; i++ ) {
            if( baseClasses.contains(cls[i]) )  continue;
            checker.reset();
            cls[i].visit(checker);
        }
    }
    
    private FieldCollisionChecker(AnnotatorController _controller) {
        this.controller = _controller;
    }
    
    private final AnnotatorController controller;
    
    /**
     * {@link FieldItem}s will be stored in the order
     * they are found.
     * 
     * The list must work accumulatively; IOW, list
     * may not be cleared, and items cannot be removed,
     * so that sub-lists of this list can be added to
     * {@link #class2fields}
     */
    private List fields = new ArrayList();
    
    /**
     * Once a list of {@link FieldItem}s is created for a {@link ClassItem},
     * it is stored here.
     * 
     * <p>
     * This allows us to avoid visiting the same class multiple times.
     */
    private final Map class2fields = new HashMap();
    
    /**
     * Remembers the fields appeared inside the last {@link SuperClassItem}.
     */
    private int sl,sr;
    
    private void reset() {
        fields = new ArrayList();
        sl = sr = -1;
    }
    
    public void onInterleave(InterleaveExp exp) {
        check(exp);
    }

    public void onSequence(SequenceExp exp) {
        check(exp);
    }
    
    private void check(BinaryExp exp) {
        int l = fields.size();
        exp.exp1.visit(this);
        int r = fields.size();
        exp.exp2.visit(this);
        
        compare(l,r,r,fields.size());
    }

    public void onChoice(ChoiceExp exp) {
        int l = fields.size();
        exp.exp1.visit(this);
        int r = fields.size();
        exp.exp2.visit(this);
        
        if( l<=sl && sr<=r )
            // if super class item appears in the left branch,
            // compare them with the right branch
            compare( sl,sr, r,fields.size() );
        else
        if( r<=sl && sr<=fields.size() )
            // if it appears in the right branch,
            // compare them with the left branch
            compare( l,r, sl,sr );
    }
    
    public Object onSuper(SuperClassItem sci) {
        sl = fields.size();
        
        // we know that there's nothing substancial between SuperClassItem
        // and its child ClassItem, so we can just skip nodes in between. 
        sci.definition.visit(this);
        
        sr = fields.size();
        
        return null;    // unused
    }
    
    public Object onField(FieldItem item) {
        fields.add(item);
        
        // test reserved name.
        if( item.name.equals("Class") )
            error(item.locator, Messages.ERR_RESERVEDWORD_COLLISION, item.name);
        
        return null;    // unused
    }
    
    public Object onClass(ClassItem item) {
        List subList = (List)class2fields.get(item);
        if( subList==null ) {
            // this is the first time this class is processed.
            int s = fields.size();
            super.onClass(item);
            int e = fields.size();
            
            // remember the accumulated fields
            class2fields.put(item,new SubList(fields,s,e));
        } else {
            // instead of visiting the children, just fill in from the
            // previously computed result
            fields.addAll(subList);
        }
        return null;    // unused
    }
    
    public void onOther(OtherExp exp) {
        if(exp instanceof OccurrenceExp) {
            ((OccurrenceExp)exp).itemExp.visit(this);
        } else
            super.onOther(exp);
    }
    
    /**
     * Checks the name collisions between fields of [ls,le) and [rs,re).
     */
    private void compare( int ls, int le, int rs, int re ) {
        for( int l=ls; l<le; l++ ) {
            FieldItem left = (FieldItem)fields.get(l);
            
            for( int r=rs; r<re; r++ ) {
                FieldItem right = (FieldItem)fields.get(r);
                
                if( left.name.equals(right.name)
                && (!left.collisionExpected || !right.collisionExpected)) {
                    
                    Locator locator;
                    if (left.locator != null)   locator = left.locator;
                    else                        locator = right.locator;
                    
                    error(locator, Messages.ERR_PROPERTYNAME_COLLISION, left.name);
                    
                    if( left.locator!=null && right.locator!=null ) {
                        error(right.locator, Messages.ERR_PROPERTYNAME_COLLISION_SOURCE,
                              left.name);
                    }
                }
            }
        }
    }
    
    private void error( Locator loc, String prop, Object arg ) {
        controller.getErrorReceiver().error(loc,Messages.format(prop,arg));
    }
}
