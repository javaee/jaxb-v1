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

import org.xml.sax.Locator;

import com.sun.msv.grammar.Expression;
import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.reader.NameConverter;
import com.sun.tools.xjc.reader.PackageTracker;

/**
 * Receives errors that are found during the annotation process.
 * 
 * @author
 *    <a href="mailto:kohsuke.kawaguchi@sun.com">Kohsuke KAWAGUCHI</a>
 */
public interface AnnotatorController
{
    /**
     * Gets the name converter which is used to convert
     * various XML tokens into Java identifiers.
     */
    NameConverter getNameConverter();
    
    /** Used to associate ReferenceExps to a Java package. */
    PackageTracker getPackageTracker();
    
    /**
     * Reports an error.
     * 
     * @param    locations
     *        Expressions that caused the error. Can be null, or an empty array.
     */
    void reportError( Expression[] locations, String msg );
    
    /** Reports an error. */
    void reportError( Locator[] locations, String msg );
    
    /**
     * Errors should be reported through this interface.
     */
    ErrorReceiver getErrorReceiver(); 
}
