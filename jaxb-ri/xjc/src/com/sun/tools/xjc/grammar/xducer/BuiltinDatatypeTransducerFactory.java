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
package com.sun.tools.xjc.grammar.xducer;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JType;
import com.sun.msv.datatype.xsd.Base64BinaryType;
import com.sun.msv.datatype.xsd.BooleanType;
import com.sun.msv.datatype.xsd.ByteType;
import com.sun.msv.datatype.xsd.DateTimeType;
import com.sun.msv.datatype.xsd.DateType;
import com.sun.msv.datatype.xsd.DoubleType;
import com.sun.msv.datatype.xsd.FloatType;
import com.sun.msv.datatype.xsd.HexBinaryType;
import com.sun.msv.datatype.xsd.IDREFType;
import com.sun.msv.datatype.xsd.IDType;
import com.sun.msv.datatype.xsd.IntType;
import com.sun.msv.datatype.xsd.IntegerType;
import com.sun.msv.datatype.xsd.LongType;
import com.sun.msv.datatype.xsd.NormalizedStringType;
import com.sun.msv.datatype.xsd.NumberType;
import com.sun.msv.datatype.xsd.QnameType;
import com.sun.msv.datatype.xsd.ShortType;
import com.sun.msv.datatype.xsd.SimpleURType;
import com.sun.msv.datatype.xsd.StringType;
import com.sun.msv.datatype.xsd.TimeType;
import com.sun.msv.datatype.xsd.TokenType;
import com.sun.msv.datatype.xsd.UnsignedByteType;
import com.sun.msv.datatype.xsd.UnsignedIntType;
import com.sun.msv.datatype.xsd.UnsignedShortType;
import com.sun.msv.datatype.xsd.WhiteSpaceFacet;
import com.sun.msv.datatype.xsd.XSDatatype;
import com.sun.msv.datatype.xsd.XSDatatypeImpl;
import com.sun.tools.xjc.generator.util.WhitespaceNormalizer;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.id.IDREFTransducer;
import com.sun.tools.xjc.grammar.id.IDTransducer;
import com.sun.xml.bind.JAXBAssertionError;

/**
 * Transducer for XML Schema built-in datatypes.
 * 
 * <p>
 * Use the {@link #get} method to obtain a transducer for
 * any atomic datatype (note that union/list types are not
 * supported in this implementation; they have to be decomposed to
 * atomic types first.)
 * 
 * 
 * @author
 *    <a href="mailto:kohsuke.kawaguchi@sun.com">Kohsuke KAWAGUCHI</a>
 */
public class BuiltinDatatypeTransducerFactory
{
    // TODO: shall we change the class name to BuiltinDatatypeTransducerFactory?
    // I mean, this class is not a transducer by itself.
    
    /**
     * Builds a transducer that uses a conversion method
     * in XSDLib
     */
    private static Transducer create( JCodeModel model, Class type ) {
        
        try {
            Method m = type.getMethod("load",new Class[]{String.class});
            String className = type.getName();
            
            {// assertion check
                if( !Modifier.isStatic(m.getModifiers()) )
                    // 'load' must be a static method
                    throw new JAXBAssertionError();
            }
            
            
            return new UserTransducer(
                model.ref(m.getReturnType()), className+".load", className+".save" );
        } catch( NoSuchMethodException e ) {
            throw new NoSuchMethodError(
                "cannot find the load method for "+type.getName());
        }
    }
    
    private static Transducer create( JType returnType, String stem ) { 
        return new UserTransducer(returnType,
                    "javax.xml.bind.DatatypeConverter.parse"+stem,
                    "javax.xml.bind.DatatypeConverter.print"+stem );
    }
    

    /**
     * Obtains a transducer that corresponds to the specified datatype.
     * Note that the datatype must be a built-in type.
     * 
     * This method will return the transducer that performs the
     * correct whitespace normalization.
     * 
     * @return
     *      Always return a non-null valid object.
     */
    public static Transducer get( 
        AnnotatedGrammar grammar, XSDatatype dt ) {
            
        Transducer base = getWithoutWhitespaceNormalization(grammar,dt);
        
        // TODO: fix XSDLib
        if( dt instanceof XSDatatypeImpl )
            return WhitespaceTransducer.create( base, grammar.codeModel,
                ((XSDatatypeImpl)dt).whiteSpace );
        WhiteSpaceFacet wsf = (WhiteSpaceFacet)dt.getFacetObject(XSDatatype.FACET_WHITESPACE);
        if(wsf!=null)
            return WhitespaceTransducer.create( base, grammar.codeModel, wsf.whiteSpace );
        
        return WhitespaceTransducer.create( base, grammar.codeModel, WhitespaceNormalizer.COLLAPSE );
    }
    
