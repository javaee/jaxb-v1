/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader;

import com.sun.codemodel.JPackage;
import com.sun.msv.grammar.ReferenceExp;

/**
 * Maintains association between packages and grammar expressions.
 * 
 * To generate enumeration classes into appropriate directories,
 * we need to be able to tell which package it should belong to.
 * However, since {@link Expression}s are shared and reused, this is
 * tricky to do.
 * 
 * Fortunately, {@link ReferenceExp} is not shared, and this can be
 * uniquely associated with a package. Any other expressions that
 * hang from a ReferenceExp can be considered as in the same package.
 * (since any declaration starts with a ReferenceExp.)
 * 
 * This property holds for all currently supported schema languages.
 * 
 * @author
 *    <a href="mailto:kohsuke.kawaguchi@sun.com">Kohsuke KAWAGUCHI</a>
 */
public interface PackageTracker
{
    /**
     * Gets the JPackage object which is associated to the specified ReferenceExp.
     * returns null if associateion is not found.
     */
    public JPackage get( ReferenceExp exp );
}
