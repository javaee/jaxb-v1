/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.generator.field;

import java.util.ArrayList;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JForLoop;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JOp;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.generator.ClassContext;
import com.sun.tools.xjc.generator.JavadocBuilder;
import com.sun.tools.xjc.grammar.FieldUse;

/**
 * Realizes a property as an "indexed property"
 * as specified in the JAXB spec.
 * 
 * <p>
 * We will generate the following set of methods:
 * <pre>
 * T[] getX();
 * T getX( int idx );
 * void setX(T[] values);
 * void setX( int idx, T value );
 * </pre>
 * 
 * We still use List as our back storage.
 * This renderer also handles boxing/unboxing if
 * T is a boxed type.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class ArrayFieldRenderer extends AbstractListFieldRenderer {
    
    /**
     * FieldRendererFactory implementation that returns
     * ArrayFieldRenderer.
     */
    public final static FieldRendererFactory theFactory =
        new FieldRendererFactory() {
            public FieldRenderer create(ClassContext cc, FieldUse fu) {
                return new ArrayFieldRenderer(
                    cc, fu, fu.codeModel.ref(ArrayList.class) );
            }
        };

    protected ArrayFieldRenderer( ClassContext cc, FieldUse fu, JClass coreList ) {
        super(cc,fu,coreList);
    }

    public void generateAccessors() {
        
        JVar $idx,$value; JBlock body;
        JType arrayType = fu.type.array();
        
        // type used in the method signature
        JType exposedType = fu.type;

        // type used to store values in a list
        JType internalType = primitiveType!=null ? primitiveType.getWrapperClass() : fu.type;
        
                        
        // [RESULT] T[] getX() {
        // #ifdef default value
        //     if( !<var>.isModified() ) {
        //         T[] r = new T[defaultValues.length];
        //         System.arraycopy( defaultValues, 0, r, 0, r,length );
        //         return r;
        //     }
        // #else
        //     if( <var>==null )    return new T[0];
        // #endif
        //     return (T[]) <var>.toArray(new T[<var>.size()]);
        // }
        JMethod $get = writer.declareMethod( exposedType.array(),"get"+fu.name);
        writer.javadoc().appendComment(fu.getJavadoc());
        body = $get.body();
        
        if($defValues!=null) {
            JBlock then = body._if( hasSetValue().not() )._then();
            JVar $r = then.decl( exposedType.array(), "r",JExpr.newArray(exposedType, $defValues.ref("length")));
            
            // [RESULT]
            // System.arraycopy( defaultValues, 0, r, 0, defaultValues.length );
            then.staticInvoke( codeModel.ref(System.class), "arraycopy")
                .arg( $defValues ).arg( JExpr.lit(0) )
                .arg( $r ).arg( JExpr.lit(0) ).arg( $defValues.ref("length") );
//            } else {
//                // need to copy them manually to unbox values
//                // [RESULT]
//                // for( int i=0; i<r.length; i++ )
//                //     r[i] = defaultValues[i];
//                JForLoop loop = then._for();
//                JVar $i = loop.init(codeModel.INT,"__i",JExpr.lit(0));
//                loop.test($i.lt($r.ref("length")));
//                loop.update($i.incr());
//                loop.body().assign( $r.component($i), unbox($defValues.component($i)) );
//            }
            then._return($r);   
        } else {
            body._if( ref(true).eq(JExpr._null()) )._then()
                ._return(JExpr.newArray(exposedType,0));
        }
        
        if(primitiveType==null) {
            body._return(JExpr.cast(arrayType,
                ref(true).invoke("toArray").arg( JExpr.newArray(fu.type,ref(true).invoke("size")) )));
        } else {
            // need to copy them manually to unbox values
            // [RESULT]
            // T[] r = new T[<ref>.size()];
            // for( int i=0; i<r.length; i++ )
            //     r[i] = unbox(<ref>.get(i));
            JVar $r = body.decl(exposedType.array(),"r",JExpr.newArray(exposedType, ref(true).invoke("size")));
            JForLoop loop = body._for();
            JVar $i = loop.init(codeModel.INT,"__i",JExpr.lit(0));
            loop.test($i.lt($r.ref("length")));
            loop.update($i.incr());
            loop.body().assign( $r.component($i),
                primitiveType.unwrap(JExpr.cast( internalType, ref(true).invoke("get").arg($i))) );
            body._return($r);
        }
        
        writer.javadoc().addReturn("array of\n"+JavadocBuilder.listPossibleTypes(fu));
                        
        // [RESULT]
        // ET getX(int idx) {
        // #ifdef default value
        //     if( !<var>.isModified() ) {
        //         return defaultValues[idx];
        //     }
        // #else
        //     if( <var>==null )    throw new IndexOutOfBoundsException();
        // #endif
        //     return (ET)unbox(<var>.get(idx));
        // }
        $get = writer.declareMethod(exposedType,"get"+fu.name);
        $idx = writer.addParameter(codeModel.INT,"idx");
        
        if($defValues!=null) {
            JBlock then = $get.body()._if( hasSetValue().not() )._then();
            then._return($defValues.component($idx));
        } else {
            $get.body()._if(ref(true).eq(JExpr._null()))._then()
                ._throw(JExpr._new(codeModel.ref(IndexOutOfBoundsException.class)));
        }
                    
        writer.javadoc().appendComment(fu.getJavadoc());
        $get.body()._return(unbox(JExpr.cast( internalType, ref(true).invoke("get").arg($idx) )));

        writer.javadoc().addReturn(
            "one of\n"+JavadocBuilder.listPossibleTypes(fu));

                        
        // [RESULT] int getXLength() {
        // #ifdef default values
        //     if( !storage.isModified() )
        //         return defaultValues.length;
        // #else
        //     if( <var>==null )    throw new IndexOutOfBoundsException();
        // #endif
        //     return <ref>.size();
        // }
        JMethod $getLength = writer.declareMethod(codeModel.INT,"get"+fu.name+"Length");
        if($defValues!=null) {
            $getLength.body()._if( hasSetValue().not() )._then()
                ._return($defValues.ref("length"));
        } else {
            $getLength.body()._if(ref(true).eq(JExpr._null()))._then()
                ._return(JExpr.lit(0));
        }
        $getLength.body()._return(ref(true).invoke("size"));
        
                        
        // [RESULT] void setX(ET[] values) {
        //     clear();
        //     int len = values.length;
        //     for( int i=0; i<len; i++ )
        //         <ref>.add(values[i]);
        // }
        JMethod $set = writer.declareMethod(
            codeModel.VOID,
            "set"+fu.name);
        
        writer.javadoc().appendComment(fu.getJavadoc());
        
        $value = writer.addParameter(exposedType.array(),"values");
        $set.body().invoke(ref(false),"clear");
        JVar $len = $set.body().decl(codeModel.INT,"len", $value.ref("length"));
        JForLoop _for = $set.body()._for();
        JVar $i = _for.init( codeModel.INT, "i", JExpr.lit(0) );
        _for.test( JOp.lt($i,$len) );
        _for.update( $i.incr() );
        _for.body().invoke(ref(true),"add").arg(box($value.component($i)));

        writer.javadoc().addParam( $value,
            "allowed objects are\n"+JavadocBuilder.listPossibleTypes(fu));
                        
        // [RESULT] ET setX(int,ET)
        $set = writer.declareMethod(
            exposedType,
            "set"+fu.name);
        $idx = writer.addParameter( codeModel.INT, "idx" );
        $value = writer.addParameter( exposedType, "value" );

        writer.javadoc().appendComment(fu.getJavadoc());
                        
        body = $set.body();
        body._return( unbox(JExpr.cast(internalType,
            ref(true).invoke("set").arg($idx).arg(box($value)))));

        writer.javadoc().addParam( $value,
            "allowed object is\n"+JavadocBuilder.listPossibleTypes(fu));
    }

}
