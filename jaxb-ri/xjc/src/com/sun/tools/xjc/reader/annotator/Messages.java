/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
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
