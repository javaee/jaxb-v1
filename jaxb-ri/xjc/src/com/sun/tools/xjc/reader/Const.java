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

/**
 * Useful constant values.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class Const {
    
    /** XML Schema namespace URI. */
    public final static String XMLSchemaNSURI =
        "http://www.w3.org/2001/XMLSchema";
    
    /** XML Schema instance namespace URI. */
    public final static String XMLSchemaInstanceNSURI =
        "http://www.w3.org/2001/XMLSchema-instance";
    
    /** XML namespace URI. */
    public final static String XMLNS_URI =
        "http://www.w3.org/2000/xmlns/";
    
    /** JAXB customization URI. */
    public final static String JAXB_NSURI =
        "http://java.sun.com/xml/ns/jaxb";
    
    /** XJC vendor extension namespace URI. */
    public final static String XJC_EXTENSION_URI =
        "http://java.sun.com/xml/ns/jaxb/xjc";
    
    /** JAXB customization version. */
    public final static String JAXB_VERSION = "1.0";

    /** RELAX NG namespace URI. */
    public static final String RELAXNG_URI =
        "http://relaxng.org/ns/structure/1.0";
    
    /** List of all built-in types. */
    public final static String[] builtinTypeNames = new String[] {
        "anySimpleType",
        "duration",
        "dateTime",
        "time",
        "date",
        "gYearMonth",
        "gYear",
        "gMonthDay",
        "gDay",
        "gMonth",
        "string",
        "normalizedString",
        "token",
        "language",
        "Name",
        "NCName",
        "ID",
        "IDREF",
        "IDREFS",
        "ENTITY",
        "ENTITIES",
        "NMTOKEN",
        "NMTOKENS",
        "boolean",
        "base64Binary",
        "hexBinary",
        "float",
        "decimal",
        "integer",
        "nonPositiveInteger",
        "negativeInteger",
        "long",
        "int",
        "short",
        "byte",
        "nonNegativeInteger",
        "unsignedLong",
        "unsignedInt",
        "unsignedShort",
        "unsignedByte",
        "positiveInteger",
        "double",
        "anyURI",
        "QName",
        "NOTATION"};
}

