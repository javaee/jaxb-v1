/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
