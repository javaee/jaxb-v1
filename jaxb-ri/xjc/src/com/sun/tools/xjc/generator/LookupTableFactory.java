/*
 * @(#)$Id: LookupTableFactory.java,v 1.1 2004-06-25 21:14:14 kohsuke Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.generator;

import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;
import com.sun.msv.grammar.AttributeExp;
import com.sun.msv.grammar.ChoiceExp;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.SequenceExp;
import com.sun.msv.grammar.SimpleNameClass;
import com.sun.msv.grammar.ValueExp;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.JavaItem;
import com.sun.xml.bind.JAXBAssertionError;

/**
 * Tries to create a fresh look-up table for each {@link ChoiceExp}.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
class LookupTableFactory implements LookupTableBuilder {
    
    private static class Branch {
        final SimpleNameClass attName;
        final ValueExp value;
        final ClassItem body;
        
        private Branch(SimpleNameClass _attName, ValueExp _value, ClassItem _body) {
            this.attName = _attName;
            this.value = _value;
            this.body = _body;
        }
        
        private static boolean agree( Branch lhs, Branch rhs ) {
            if( lhs==null || rhs==null )    return false;
            return lhs.attName.namespaceURI.equals(rhs.attName.namespaceURI)
                && lhs.attName.localName.equals(rhs.attName.localName);
        }
        
        public LookupTable.Entry toEntry() {
            return new LookupTable.Entry(body, value );
        }
        
        static Branch create( Expression exp ) {
            try {
                SequenceExp sexp = (SequenceExp)exp;
                
                AttributeExp att = (AttributeExp)sexp.exp1;
                SimpleNameClass name = (SimpleNameClass)att.nameClass;
                ValueExp value;
                
                if( att.exp instanceof ValueExp )
                    value = (ValueExp)att.exp;
                else
                    value = (ValueExp)((JavaItem)att.exp).exp;
                
                return new Branch( name, value, (ClassItem)sexp.exp2 );
            } catch( ClassCastException e ) {
                return null; 
            }
        }
    };
    
    /**
     * Once the necessity of a table class is recognized,
     * a class will be created and set to this field.
     */
    private JDefinedClass tableClass;
    
    /**
     * Table class should go into this package.
     */
    private final JPackage pkg;
    
    /**
     * Used to assign unique IDs to each table.
     */
    private int id=0;
    
    
    public LookupTableFactory(JPackage _pkg) {
        this.pkg = _pkg;
    }
    
    JDefinedClass getTableClass() {
        if( tableClass==null ) {
            try {
                tableClass = pkg._class(JMod.PUBLIC,"Table");
            } catch( JClassAlreadyExistsException e ) {
                throw new JAXBAssertionError();  // assertion failure
            }
        }
        
        return tableClass;
    }
    
    /**
     * @return null
     *      if the table look up on a switch attribute doesn't
     *      benefit the given choice.
     */
    public LookupTableUse buildTable( ChoiceExp exp ) {
        // TODO: need to return the att name to look for
        // TODO: how to handle "if the att is missing, do this..." 
        
        Expression[] children = exp.getChildren();
        if(children.length<3)   return null;
        
        int nullBranchCount=0;
        Branch[] branches = new Branch[children.length];
        
        for( int i=0; i<children.length; i++ ) {
            if( (branches[i] = Branch.create(children[i]))==null )
                nullBranchCount++;
        }
        
        // if there's more than one non-valid branch, abort.
        if( nullBranchCount>1 ) return null;
        
        // determine the dominant group and the anomalty
        Branch dominant; int anomaly=-1;
        
        if( Branch.agree(branches[0],branches[1]) ) {
            dominant = branches[0];
        } else {
            if( Branch.agree(branches[0],branches[2]) ) {
                dominant = branches[0];
                anomaly = 1;
            } else
            if( Branch.agree(branches[1],branches[2]) ) {
                dominant = branches[1];
                anomaly = 0;
            } else
                return null;    // three different branches.
        }
        
        for( int i=2; i<branches.length; i++ ) {
            if( !Branch.agree(dominant,branches[i]) ) {
                if( anomaly!=-1 )
                    return null;    // too many anomalies.
                anomaly = i;
            }
        }
        
        if( anomaly!=-1 )
            branches[anomaly] = null;   // delete the anomaly
        
        LookupTable t = new LookupTable(this,id++);
        for( int i=0; i<branches.length; i++ ) {
            if(branches[i]==null)   continue;
            
            LookupTable.Entry e = branches[i].toEntry();
            if( !t.isConsistentWith(e) )
                return null;    // it is not  self-consitent. abort.
            
            t.add(e);
        }
        
        return new LookupTableUse( t,
            anomaly==-1?null:children[anomaly],
            dominant.attName );
    }
}
