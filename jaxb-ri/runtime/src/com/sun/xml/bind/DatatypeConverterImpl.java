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

package com.sun.xml.bind;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.DatatypeConverterInterface;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;

import com.sun.msv.datatype.xsd.Base64BinaryType;
import com.sun.msv.datatype.xsd.BooleanType;
import com.sun.msv.datatype.xsd.ByteType;
import com.sun.msv.datatype.xsd.DateTimeType;
import com.sun.msv.datatype.xsd.DateType;
import com.sun.msv.datatype.xsd.DoubleType;
import com.sun.msv.datatype.xsd.FloatType;
import com.sun.msv.datatype.xsd.HexBinaryType;
import com.sun.msv.datatype.xsd.IntType;
import com.sun.msv.datatype.xsd.IntegerType;
import com.sun.msv.datatype.xsd.LongType;
import com.sun.msv.datatype.xsd.NumberType;
import com.sun.msv.datatype.xsd.ShortType;
import com.sun.msv.datatype.xsd.SimpleURType;
import com.sun.msv.datatype.xsd.TimeType;
import com.sun.msv.datatype.xsd.UnsignedIntType;
import com.sun.msv.datatype.xsd.UnsignedShortType;
import com.sun.msv.datatype.xsd.XmlNames;

/**
 * This class is the JAXB RI's default implementation of the 
 * javax.xml.bind.DatatypeConverterInterface.
 * 
 * When client apps specify the use of the static print/parse
 * methods in javax.xml.bind.DatatypeConverter, it will delegate
 * to this class, which in turn, delegates to XSDLib where all
 * of the real work happens.
 * 
 * @author <ul><li>Ryan Shoemaker, Sun Microsystems, Inc.</li></ul>
 * @version $Revision: 1.3 $
 * @since JAXB1.0
 */
public class DatatypeConverterImpl implements DatatypeConverterInterface {
    
    /**
     * To avoid re-creating instances, we cache one instance.
     */
    public static final DatatypeConverterInterface theInstance = new DatatypeConverterImpl();
        
    protected DatatypeConverterImpl() {
    }
    
    /**
     * @see javax.xml.bind.DatatypeConverterInterface#parseString(String)
     */
    public String parseString(String lexicalXSDString) {
        return lexicalXSDString;
    }

    /**
     * @see javax.xml.bind.DatatypeConverterInterface#parseInteger(String)
     */
    public BigInteger parseInteger(String lexicalXSDInteger) {
        return IntegerType.load( lexicalXSDInteger );
    }

    /**
     * @see javax.xml.bind.DatatypeConverterInterface#parseInt(String)
     */
    public int parseInt(String lexicalXSDInt) {
        return IntType.load( lexicalXSDInt ).intValue();
    }

    /**
     * @see javax.xml.bind.DatatypeConverterInterface#parseLong(String)
     */
    public long parseLong(String lexicalXSLong) {
        return LongType.load( lexicalXSLong ).longValue();
    }

    /**
     * @see javax.xml.bind.DatatypeConverterInterface#parseShort(String)
     */
    public short parseShort(String lexicalXSDShort) {
        return ShortType.load( lexicalXSDShort ).shortValue();
    }

    /**
     * @see javax.xml.bind.DatatypeConverterInterface#parseDecimal(String)
     */
    public BigDecimal parseDecimal(String content) {
        // since JDK1.3, leading '+' is allowed.
        // perhaps we should assume that we use JDK1.3 or higher?
        if( content.charAt(0)=='+' )
            content = content.substring(1);
        
        return new BigDecimal(content);
        
        // from purely XML Schema perspective,
        // this implementation has a problem, since 
        // in xs:decimal "1.0" and "1" is equal whereas the above
        // code will return different values for those two forms.
        //
        // the code was originally using com.sun.msv.datatype.xsd.NumberType.load,
        // but a profiling showed that the process of normalizing "1.0" into "1"
        // could take non-trivial time.
        //
        // also, from the user's point of view, one might be surprised if
        // 1 (not 1.0) is returned from "1.000" 
    }

    /**
     * @see javax.xml.bind.DatatypeConverterInterface#parseFloat(String)
     */
    public float parseFloat(String lexicalXSDFloat) {
        return FloatType.load( lexicalXSDFloat ).floatValue();
    }

    /**
     * @see javax.xml.bind.DatatypeConverterInterface#parseDouble(String)
     */
    public double parseDouble(String lexicalXSDDouble) {
        return DoubleType.load( lexicalXSDDouble ).doubleValue();
    }

