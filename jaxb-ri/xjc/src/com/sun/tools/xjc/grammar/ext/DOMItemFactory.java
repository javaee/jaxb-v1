/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.grammar.ext;

import org.xml.sax.Locator;

import com.sun.msv.grammar.NameClass;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.ExternalItem;

/**
 * 
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public abstract class DOMItemFactory {
    public abstract ExternalItem create(NameClass _elementName, AnnotatedGrammar grammar, Locator loc);
    
    /**
     * Obtains an instance of {@link DOMItemFactory} from a name.
     * 
     * @exception IllegalArgumentException
     *      if the type value is incorrect.
     */
    public static DOMItemFactory getInstance( String type ) throws UndefinedNameException {
        type = type.toUpperCase();
                
        if( type.equals("W3C") )
            return W3CDOMItem.factory;
        if( type.equals("DOM4J") )
            return Dom4jItem.factory;
        
        throw new UndefinedNameException(type);
    }
    
    public static class UndefinedNameException extends Exception {
        UndefinedNameException( String typeName ) {
            super( "DOM type " + typeName + " is not recognized ");
        }
    }
}
