/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.grammar.xducer;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.xml.sax.Locator;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JJavaName;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPrimitiveType;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.msv.datatype.xsd.XSDatatype;
import com.sun.msv.grammar.ChoiceExp;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ValueExp;
import com.sun.msv.grammar.util.ExpressionPrinter;
import com.sun.tools.xjc.generator.GeneratorContext;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.reader.NameConverter;
import com.sun.xml.bind.DatatypeConverterImpl;
import com.sun.xml.bind.JAXBAssertionError;

/**
 * Transducer that converts a string into an "enumeration class."
 * 
 * The structure of the generated class needs to precisely
 * follow the JAXB spec.
 * 
 * @author
 *    <a href="mailto:kohsuke.kawaguchi@sun.com">Kohsuke KAWAGUCHI</a>
 */
public class EnumerationXducer extends TransducerImpl
{
    public static class MemberInfo
    {
        /** Name of the constant, or null to default. */
        public final String name;
        /** Javadoc comment. Can be null. */
        public final String javadoc;
        
        public MemberInfo( String _name, String _javadoc ) {
            this.name = _name;
            this.javadoc = _javadoc;
        }
    }
    // TODO: support for context-dependent datatypes

    /**
     * Type object that corresponds to
     * the generated enumeration class.
     */
    private final JDefinedClass type;
    public JType getReturnType() { return type; }
    
    /** Used to obtain names for enumeration members. */
    private final NameConverter nameConverter;
    
    private final JCodeModel codeModel;
    
    /**
     * Map from ValueExp to {@link MemberInfo} objects.
     * Cannot be null, but can be empty.
     */
    private final Map members;
    
    /**
     * Source line information that points to the place
     * where this type-safe enum is defined.
     * Used to report error messages.
     */
    private Locator sourceLocator;
    
    /**
     * @param clz
     *        empty JDefinedClass object that will be used
     *        as the enumeration class.
     * @param _members
     *      Any user-specified name for values. A map from
     *      ValueExp to {@link MemberInfo}. Cannot be null.
     */
    public EnumerationXducer(
        NameConverter _nc,
        JDefinedClass clz,
        Expression enumExp,
        Map _members,
        Locator _sourceLocator) {

        this.type = clz;
        this.codeModel = clz.owner();
        this.nameConverter = _nc;
        this.members = _members;
        this.sourceLocator = _sourceLocator;

        // obtain child ValueExps
        this.values = getValues(enumExp);
    }

    private boolean populated = false;
    private ValueExp[] values;  // choice of enumerations
    /**
     * choices[i] is the generated constant for value[i].
     */
    private JFieldVar[] items;
    
    /**
     * Type of the Java value.
     * For example, if this is an enumeration of xs:int,
     * then this field will be Java int.
     */
    private JType valueType;
    
