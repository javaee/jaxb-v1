/*
 * @(#)$Id: LookupTableBuilder.java,v 1.1 2004-06-25 21:14:13 kohsuke Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.generator;

import com.sun.msv.grammar.ChoiceExp;

/**
 * Obtains a look up table for a switch attribute (such as xsi:type) 
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface LookupTableBuilder {
    /**
     * @return null
     *      if the table look up on a switch attribute doesn't
     *      benefit the given choice.
     */
    LookupTableUse buildTable( ChoiceExp exp );
}
