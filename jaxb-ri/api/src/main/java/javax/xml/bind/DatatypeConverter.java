/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2001-2011 Oracle and/or its affiliates. All rights reserved.
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

package javax.xml.bind;

import javax.xml.namespace.NamespaceContext;

/**
 * <p>
 * The javaType binding declaration can be used to customize the binding of 
 * an XML schema datatype to a Java datatype. Customizations can involve 
 * writing a parse and print method for parsing and printing lexical 
 * representations of a XML schema datatype respectively. However, writing 
 * parse and print methods requires knowledge of the lexical representations ( 
 * <a href="http://www.w3.org/TR/xmlschema-2/"> XML Schema Part2: Datatypes 
 * specification </a>) and hence may be difficult to write. 
 * </p>
 * <p>
 * This class makes it easier to write parse and print methods. It defines
 * static parse and print methods that provide access to a JAXB provider's 
 * implementation of parse and print methods. These methods are invoked by 
 * custom parse and print methods. For example, the binding of xsd:dateTime 
 * to a long can be customized using parse and print methods as follows:
 * <blockquote>
 *    <pre>
 *    // Customized parse method 
 *    public long myParseCal( String dateTimeString ) {
 *        java.util.Calendar cal = DatatypeConverter.parseDateTime(dateTimeString);
 *        long longval = convert_calendar_to_long(cal); //application specific
 *        return longval;
 *    }
 *     
 *    // Customized print method
 *    public String myPrintCal( Long longval ) {
 *        java.util.Calendar cal = convert_long_to_calendar(longval) ; //application specific
 *        String dateTimeString = DatatypeConverter.printDateTime(cal);
 *        return dateTimeString;
 *    }
 *    </pre>
 * </blockquote>
 * <p>
 * There is a static parse and print method corresponding to each parse and 
 * print method respectively in the {@link DatatypeConverterInterface 
 * DatatypeConverterInterface}. 
 * <p>
 * The static methods defined in the class can also be used to specify
 * a parse or a print method in a javaType binding declaration.
 * </p>
 * <p>
 * JAXB Providers are required to call the 
 * {@link #setDatatypeConverter(DatatypeConverterInterface) 
 * setDatatypeConverter} api at some point before the first marshal or unmarshal 
 * operation (perhaps during the call to JAXBContext.newInstance).  This step is 
 * necessary to configure the converter that should be used to perform the 
 * print and parse functionality.  
 * </p>
 * 
 * @author <ul><li>Sekhar Vajjhala, Sun Microsystems, Inc.</li><li>Joe Fialli, Sun Microsystems Inc.</li><li>Kohsuke Kawaguchi, Sun Microsystems, Inc.</li><li>Ryan Shoemaker,Sun Microsystems Inc.</li></ul>
 * @version $Revision: 1.3 $
 * @see DatatypeConverterInterface
 * @see ParseConversionEvent
 * @see PrintConversionEvent
 * @since JAXB1.0
 */

final public class DatatypeConverter {

    // delegate to this instance of DatatypeConverter
    private static DatatypeConverterInterface theConverter = null;
        
    private DatatypeConverter() {
        // private constructor
    }
    
    /**
     * This method is for JAXB provider use only.
     * <p>
     * JAXB Providers are required to call this method at some point before
     * allowing any of the JAXB client marshal or unmarshal operations to
     * occur.  This is necessary to configure the datatype converter that 
     * should be used to perform the print and parse conversions.
     * 
     * <p>
     * Calling this api repeatedly will have no effect - the 
     * DatatypeConverterInterface instance passed into the first invocation is 
     * the one that will be used from then on.
     * 
     * @param converter an instance of a class that implements the 
     * DatatypeConverterInterface class - this parameter must not be null.
     * @throws IllegalArgumentException if the parameter is null
     */
    public static void setDatatypeConverter( DatatypeConverterInterface converter ) {
        if( converter == null ) {
            throw new IllegalArgumentException( 
                Messages.format( Messages.CONVERTER_MUST_NOT_BE_NULL ) );
        } else if( theConverter == null ) {
            theConverter = converter;
        }
    }

