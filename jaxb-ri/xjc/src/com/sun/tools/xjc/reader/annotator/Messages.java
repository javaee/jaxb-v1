/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.xjc.reader.annotator;

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
    static final String ENUM_FACET_UNSUPPORTED = // 0 args
        "DatatypeSimplifier.EnumFacetUnsupported";

    static final String PATTERN_FACET_UNSUPPORTED = // 0 args
        "DatatypeSimplifier.PatternFacetUnsupported";
        
    static final String ERR_MULTIPLE_SUPERCLASS_BODY = // 0 args
        "Normalizer.MultipleSuperClassBody";

    static final String ERR_MULTIPLE_INHERITANCE = // 1 args
        "Normalizer.MultipleInheritance";

    public static final String ERR_BAD_SUPERCLASS_USE = // arg:0
        "Normalizer.BadSuperClassUse";

    public static final String ERR_BAD_ITEM_USE = // arg:0
        "Normalizer.BadItemUse";

    public static final String ERR_MISSING_SUPERCLASS_BODY = // arg:0
        "Normalizer.MissingSuperClassBody";

    public static final String ERR_BAD_SUPERCLASS_MULTIPLICITY  = // arg:1
        "Normalizer.BadSuperClassMultiplicity";

    public static final String ERR_BAD_SUPERCLASS_BODY_MULTIPLICITY = // arg:1
        "Normalizer.BadSuperClassBodyMultiplicity";

    public static final String ERR_BAD_INTERFACE_CLASS_MULTIPLICITY = // arg:1
        "Normalizer.BadInterfaceToClassMultiplicity";

    public static final String ERR_CONFLICT_BETWEEN_USERTYPE_AND_ACTUALTYPE = //arg:3
        "Normalizer.ConflictBetweenUserTypeAndActualType";
    
    public static final String ERR_DELEGATION_MULTIPLICITY_MUST_BE_1 = // arg:1
        "Normalizer.DelegationMultiplicityMustBe1";
           
    public static final String ERR_DELEGATION_MUST_BE_INTERFACE = // arg:1
        "Normalizer.DelegationMustBeInterface";

    public static final String ERR_EMPTY_PROPERTY = // arg:1
        "Normalizer.EmptyProperty";

    static final String ERR_PROPERTYNAME_COLLISION =
        "FieldCollisionChecker.PropertyNameCollision"; // arg:1

    static final String ERR_PROPERTYNAME_COLLISION_SOURCE =
        "FieldCollisionChecker.PropertyNameCollision.Source"; // arg:0

    static final String ERR_RESERVEDWORD_COLLISION =
        "FieldCollisionChecker.ReservedWordCollision"; // arg:1
}
