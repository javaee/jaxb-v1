/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.xjc.reader;

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
    
    
    static final String ERR_UNDECLARED_PREFIX =
        "ExtensionBindingChecker.UndeclaredPrefix"; // arg:1

    static final String ERR_UNEXPECTED_EXTENSION_BINDING_PREFIXES =
        "ExtensionBindingChecker.UnexpectedExtensionBindingPrefixes";

    static final String ERR_UNSUPPORTED_EXTENSION =
        "ExtensionBindingChecker.UnsupportedExtension"; // arg:1

    static final String ERR_SUPPORTED_EXTENSION_IGNORED =
        "ExtensionBindingChecker.SupportedExtensionIgnored"; // arg:1
    
    static final String ERR_RELEVANT_LOCATION = // 0 args
        "GrammarReaderControllerAdaptor.RelevantLocation";

    static final String ERR_CLASS_NOT_FOUND =
        "TypeUtil.ClassNotFound";  // arg:1
}