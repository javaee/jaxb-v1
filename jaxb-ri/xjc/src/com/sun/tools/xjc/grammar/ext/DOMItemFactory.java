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
