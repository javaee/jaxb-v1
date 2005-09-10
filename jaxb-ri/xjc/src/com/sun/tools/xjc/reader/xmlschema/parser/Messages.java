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

package com.sun.tools.xjc.reader.xmlschema.parser;

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
    
    
    
    static final String ERR_UNACKNOWLEDGED_CUSTOMIZATION =
        "CustomizationContextChecker.UnacknolwedgedCustomization"; // arg:1

    static final String WARN_INCORRECT_URI = // 1 args
        "IncorrectNamespaceURIChecker.WarnIncorrectURI";

    static final String STRICT_MODE_PREFIX = // 0 args
        "ProhibitedFeaturesFilter.StrictModePrefix";

    static final String ERROR_PREFIX = // 0 args
        "ProhibitedFeaturesFilter.ErrorPrefix";
        
    static final String WARNING_PREFIX = // 0 args
        "ProhibitedFeaturesFilter.WarningPrefix";
        
    static final String UNSUPPORTED_PREFIX = // 0 args
        "ProhibitedFeaturesFilter.UnsupportedPrefix";
        
    static final String PROCESSCONTENTS_ATTR_OF_ANY = // 0 args
        "ProhibitedFeaturesFilter.ProcessContentsAttrOfAny";
        
    static final String ANY_ATTR = // 0 args
        "ProhibitedFeaturesFilter.AnyAttr";
        
    static final String ANY_ATTR_WARNING = // 0 args
        "ProhibitedFeaturesFilter.AnyAttrWarning";
        
    static final String BLOCK_ATTR_OF_COMPLEXTYPE = // 0 args
        "ProhibitedFeaturesFilter.BlockAttrOfComplexType";
        
    static final String ABSTRACT_ATTR_OR_ELEMENT = // 0 args
        "ProhibitedFeaturesFilter.AbstractAttrOfElement";
        
    static final String SUBSTITUTIONGROUP_ATTR_OF_ELEMENT = // 0 args
        "ProhibitedFeaturesFilter.SubstitutionGroupAttrOfElement";
        
    static final String BLOCK_ATTR_OF_ELEMENT = // 0 args
        "ProhibitedFeaturesFilter.BlockAttrOfElement";
        
    static final String KEY = // 0 args
        "ProhibitedFeaturesFilter.Key";
        
    static final String KEY_WARNING = // 0 args
        "ProhibitedFeaturesFilter.KeyWarning";
        
    static final String KEYREF = // 0 args
        "ProhibitedFeaturesFilter.Keyref";
        
    static final String KEYREF_WARNING = // 0 args
        "ProhibitedFeaturesFilter.KeyrefWarning";
        
    static final String NOTATION = // 0 args
        "ProhibitedFeaturesFilter.Notation";
        
    static final String NOTATION_WARNING = // 0 args
        "ProhibitedFeaturesFilter.NotationWarning";
        
    static final String UNIQUE = // 0 args
        "ProhibitedFeaturesFilter.Unique";
        
    static final String UNIQUE_WARNING = // 0 args
        "ProhibitedFeaturesFilter.UniqueWarning";
        
    static final String FINAL_ATTR_OF_ELEMENT = // 0 args
        "ProhibitedFeaturesFilter.FinalAttrOfElement";
        
    static final String FINAL_ATTR_OF_COMPLEXTYPE = // 0 args
        "ProhibitedFeaturesFilter.FinalAttrOfComplexType";
        
    static final String BLOCKDEFAULT_ATTR_OF_SCHEMA = // 0 args
        "ProhibitedFeaturesFilter.BlockDefaultAttrOfSchema";
        
    static final String FINALDEFAULT_ATTR_OF_SCHEMA = // 0 args
        "ProhibitedFeaturesFilter.FinalDefaultAttrOfSchema";
    
    static final String EXTENSIONBINDINGPREFIXES_OF_SCHEMA = // 0 args
        "ProhibitedFeaturesFilter.ExtensionBindingPrefixesOfSchema";
        
    static final String REDEFINE = // 0 args
        "ProhibitedFeaturesFilter.Redefine";
        
    static final String ILLEGAL_BOOLEAN_VALUE = // 1 arg
        "ProhibitedFeaturesFilter.IllegalBooleanValue";
    


    static final String XERCES_TOO_OLD = // arg:2
        "SchemaConstraintChecker.XercesTooOld";

    static final String UNABLE_TO_CHECK_XERCES_VERSION = // arg:2
        "SchemaConstraintChecker.UnableToCheckXercesVersion";
}
