/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.xjc.reader.decorator;

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
    
//
//
// Message resources
//
//
// TODO: add more arguments to produce user-friendly messages.
    
    
    static final String ERR_INVALID_COLLECTION_TYPE = // arg:1
        "InvalidCollectionType";

    static final String ERR_INVALID_ACCESS_MODIFIER = // arg:1
        "InvalidAccessModifier";

    static final String ERR_INVALID_ACCESSOR = // arg:1
        "InvalidAccessor";

    static final String ERR_UNDEFINED_ROLE = // arg:1
        "UndefinedRole"; 

    static final String ERR_CLASS_NOT_FOUND = // arg:1
        "ClassNotFound";
        
    static final String ERR_NAME_NEEDED = // arg:0
        "NameNeeded";   
        
}