    public void populate( AnnotatedGrammar grammar, GeneratorContext context ) {
        
        // avoid populating twice.
        if(populated)      return;
        populated = true;
        
        // we will use a lot of those.
        final JClass stringRef = codeModel.ref(String.class);
        final JClass objectRef = codeModel.ref(Object.class);
        
        
        // assertion check for those datatypes
        if(!sanityCheck(context))   return;
        
        
        // create the transducer for the member types.
        Transducer xducer = BuiltinDatatypeTransducerFactory.get(
            grammar,(XSDatatype)values[0].dt);
        valueType = xducer.getReturnType(); // value type
        
        
        // [RESULT]
        // private static final Map valueMap = new HashMap()
        JVar $valueMap;
        {
            $valueMap = type.field(
                JMod.PRIVATE|JMod.FINAL|JMod.STATIC,
                Map.class,
                "valueMap",
                JExpr._new(codeModel.ref(HashMap.class)));
        }
        
        
        
        
        items = new JFieldVar[values.length];
        JVar[] valueObjs = new JVar[values.length];
        
        // for each member <m>,
        // [RESULT]
        //    public static final <ThisType> <EnumName> = new <ThisType>(<value>);
        //    public static final <valueType> _<EnumName> = <deserializer of m>(<value>);
        
        Set enumFieldNames = new HashSet();    // record generated field names to detect collision
        
        for( int i=0; i<values.length; i++ ) {
            
            String lexical;
            if( values[i].dt instanceof XSDatatype )
                // we are assuming that the datatype is context-independent
                lexical = ((XSDatatype)values[i].dt)
                    .convertToLexicalValue( values[i].value, null );
            else
                // try the toString method otherwise
                lexical = values[i].value.toString();
            
            MemberInfo mem = (MemberInfo)members.get(values[i]);
            String constName = null;
            
            if( mem!=null )
                constName = mem.name;
            
            if( constName==null )
                constName = nameConverter.toConstantName(fixUnsafeCharacters(lexical));
            
            if(!JJavaName.isJavaIdentifier(constName)) {
                // didn't produce a name.
                reportError( context,
                    Messages.format(ERR_UNUSABLE_NAME, lexical, constName ) );
            }

            if( !enumFieldNames.add(constName) )
                reportError( context, Messages.format(ERR_NAME_COLLISION, constName ));
            else
            // if constName collids, '_'+constName is also likely to collide
            // but there's no point in reporting them individually.
            if( !enumFieldNames.add('_'+constName) )
                reportError( context, Messages.format(ERR_NAME_COLLISION, '_'+constName ));

            valueObjs[i] = type.field( JMod.PUBLIC|JMod.STATIC|JMod.FINAL,
                valueType, '_'+constName );
            
            items[i] = type.field( JMod.PUBLIC|JMod.STATIC|JMod.FINAL,
                type, constName );
            
            items[i].init(JExpr._new(type)
                .arg(valueObjs[i]));
            
            
            // set javadoc
            if( mem!=null && mem.javadoc!=null )
                items[i].javadoc().appendComment(mem.javadoc);
            
            // ASSUMPTION: datatype is context-independent
            valueObjs[i].init(
                xducer.generateDeserializer(
                    codeModel.ref(DatatypeConverterImpl.class)
                    .staticInvoke("installHook").arg(
                        JExpr.lit(lexical)), null ) );
            
        }
        
        
        // [RESULT]
        // private final String lexicalValue;
        JVar $lexical = type.field( JMod.PRIVATE|JMod.FINAL,
            stringRef, "lexicalValue" );
        // private final <valueType> value;
        JVar $value = type.field( JMod.PRIVATE|JMod.FINAL,
            valueType, "value" );
        
        
        // [RESULT]
        // protected <constructor>(<valueType> v) {
        //     this.value=v;
        //     this.lexicalValue=<serialize>(v);
        //     valueMap.put( v, this );
        // }
        {
            JMethod m = type.constructor(JMod.PROTECTED);
            JVar $v = m.param( valueType, "v" );
            m.body().assign( $value, $v );
            m.body().assign( $lexical, xducer.generateSerializer($v,null) );
            
            m.body().invoke($valueMap,"put").arg( wrapToObject($v) ).arg(JExpr._this());
        }
        
        // [RESULT]
        // public String toString() { return lexicalValue; }
        type.method(JMod.PUBLIC,stringRef,"toString").body()
            ._return($lexical);
        
        // [RESULT]
        // public <valueType> getValue() { return value; }
        type.method(JMod.PUBLIC,valueType,"getValue").body()
            ._return($value);
        
        // [RESULT]
        // public final int hashCode() { return super.hashCode(); }
        type.method(JMod.PUBLIC|JMod.FINAL,codeModel.INT,"hashCode").body()
            ._return(JExpr._super().invoke("hashCode"));
        
        // [RESULT]
        // public final boolean equals(Object o) { return equals(o); }
        {
            JMethod m = type.method(JMod.PUBLIC|JMod.FINAL,codeModel.BOOLEAN,"equals");
            JVar o = m.param(Object.class,"o");
            m.body()._return(JExpr._super().invoke("equals").arg(o));
        }
         
        
        // [RESULT]
        // public static <Type> fromValue( <valueType> value ) {
        //     <Type> t = (<Type>)valueMap.get(value);
        //     if(t==null)
        //         throw new IllegalArgumentException();
        //     else
        //         return t;
        // }
        {
            JMethod fromValue = type.method(JMod.PUBLIC|JMod.STATIC, type, "fromValue" );
            JVar $v = fromValue.param( valueType, "value" );
            
            JVar $t = fromValue.body().decl( type, "t",
                JExpr.cast( type, $valueMap.invoke("get").arg(wrapToObject($v))) );
            
            JConditional cond = fromValue.body()._if($t.eq(JExpr._null()));
            cond._then()._throw(JExpr._new(codeModel.ref(IllegalArgumentException.class)));
            cond._else()._return($t);
        }
        
        
        // [RESULT]
        // public static <Type> fromString( String str ) {
        //     ....
        // }
        JMethod fromString = type.method(JMod.PUBLIC|JMod.STATIC, type, "fromString" );
        JVar $str = fromString.param( stringRef, "str" );
        
        JExpression rhs = xducer.generateDeserializer($str,null);        
        fromString.body()._return(
            JExpr.invoke("fromValue").arg(rhs));
            
            
        if( grammar.serialVersionUID!=null ) {
            // serialization support
            type._implements(Serializable.class);
            
            // [RESULT]
            // private Object readResolve() {
            //    return fromValue(getValue());
            // }
            type.method(JMod.PRIVATE,objectRef,"readResolve")
                .body()._return(
                    JExpr.invoke("fromValue").arg( JExpr.invoke("getValue") )
                );

        }
    }
    