    /**
     * @see javax.xml.bind.DatatypeConverterInterface#parseBoolean(String)
     */
    public boolean parseBoolean(String lexicalXSDBoolean) {
        return BooleanType.load( lexicalXSDBoolean ).booleanValue();
    }

    /**
     * @see javax.xml.bind.DatatypeConverterInterface#parseByte(String)
     */
    public byte parseByte(String lexicalXSDByte) {
        return ByteType.load( lexicalXSDByte ).byteValue();
    }

    /**
     * @see javax.xml.bind.DatatypeConverterInterface#parseQName(String, NamespaceContext)
     */
    public QName parseQName(String lexicalXSDQName, NamespaceContext nsc) {
        // Double-check
        String uri = null;
        String localPart = null;
        String prefix;
        int first = lexicalXSDQName.indexOf( ':' );

        if( first <= 0 ) {
            // javax.xml.namespace.NamespaceContext2 doesn't currently specify
            // how you get the default namespace uri.  For now, we'll assume
            // that you pass the empty string to the getUri() method to get it.
            uri = resolveNamespacePrefix( "", nsc );
            localPart = lexicalXSDQName;
            prefix = "";
        } else {
            // Prefix exists, check everything
            prefix = lexicalXSDQName.substring(0, first);
            localPart = lexicalXSDQName.substring(first + 1);
            
            uri = resolveNamespacePrefix(prefix, nsc );
        }
        
        if(uri==null)
            // TODO: not sure if this is an error.
            // depends on the behavior of the getUri method.
            return null;
            
        if(! XmlNames.isUnqualifiedName( localPart ) )
            // technically we don't need to do this since we are not required
            // to check the validity.
            return null;
        
        return new QName(uri,localPart,prefix);
    }
    
    /**
     * take a prefix and NamespaceContext2 and figure out the uri
     */
    private String resolveNamespacePrefix( String prefix, NamespaceContext nsc ) {
        if(prefix.equals("xml"))
            return "http://www.w3.org/XML/1998/namespace";
        
        return nsc.getNamespaceURI( prefix );
    }

    /**
     * @see javax.xml.bind.DatatypeConverterInterface#parseDateTime(String)
     */
    public Calendar parseDateTime(String lexicalXSDDateTime) {
        return (Calendar)DateTimeType.theInstance.createJavaObject( lexicalXSDDateTime, null );
    }

    /**
     * @see javax.xml.bind.DatatypeConverterInterface#parseBase64Binary(String)
     */
    public byte[] parseBase64Binary(String lexicalXSDBase64Binary) {
        return Base64BinaryType.load( lexicalXSDBase64Binary );
    }

    /**
     * @see javax.xml.bind.DatatypeConverterInterface#parseHexBinary(String)
     */
    public byte[] parseHexBinary(String lexicalXSDHexBinary) {
        return HexBinaryType.load( lexicalXSDHexBinary );
    }

    /**
     * @see javax.xml.bind.DatatypeConverterInterface#parseUnsignedInt(String)
     */
    public long parseUnsignedInt(String lexicalXSDUnsignedInt) {
        return UnsignedIntType.load( lexicalXSDUnsignedInt ).longValue();
    }

    /**
     * @see javax.xml.bind.DatatypeConverterInterface#parseUnsignedShort(String)
     */
    public int parseUnsignedShort(String lexicalXSDUnsignedShort) {
        return UnsignedShortType.load( lexicalXSDUnsignedShort ).shortValue();
    }

    /**
     * @see javax.xml.bind.DatatypeConverterInterface#parseTime(String)
     */
    public Calendar parseTime(String lexicalXSDTime) {
        return (Calendar)TimeType.theInstance.createJavaObject( lexicalXSDTime, null );
    }

    /**
     * @see javax.xml.bind.DatatypeConverterInterface#parseDate(String)
     */
    public Calendar parseDate(String lexicalXSDDate) {
        return (Calendar)DateType.theInstance.createJavaObject( lexicalXSDDate, null );
    }

    /**
     * @see javax.xml.bind.DatatypeConverterInterface#parseAnySimpleType(String)
     */
    public String parseAnySimpleType(String lexicalXSDAnySimpleType) {
        return lexicalXSDAnySimpleType;
//        return (String)SimpleURType.theInstance._createValue( lexicalXSDAnySimpleType, null );
    }

