/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.xjc.reader.dtd;

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
    

    public static final String ERR_NO_ROOT_ELEMENT = // arg:0
        "TDTDReader.NoRootElement";

    public static final String ERR_UNDEFINED_ELEMENT_IN_BINDINFO = // arg:1
        "TDTDReader.UndefinedElementInBindInfo";

    public static final String ERR_CONVERSION_FOR_NON_VALUE_ELEMENT = // arg:1
        "TDTDReader.ConversionForNonValueElement";

    public static final String ERR_CONTENT_PROPERTY_PARTICLE_MISMATCH = // arg:1
        "TDTDReader.ContentProperty.ParticleMismatch";

    public static final String ERR_CONTENT_PROPERTY_DECLARATION_TOO_SHORT = // arg:1
        "TDTDReader.ContentProperty.DeclarationTooShort";
    
    public static final String ERR_BINDINFO_NON_EXISTENT_ELEMENT_DECLARATION = // arg:1
        "TDTDReader.BindInfo.NonExistentElementDeclaration";

    public static final String ERR_BINDINFO_NON_EXISTENT_INTERFACE_MEMBER = // arg:1
        "TDTDReader.BindInfo.NonExistentInterfaceMember";
        
}