    /**
     * <p>
     * Convert the lexical XSD string argument into a String value.
     * @param
     *     lexicalXSDString A string containing a lexical representation of 
     *     xsd:string.
     * @return
     *     A String value represented by the string argument.
     */ 
    public static String parseString( String lexicalXSDString ) {
        return theConverter.parseString( lexicalXSDString );
    }

    /**
     * <p>
     * Convert the string argument into a BigInteger value.
     * @param
     *     lexicalXSDInteger A string containing a lexical representation of 
     *     xsd:integer.
     * @return
     *     A BigInteger value represented by the string argument.
     */ 
    public static java.math.BigInteger parseInteger( String lexicalXSDInteger ) {
        return theConverter.parseInteger( lexicalXSDInteger );
    }

    /**
     * <p>
     * Convert the string argument into an int value.
     * @param
     *     lexicalXSDInt A string containing a lexical representation of 
     *     xsd:int.
     * @return
     *     A int value represented by the string argument.
     */ 
    public static int parseInt( String lexicalXSDInt ) {
        return theConverter.parseInt( lexicalXSDInt );
    }

    /**
     * <p>
     * Converts the string argument into a long value.
     * @param
     *     lexicalXSDLong A string containing lexical representation of 
     *     xsd:long.
     * @return
     *     A long value represented by the string argument.
     */ 
    public static long parseLong( String lexicalXSLong ) {
        return theConverter.parseLong( lexicalXSLong );
    }

    /**
     * <p>
     * Converts the string argument into a short value.
     * @param
     *     lexicalXSDShort A string containing lexical representation of 
     *     xsd:short.
     * @return
     *     A short value represented by the string argument.
     */ 
    public static short parseShort( String lexicalXSShort ) { 
        return theConverter.parseShort( lexicalXSShort );
    }

    /**
     * <p>
     * Converts the string argument into a BigDecimal value.
     * @param
     *     lexicalXSDDecimal A string containing lexical representation of 
     *     xsd:decimal.
     * @return
     *     A BigDecimal value represented by the string argument.
     */ 
    public static java.math.BigDecimal parseDecimal( String lexicalXSDDecimal ) {
        return theConverter.parseDecimal( lexicalXSDDecimal );
    }

    /**
     * <p>
     * Converts the string argument into a float value.
     * @param
     *     lexicalXSDFloat A string containing lexical representation of 
     *     xsd:float.
     * @return
     *     A float value represented by the string argument.
     */ 
    public static float parseFloat( String lexicalXSDFloat ) {
        return theConverter.parseFloat( lexicalXSDFloat );
    }

    /**
     * <p>
     * Converts the string argument into a double value.
     * @param
     *     lexicalXSDDouble A string containing lexical representation of 
     *     xsd:double.
     * @return
     *     A double value represented by the string argument.
     */ 
    public static double parseDouble( String lexicalXSDDouble ) { 
        return theConverter.parseDouble( lexicalXSDDouble );
    }

    /**
     * <p>
     * Converts the string argument into a boolean value.
     * @param
     *     lexicalXSDBoolean A string containing lexical representation of 
     *     xsd:boolean.
     * @return
     *     A boolean value represented by the string argument.
     */ 
    public static boolean parseBoolean( String lexicalXSDBoolean ) {
        return theConverter.parseBoolean( lexicalXSDBoolean );
    }

    /**
     * <p>
     * Converts the string argument into a byte value.
     * @param
     *     lexicalXSDByte A string containing lexical representation of 
     *     xsd:byte.
     * @return
     *     A byte value represented by the string argument.
     */ 
    public static byte parseByte( String lexicalXSDByte ) { 
        return theConverter.parseByte( lexicalXSDByte );
    }

    /**
     * <p>
     * Converts the string argument into a byte value.
     * @param lexicalXSDQName
     *     A string containing lexical representation of xsd:QName.
     * @param nsc
     *     A namespace context for interpreting a prefix within a QName.
     * @return
     *     A QName value represented by the string argument.
     */ 
    public static javax.xml.namespace.QName parseQName( String lexicalXSDQName,
    				                    NamespaceContext nsc) {
        return theConverter.parseQName( lexicalXSDQName, nsc );
    }

