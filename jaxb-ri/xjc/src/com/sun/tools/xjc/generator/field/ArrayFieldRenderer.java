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
