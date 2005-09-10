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
package com.sun.tools.xjc.generator.field;

import java.util.ArrayList;
import java.util.Iterator;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JOp;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.generator.ClassContext;
import com.sun.tools.xjc.generator.JavadocBuilder;
import com.sun.tools.xjc.grammar.FieldUse;
import com.sun.xml.bind.util.EmptyIterator;

/**
 * Realizes a property as a {@link java.util.List} and
 * exposes methods for static typed access.
 * 
 * <pre>
 * T getXXX(int idx);
 * Iterator iterateXXX();
 * T setXXX(int idx, T newValue);
 * ...
 * </pre>
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class TypedListFieldRenderer extends AbstractListFieldRenderer {
    
    /**
     * FieldRendererFactory implementation that returns
     * TypedListFieldRenderer.
     */
    public final static FieldRendererFactory theFactory =
        new FieldRendererFactory() {
            public FieldRenderer create(ClassContext context, FieldUse fu) {
                return new TypedListFieldRenderer(
                    context, fu, fu.codeModel.ref(ArrayList.class) );
            }
        };
        
    protected TypedListFieldRenderer( ClassContext context, FieldUse fu, JClass coreList ) {
        super(context,fu,coreList);
    }
    
    public void generateAccessors() {
                        
        JVar $idx,$value; JBlock body;
                        
        // [RESULT] void addX(int,X)
        JMethod $add = writer.declareMethod(codeModel.VOID,"add"+fu.name);
        $idx = writer.addParameter(codeModel.INT,"idx");
        $value = writer.addParameter(fu.type,"value");

        writer.javadoc().appendComment(fu.getJavadoc());
                        
        body = $add.body();
        body.invoke(ref(false),"add")
            .arg($idx).arg($value);

        writer.javadoc().addParam( $value,
            "allowed object is\n"+JavadocBuilder.listPossibleTypes(fu));

                        
        // [RESULT]
        // X getX(int) {
        //   return (X)<field>.get(idx);
        // }
        JMethod $get = writer.declareMethod(fu.type,"get"+fu.name);
        $idx = writer.addParameter(codeModel.INT,"idx");

        writer.javadoc().appendComment(fu.getJavadoc());
        
        // if the field is null, then the size of the list is null,
        // so the operation will fail anyway. no need for the initialization check here.
        $get.body()._return(JExpr.cast(fu.type,
            ref(true).invoke("get").arg($idx)));

        writer.javadoc().addReturn(JavadocBuilder.listPossibleTypes(fu));
        
                        
        // [RESULT] Iterator iterateX()
        JMethod $iterate = writer.declareMethod(
            codeModel.ref(Iterator.class),
            "iterate"+fu.name);
        writer.javadoc().appendComment(fu.getJavadoc());
        $iterate.body()._return(
            JOp.cond( ref(true).eq(JExpr._null()),
                codeModel.ref(EmptyIterator.class).staticRef("theInstance"),
                ref(true).invoke("iterator") ));
                        
        // [RESULT] int sizeOfX()
        JMethod $size = writer.declareMethod(
            codeModel.INT,
            "sizeOf"+fu.name);
        $size.body()._return(count());
                        
        // [RESULT] X setX(int,X)
        JMethod $set = writer.declareMethod(
            fu.type,
            "set"+fu.name);
        $idx = writer.addParameter( codeModel.INT, "idx" );
        $value = writer.addParameter( fu.type, "value" );
                        
        writer.javadoc().appendComment(fu.getJavadoc());
        
        body = $set.body();
        body._return( JExpr.cast(fu.type,
            ref(false).invoke("set").arg($idx).arg($value)));

        writer.javadoc().addParam($value,
            "allowed object is\n"+JavadocBuilder.listPossibleTypes(fu));

    }
}
