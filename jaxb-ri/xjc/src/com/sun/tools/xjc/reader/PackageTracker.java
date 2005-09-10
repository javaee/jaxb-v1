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