    /**
     * @see javax.xml.bind.DatatypeConverterInterface#printString(String)
     */
    public String printString(String val) {
//        return StringType.theInstance.convertToLexicalValue( val, null );
        return val;
    }

    /**
     * @see javax.xml.bind.DatatypeConverterInterface#printInteger(BigInteger)
     */
    public String printInteger(BigInteger val) {
        return IntegerType.save( val );
    }

    /**
     * @see javax.xml.bind.DatatypeConverterInterface#printInt(int)
     */
    public String printInt(int val) {
        return IntType.save( new Integer( val ) );
    }

    /**
     * @see javax.xml.bind.DatatypeConverterInterface#printLong(long)
     */
    public String printLong(long val) {
        return LongType.save( new Long( val ) );
    }

    /**
     * @see javax.xml.bind.DatatypeConverterInterface#printShort(short)
     */
    public String printShort(short val) {
        return ShortType.save( new Short( val ) );
    }

    /**
     * @see javax.xml.bind.DatatypeConverterInterface#printDecimal(BigDecimal)
     */
    public String printDecimal(BigDecimal val) {
        return NumberType.save( val );
    }

    /**
     * @see javax.xml.bind.DatatypeConverterInterface#printFloat(float)
     */
    public String printFloat(float val) {
        return FloatType.save( new Float( val ) );
    }

    /**
     * @see javax.xml.bind.DatatypeConverterInterface#printDouble(double)
     */
    public String printDouble(double val) {
        return DoubleType.save( new Double( val ) );
    }

    /**
     * @see javax.xml.bind.DatatypeConverterInterface#printBoolean(boolean)
     */
    public String printBoolean(boolean val) {
        return BooleanType.save( new Boolean( val ) );
    }

    /**
     * @see javax.xml.bind.DatatypeConverterInterface#printByte(byte)
     */
    public String printByte(byte val) {
        return ByteType.save( new Byte( val ) );
    }

    /**
     * @see javax.xml.bind.DatatypeConverterInterface#printQName(QName, NamespaceContext)
     */
    public String printQName(QName val, NamespaceContext nsc) {
        // Double-check
        String qname = null;
        String prefix = nsc.getPrefix( val.getNamespaceURI() );
        String localPart = val.getLocalPart();
        
        if( prefix == null || prefix.length()==0 ) { // be defensive
            qname = localPart;
        } else {
            qname = prefix + ":" + localPart;
        }

        return qname;
    }

    /**
     * @see javax.xml.bind.DatatypeConverterInterface#printDateTime(Calendar)
     */
    public String printDateTime(Calendar val) {
        return DateTimeType.theInstance.serializeJavaObject( val, null );
    }

    /**
     * @see javax.xml.bind.DatatypeConverterInterface#printBase64Binary(byte[])
     */
    public String printBase64Binary(byte[] val) {
        return Base64BinaryType.save( val );
    }

    /**
     * @see javax.xml.bind.DatatypeConverterInterface#printHexBinary(byte[])
     */
    public String printHexBinary(byte[] val) {
        return HexBinaryType.save( val );
    }

    /**
     * @see javax.xml.bind.DatatypeConverterInterface#printUnsignedInt(long)
     */
    public String printUnsignedInt(long val) {
        return UnsignedIntType.save( new Long( val ) );
    }

    /**
     * @see javax.xml.bind.DatatypeConverterInterface#printUnsignedShort(int)
     */
    public String printUnsignedShort(int val) {
        return UnsignedShortType.save( new Integer( val ) );
    }

    /**
     * @see javax.xml.bind.DatatypeConverterInterface#printTime(Calendar)
     */
    public String printTime(Calendar val) {
        return TimeType.theInstance.serializeJavaObject( val, null );
    }

    /**
     * @see javax.xml.bind.DatatypeConverterInterface#printDate(Calendar)
     */
    public String printDate(Calendar val) {
        return DateType.theInstance.serializeJavaObject( val, null );
    }

    /**
     * @see javax.xml.bind.DatatypeConverterInterface#printAnySimpleType(String)
     */
    public String printAnySimpleType(String val) {
        // Double-check
        return SimpleURType.theInstance.convertToLexicalValue( val, null );
    }
    
    
    /**
     * Just return the string passed as a parameter but
     * installs an instance of this class as the DatatypeConverter
     * implementation. Used from static fixed value initializers.
     */
    public static String installHook( String s ) {
        DatatypeConverter.setDatatypeConverter(theInstance);
        return s;
    }

}
