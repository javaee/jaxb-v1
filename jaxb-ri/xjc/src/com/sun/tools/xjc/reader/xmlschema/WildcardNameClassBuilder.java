/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader.xmlschema;

import java.util.Iterator;

import com.sun.msv.grammar.ChoiceNameClass;
import com.sun.msv.grammar.DifferenceNameClass;
import com.sun.msv.grammar.NameClass;
import com.sun.msv.grammar.NamespaceNameClass;
import com.sun.xml.bind.JAXBAssertionError;
import com.sun.xml.xsom.XSWildcard;
import com.sun.xml.xsom.visitor.XSWildcardFunction;

/**
 * Builds a name class representation of a wildcard.
 * 
 * <p>
 * Singleton. Use the build method to create a NameClass.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public final class WildcardNameClassBuilder implements XSWildcardFunction {
    private WildcardNameClassBuilder() {}
    
    private static final XSWildcardFunction theInstance =
        new WildcardNameClassBuilder();
    
    public static NameClass build( XSWildcard wc ) {
        return (NameClass)wc.apply(theInstance);
    }
    
    public Object any(XSWildcard.Any wc) {
        return NameClass.ALL;
    }

    public Object other(XSWildcard.Other wc) {
        return new DifferenceNameClass(
            NameClass.ALL,
            new ChoiceNameClass(
                new NamespaceNameClass(""),
                new NamespaceNameClass(wc.getOtherNamespace())));
    }

    public Object union(XSWildcard.Union wc) {
        NameClass nc = null;
        for (Iterator itr = wc.iterateNamespaces(); itr.hasNext();) {
            String ns = (String) itr.next();
            
            if(nc==null)    nc = new NamespaceNameClass(ns);
            else
                nc = new ChoiceNameClass(nc,new NamespaceNameClass(ns));
        }
        
        if(nc==null)
            // there should be at least one.
            throw new JAXBAssertionError();
        
        return nc;
    }

}
