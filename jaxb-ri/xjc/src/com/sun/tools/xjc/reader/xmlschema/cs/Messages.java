/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.xjc.reader.xmlschema.cs;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * Formats error messages.
 */
class Messages
{
    static String format( String property ) {
        return format( property, null );
    }
    
    static String format( String property, Object arg1 ) {
        return format( property, new Object[]{arg1} );
    }
    
    static String format( String property, Object arg1, Object arg2 ) {
        return format( property, new Object[]{arg1,arg2} );
    }
    
    static String format( String property, Object arg1, Object arg2, Object arg3 ) {
        return format( property, new Object[]{arg1,arg2,arg3} );
    }
    
    // add more if necessary.
    
    /** Loads a string resource and formats it with specified arguments. */
    static String format( String property, Object[] args ) {
        String text = ResourceBundle.getBundle(Messages.class.getName()).getString(property);
        return MessageFormat.format(text,args);
    }
    

    static final String ERR_ABSTRACT_COMPLEX_TYPE =
        "ClassSelector.AbstractComplexType"; // arg:1

    static final String ERR_ABSTRACT_COMPLEX_TYPE_SOURCE =
        "ClassSelector.AbstractComplexType.SourceLocation"; // arg:0

    static final String JAVADOC_HEADING = // 1 arg
        "ClassSelector.JavadocHeading";
        
    static final String JAVADOC_LINE_UNKNOWN = // 0 args
        "ClassSelector.JavadocLineUnknown";
    
    static final String ERR_RESERVED_CLASS_NAME = // 1 arg
        "ClassSelector.ReservedClassName";
    
    static final String ERR_CLASS_NAME_IS_REQUIRED =
        "ClassSelector.ClassNameIsRequired";    // arg:0
    
    static final String ERR_INCORRECT_CLASS_NAME =
        "ClassSelector.IncorrectClassName";     // arg:1
    
    static final String ERR_INCORRECT_PACKAGE_NAME =
        "ClassSelector.IncorrectPackageName";   // arg:2
        
    static final String ERR_UNABLE_TO_GENERATE_NAME_FROM_MODELGROUP =
        "DefaultParticleBinder.UnableToGenerateNameFromModelGroup"; // arg:0

}
