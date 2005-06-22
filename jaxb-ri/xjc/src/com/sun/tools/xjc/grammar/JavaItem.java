/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.grammar;

import java.util.List;
import java.util.ArrayList;

import org.xml.sax.Locator;

import com.sun.msv.grammar.OtherExp;

/**
 * the base class of all special OtherExps
 * that are used to annotate tahiti data-binding information
 * to AGM.
 * 
 * @author
 *    <a href="mailto:kohsuke.kawaguchi@sun.com">Kohsuke KAWAGUCHI</a>
 */
public abstract class JavaItem extends OtherExp {
    public JavaItem( String name, Locator loc ) {
        this.name = name;
        this.locator = loc;
    }
    
    public String name;
    
    public abstract Object visitJI( JavaItemVisitor visitor );

    /**
     * The source location information that points the position
     * where this field was defined.
     * 
     * This field can be null if the location information is unavailable
     * for some reason.
     */
    public final Locator locator;

    public final List declarations = new ArrayList();
}