    /**
     * <p>
     * Converts the string argument into a Calendar value.
     * @param
     *     lexicalXSDDateTime A string containing lexical representation of 
     *     xsd:datetime.
     * @return
     *     A Calendar object represented by the string argument.
     */ 
    public static java.util.Calendar parseDateTime( String lexicalXSDDateTime ) {
        return theConverter.parseDateTime( lexicalXSDDateTime );
    }

    /**
     * <p>
     * Converts the string argument into an array of bytes.
     * @param
     *     lexicalXSDBase64Binary A string containing lexical representation
     *     of xsd:base64Binary.
     * @return
     *     An array of bytes represented by the string argument.
     */ 
    public static byte[] parseBase64Binary( String lexicalXSDBase64Binary ) {
        return theConverter.parseBase64Binary( lexicalXSDBase64Binary );
    }

    /**
     * <p>
     * Converts the string argument into an array of bytes.
     * @param
     *     lexicalXSDHexBinary A string containing lexical representation of
     *     xsd:hexBinary.
     * @return
     *     An array of bytes represented by the string argument.
     */ 
   public static byte[] parseHexBinary( String lexicalXSDHexBinary ) {
        return theConverter.parseHexBinary( lexicalXSDHexBinary );
    }

    /**
     * <p>
     * Converts the string argument into a long value.
     * @param
     *     lexicalXSDUnsignedInt A string containing lexical representation 
     *     of xsd:unsignedInt.
     * @return
     *     A long value represented by the string argument.
     */ 
    public static long parseUnsignedInt( String lexicalXSDUnsignedInt ) {
        return theConverter.parseUnsignedInt( lexicalXSDUnsignedInt );
    }

    /**
     * <p>
     * Converts the string argument into an int value.
     * @param
     *     lexicalXSDUnsignedShort A string containing lexical 
     *     representation of xsd:unsignedShort.
     * @return
     *     An int value represented by the string argument.
     */ 
    public static int	parseUnsignedShort( String lexicalXSDUnsignedShort ) {
        return theConverter.parseUnsignedShort( lexicalXSDUnsignedShort );
    }

    /**
     * <p>
     * Converts the string argument into a Calendar value.
     * @param
     *     lexicalXSDTime A string containing lexical representation of 
     *     xsd:time.
     * @return
     *     A Calendar value represented by the string argument.
     */ 
    public static java.util.Calendar parseTime( String lexicalXSDTime ) {
        return theConverter.parseTime( lexicalXSDTime ); 
    }
    /**
     * <p>
     * Converts the string argument into a Calendar value.
     * @param
     *     lexicalXSDDate A string containing lexical representation of 
     *     xsd:Date.
     * @return
     *     A Calendar value represented by the string argument.
     */ 
    public static java.util.Calendar parseDate( String lexicalXSDDate ) {
        return theConverter.parseDate( lexicalXSDDate );
    }

    /**
     * <p>
     * Return a string containing the lexical representation of the 
     * simple type.
     * @param
     *     lexicalXSDAnySimpleType A string containing lexical 
     *     representation of the simple type.
     * @return
     *     A string containing the lexical representation of the 
     *     simple type.
     */ 
    public static String parseAnySimpleType( String lexicalAnySimpleType ) {
        return theConverter.parseAnySimpleType( lexicalAnySimpleType );
    }
    /**
     * <p>
     * Converts the string argument into a string.
     * @param val
     *     A string value.
     * @return
     *     A string containing a lexical representation of xsd:string.
     */ 
     // also indicate the print methods produce a lexical
     // representation for given Java datatypes.
	
    public static String printString( String val ) {
        return theConverter.printString( val );
    }

    /**
     * <p>
     * Converts a BigInteger value into a string.
     * @param val
     *     A BigInteger value
     * @return
     *     A string containing a lexical representation of xsd:integer
     */ 
    public static String printInteger( java.math.BigInteger val ) {
        return theConverter.printInteger( val );
    }

    /**
     * <p>
     * Converts an int value into a string.
     * @param val
     *     An int value
     * @return
     *     A string containing a lexical representation of xsd:int
     */ 
    public static String printInt( int val ) {
        return theConverter.printInt( val );
    }

    /**
     * <p>
     * Converts A long value into a string.
     * @param val
     *     A long value
     * @return
     *     A string containing a lexical representation of xsd:long
     */ 
    public static String printLong( long val ) {
        return theConverter.printLong( val );
    }