    private JExpression wrapToObject(JExpression var) {
        if( valueType.isPrimitive() ) {
            return ((JPrimitiveType)valueType).wrap(var);
        } else
            return var;
    }

    /**
     * Replaces illegal characters by punctutation.
     * 
     * This is a deviation from the appendix C.3, but
     * it will be backwarcd compatible.
     */
    private String fixUnsafeCharacters(String lexical) {
        StringBuffer buf = new StringBuffer();
        int len = lexical.length();
        for( int i=0; i<len; i++ ) {
            char ch = lexical.charAt(i);
            if(!Character.isJavaIdentifierPart(ch))
                buf.append('-');
            else
                buf.append(ch);
        }
        return buf.toString();
    }

    /**
     * Performs some error checks and return false if there's any error.
     * Used in the populate method.
     */
    private boolean sanityCheck( GeneratorContext context ) {
        for( int i=0; i<values.length; i++ ) {
            if(values[i].dt.isContextDependent()) {
                reportError( context, Messages.format(ERR_CONTEXT_DEPENDENT_TYPE) );
                return false; // can't populate.
            }
            if(!(values[i].dt instanceof XSDatatype))
                reportError( context,
                    Messages.format(ERR_UNSUPPORTED_TYPE_FOR_ENUM, values[i].getName()) );

            // make sure all the values belong to the same type
            if( !values[0].dt.equals(values[i].dt) ) {
                reportError( context,
                    Messages.format(ERR_MULTIPLE_TYPES_IN_ENUM,
                        values[0].name,
                        values[i].name ));
                return false; // can't recover from the error
            }
        }
        return true;
    }
    

    public JExpression generateSerializer(JExpression value, SerializerContext context) {
        return value.invoke("toString");
    }

    public JExpression generateDeserializer(JExpression value, DeserializerContext context) {
        return type.staticInvoke("fromString").arg(value);
    }
    
    public JExpression generateConstant( ValueExp exp ) {
        for( int i=0; i<values.length; i++ ) {
            if(exp.dt.sameValue(values[i].value,exp.value))
                return type.staticRef(items[i].name());
        }
        
        // there must be a constant that corresponds to the given value.
        throw new JAXBAssertionError();
    }
    
    /** Extracts ValueExps from the given expression. */
    private ValueExp[] getValues( Expression exp ) {
        if(!(exp instanceof ChoiceExp)) {
            if(!(exp instanceof ValueExp)) {
                System.out.println(ExpressionPrinter.printContentModel(exp));
                // it must be a choice or value exps.
                throw new InternalError("assertion failed");
            }
            return new ValueExp[]{(ValueExp)exp};
        } else {
            Expression[] children = ((ChoiceExp)exp).getChildren();
            ValueExp[] values = new ValueExp[children.length];
            System.arraycopy(children,0,values,0,children.length);
            return values;
        }
    }
    

    
    private void reportError( GeneratorContext context, String msg ) {
        context.getErrorReceiver().error(sourceLocator,msg);
    }
    
    
    private static final String ERR_CONTEXT_DEPENDENT_TYPE =
        "EnumerationXducer.ContextDependentType"; // arg: 0
    private static final String ERR_UNSUPPORTED_TYPE_FOR_ENUM =
        "EnumerationXducer.UnsupportedTypeForEnum"; // arg:1
    private static final String ERR_UNUSABLE_NAME =
        "EnumerationXducer.UnusableName"; // arg:2
    private static final String ERR_MULTIPLE_TYPES_IN_ENUM =
        "EnumerationXducer.MultipleTypesInEnum";    // arg:2
    private static final String ERR_NAME_COLLISION =
        "EnumerationXducer.NameCollision"; // arg:1
}
