/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader.relaxng;

import com.sun.msv.grammar.AttributeExp;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.SimpleNameClass;
import com.sun.msv.reader.State;
import com.sun.msv.util.StartTagInfo;
import com.sun.tools.xjc.grammar.ClassCandidateItem;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.reader.NameConverter;
import com.sun.tools.xjc.reader.decorator.DecoratorImpl;
import com.sun.tools.xjc.util.CodeModelClassFactory;

/**
 * Performs the default annotation for RELAX NG.
 * 
 * @author
 *    <a href="mailto:kohsuke.kawaguchi@sun.com">Kohsuke KAWAGUCHI</a>
 */
class DefaultDecorator extends DecoratorImpl
{
    DefaultDecorator( TRELAXNGReader reader, NameConverter nc ) {
        super(reader,reader.annGrammar,nc);
    }
    
    
    private CodeModelClassFactory getClassFactory() {
        return ((TRELAXNGReader)reader).classFactory;
    }
    
    public Expression decorate( State state, Expression exp ) {
        StartTagInfo tag = state.getStartTag();
        final TRELAXNGReader reader = (TRELAXNGReader)this.reader;
        
        if( tag.localName.equals("define") ) {
            if((exp==Expression.nullSet || exp==Expression.epsilon)
            && state.getStartTag().containsAttribute("combine") )
                // if <define>s are combined and combined ones are empty/notAllowed,
                // then just ignore them.
                return exp;
        }
        
        // insert an ClassItem if this is the <element> tag or the <define> tag.
        // some of those temporarily added ClassItems will be removed
        // in the final wrap up.
        if( (tag.localName.equals("element") || tag.localName.equals("define"))
        &&  !(exp instanceof ClassItem) && !(exp instanceof ClassCandidateItem) ) {
            // to make marshalling easy, ClassItem is generated above the ElementExp
            // calculate the unique name to avoid name conflicts.
            String baseName = decideName(state,exp,"class","",state.getLocation());
            // TODO: name collision check
            
            return new ClassCandidateItem(
                getClassFactory(),
                grammar,
                reader.packageManager.getCurrentPackage(),
                baseName,
                state.getLocation(),
                exp );
        }
        
        if( exp instanceof AttributeExp && !(((AttributeExp)exp).nameClass instanceof SimpleNameClass) ) {
            // if the name class is not simple, treat it as a class
            return grammar.createClassItem(
                getClassFactory().createInterface(
                    reader.packageManager.getCurrentPackage(),
                    decideName(state,exp,"class","Attr",state.getLocation()),
                    state.getLocation()),
                exp, state.getLocation() );
        }
        
        return exp;
    }
}