    /**
     * Obtains a transducer that corresponds to the specified datatype.
     * Note that the datatype must be a built-in type.
     * 
     * The returned transducer will <b>NOT</b> do the whitespace
     * normalization.
     * 
     * @return
     *      Always return a non-null valid object.
     */
    public static Transducer getWithoutWhitespaceNormalization( 
        AnnotatedGrammar grammar, XSDatatype dt ) {
        
        return new TransducerDecorator(
            _getWithoutWhitespaceNormalization(grammar,dt)
            ) {
            public boolean isBuiltin() {
                return true;
            }
        };
    }
        
    private static Transducer _getWithoutWhitespaceNormalization( 
        AnnotatedGrammar grammar, XSDatatype dt ) {
        
        final JCodeModel codeModel = grammar.codeModel;
        
        if( dt.getVariety()!=XSDatatype.VARIETY_ATOMIC )
            // assertion failed: union and list must be decomposed first
            throw new JAXBAssertionError();
            
        
        if( dt==SimpleURType.theInstance )
            return new IdentityTransducer(codeModel);
        
        
        // note that those three types will be wrapped by different
        // WhitespaceTransducer, as they
        // have different whitespace normalizations.
        if( dt==StringType.theInstance
        ||  dt==NormalizedStringType.theInstance
        ||  dt==TokenType.theInstance )
            return new IdentityTransducer(codeModel);
        
        if( dt==IDType.theInstance )
            return new IDTransducer(codeModel, grammar.defaultSymbolSpace );
        
        if( dt==IDREFType.theInstance )
            return new IDREFTransducer(codeModel, grammar.defaultSymbolSpace, false );
        
        if( dt==BooleanType.theInstance )
            return create(codeModel.BOOLEAN, "Boolean" );
        
        if( dt==Base64BinaryType.theInstance )
            return create(codeModel,Base64BinaryType.class);
            
        if( dt==HexBinaryType.theInstance )
            return create(codeModel,HexBinaryType.class);
        
        if( dt==FloatType.theInstance )
            return create(codeModel.FLOAT, "Float" );
        
        if( dt==DoubleType.theInstance )
            return create(codeModel.DOUBLE, "Double" );
        
        if( dt==NumberType.theInstance )
            return create(codeModel.ref(BigDecimal.class), "Decimal" );
        
        if( dt==IntegerType.theInstance )
            return create(codeModel.ref(BigInteger.class), "Integer" );
        
        if( dt==LongType.theInstance
        ||  dt==UnsignedIntType.theInstance )
            return create( codeModel.LONG, "Long" );
        
        if( dt==IntType.theInstance
        ||  dt==UnsignedShortType.theInstance )
            return create( codeModel.INT, "Int" );
        
        if( dt==ShortType.theInstance
        ||  dt==UnsignedByteType.theInstance )
            return create( codeModel.SHORT, "Short" );
        
        if( dt==ByteType.theInstance )
            return create( codeModel.BYTE, "Byte" );
        
        if( dt==QnameType.theInstance )
            return new QNameTransducer(codeModel);
        
        if( dt==DateType.theInstance )
            return create( codeModel.ref(Calendar.class), "Date" );
//            return new DateTransducer(codeModel, codeModel.ref(DateType.class));
        
        if( dt==TimeType.theInstance )
            return new DateTransducer(codeModel, codeModel.ref(TimeType.class));
        
        if( dt==DateTimeType.theInstance )
            return new DateTransducer(codeModel, codeModel.ref(DateTimeType.class));
        
        // maybe this datatype is not a built-in type.
        // process this datatype as a base type.
        return _getWithoutWhitespaceNormalization(grammar,dt.getBaseType());
    }
}