    /**
     * <p>
     * Converts a short value into a string.
     * @param val
     *     A short value
     * @return
     *     A string containing a lexical representation of xsd:short
     */ 
    public static String printShort( short val ) {
        return theConverter.printShort( val );
    }

    /**
     * <p>
     * Converts a BigDecimal value into a string.
     * @param val
     *     A BigDecimal value
     * @return
     *     A string containing a lexical representation of xsd:decimal
     */ 
    public static String printDecimal( java.math.BigDecimal val ) {
        return theConverter.printDecimal( val );
    }

    /**
     * <p>
     * Converts a float value into a string.
     * @param val
     *     A float value
     * @return
     *     A string containing a lexical representation of xsd:float
     */ 
    public static String printFloat( float val ) {
        return theConverter.printFloat( val );
    }

    /**
     * <p>
     * Converts a double value into a string.
     * @param val
     *     A double value
     * @return
     *     A string containing a lexical representation of xsd:double
     */ 
    public static String printDouble( double val ) {
        return theConverter.printDouble( val );
    }

    /**
     * <p>
     * Converts a boolean value into a string.
     * @param val
     *     A boolean value
     * @return
     *     A string containing a lexical representation of xsd:boolean
     */ 
    public static String printBoolean( boolean val ) {
        return theConverter.printBoolean( val );
    }

    /**
     * <p>
     * Converts a byte value into a string.
     * @param val
     *     A byte value
     * @return
     *     A string containing a lexical representation of xsd:byte
     */ 
    public static String printByte( byte val ) {
        return theConverter.printByte( val );
    }

    /**
     * <p>
     * Converts a QName instance into a string.
     * @param val
     *     A QName value
     * @param nsc
     *     A namespace context for interpreting a prefix within a QName.
     * @return
     *     A string containing a lexical representation of QName
     */ 
    public static String printQName( javax.xml.namespace.QName val,
                                     NamespaceContext nsc ) {
        return theConverter.printQName( val, nsc );
    }

    /**
     * <p>
     * Converts a Calendar value into a string.
     * @param val
     *     A Calendar value
     * @return
     *     A string containing a lexical representation of xsd:dateTime
     */ 
    public static String printDateTime( java.util.Calendar val ) {
        return theConverter.printDateTime( val );
    }

    /**
     * <p>
     * Converts an array of bytes into a string.
     * @param val
     *     An array of bytes
     * @return
     *     A string containing a lexical representation of xsd:base64Binary
     */ 
    public static String printBase64Binary( byte[] val ) {
        return theConverter.printBase64Binary( val );
    }

    /**
     * <p>
     * Converts an array of bytes into a string.
     * @param val
     *     An array of bytes
     * @return
     *     A string containing a lexical representation of xsd:hexBinary
     */ 
    public static String printHexBinary( byte[] val ) {
        return theConverter.printHexBinary( val );
    }

    /**
     * <p>
     * Converts a long value into a string.
     * @param val
     *     A long value
     * @return
     *     A string containing a lexical representation of xsd:unsignedInt
     */ 
    public static String printUnsignedInt( long val ) {
        return theConverter.printUnsignedInt( val );
    }

    /**
     * <p>
     * Converts an int value into a string.
     * @param val
     *     An int value
     * @return
     *     A string containing a lexical representation of xsd:unsignedShort
     */ 
    public static String printUnsignedShort( int val ) {
        return theConverter.printUnsignedShort( val );
    }

    /**
     * <p>
     * Converts a Calendar value into a string.
     * @param val
     *     A Calendar value
     * @return
     *     A string containing a lexical representation of xsd:time
     */ 
    public static String printTime( java.util.Calendar val ) {
        return theConverter.printTime( val );
    }

    /**
     * <p>
     * Converts a Calendar value into a string.
     * @param val
     *     A Calendar value
     * @return
     *     A string containing a lexical representation of xsd:date
     */ 
    public static String printDate( java.util.Calendar val ) {
        return theConverter.printDate( val );
    }

    /**
     * <p>
     * Converts a string value into a string.
     * @param val
     *     A string value
     * @return
     *     A string containing a lexical representation of xsd:AnySimpleType
     */ 
    public static String printAnySimpleType( String val ) {
        return theConverter.printAnySimpleType( val );
    }
}
