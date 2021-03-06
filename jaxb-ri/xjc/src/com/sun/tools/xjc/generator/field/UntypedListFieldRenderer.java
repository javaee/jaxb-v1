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

import java.util.List;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JForLoop;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JOp;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.generator.ClassContext;
import com.sun.tools.xjc.generator.JavadocBuilder;
import com.sun.tools.xjc.grammar.FieldUse;

/**
 * Realizes a property as an untyped {@link List}.
 * 
 * <pre>
 * List getXXX();
 * </pre>
 * 
 * <h2>Default value handling</h2>
 * <p>
 * Since unmarshaller just adds new values into the storage,
 * we can't fill the storage by default values at the time of
 * instanciation. (or oherwise values found in the document will
 * be appended to default values, where it should overwrite them.)
 * <p>
 * Therefore, when the object is created, the storage will be empty.
 * When the getXXX method is called, we'll check if the storage is
 * modified in anyway. If it is modified, it must mean that the values
 * are found in the document, so we just return it.
 * 
 * Otherwise we will fill in default values and return it to the user.
 * 
 * <p>
 * When a list has default values, its dirty flag is set to true.
 * Marshaller will check this and treat it appropriately.
 * 
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class UntypedListFieldRenderer extends AbstractListFieldRenderer {

    protected UntypedListFieldRenderer( ClassContext context, FieldUse fu, JClass coreList ) {
        super(context,fu,coreList);
    }
    
    /**
     * FieldRendererFactory implementation that returns
     * UntypedListFieldRenderer.
     */
    public final static class Factory implements FieldRendererFactory {
        /**
         * @param coreList
         *      A concrete class that implements the List interface.
         *      An instance of this class will be used to store data
         *      for this field.
         */
        public Factory( JClass coreList ) {
            this.coreList = coreList;
        }
        private final JClass coreList;
        public FieldRenderer create(ClassContext context, FieldUse fu) {
            return new UntypedListFieldRenderer(context,fu,coreList);
        }
    }
        
    public void generateAccessors() {                            
        JBlock body;            
        // [RESULT]
        // List getXXX() {
        // #ifdef default value
        //     if(!<ref>.isModified() && <ref>.isEmpty() ) {
        //         // fill in the default values
        //         for( int i=0; i<defaultValues.length; i++ )
        //             <ref>.add(box(defaultValues[i]));
        //         <ref>.setModified(false);
        //     }
        // #endif
        //     return <ref>;
        // }
        JMethod $get = writer.declareMethod(List.class,"get"+fu.name);
        writer.javadoc().appendComment(fu.getJavadoc());
        body = $get.body();
        if($defValues!=null) {
            JBlock then = body._if(
                JOp.cand( hasSetValue().not(), ref(false).invoke("isEmpty") ) )._then();
            JForLoop loop = then._for();
            JVar $i = loop.init(codeModel.INT,"__i",JExpr.lit(0));
            loop.test($i.lt($defValues.ref("length")));
            loop.update($i.incr());
            loop.body().invoke(ref(true),"add").arg(box($defValues.component($i)));
            
            then.invoke(ref(true),"setModified").arg(JExpr.FALSE);
        }
        body._return(ref(false));
        
        
        writer.javadoc().appendComment(
            "Gets the value of the "+fu.name+" property.\n\n"+
            "<p>\n" +
            "This accessor method returns a reference to the live list,\n" +
            "not a snapshot. Therefore any modification you make to the\n" +
            "returned list will be present inside the JAXB object.\n" +
            "This is why there is not a <CODE>set</CODE> method for the " +fu.name+ " property.\n" +
            "\n"+
            "<p>\n" +
            "For example, to add a new item, do as follows:\n"+
            "<pre>\n"+
            "   get"+fu.name+"().add(newItem);\n"+
            "</pre>\n"+
            "\n\n"
        );
        
        writer.javadoc().appendComment(
            "<p>\n" +
            "Objects of the following type(s) are allowed in the list\n"+
            JavadocBuilder.listPossibleTypes(fu));
        
        
        // [RESULT]
        // #ifdef default value
        // void deleteXXX() {
        //     <ref>.clear();
        //     <ref>.setModified(false);
        // }
        // #endif
        // setModified(false) so that the getXXX method will correctly
        // recognize that we need to fill in default values again.
        if($defValues!=null) {
             JMethod $delete = writer.declareMethod(codeModel.VOID, "delete"+fu.name);
             writer.javadoc().setDeprecated(
                "this method is incorrectly generated by previous\n" +
                "releases of the RI. This method remains here just to make \n" +
                "the generated code backward compatible.\n" +
                "Applications should <strong>NOT</strong> rely on this method, and\n" +
                "if it needs this capability, it should use the unset"+fu.name+" method.\n" +
                "To generate the unset"+fu.name+" method, please use \n" +
                "the <code>generateIsSetMethod</code> attribute on\n" +
                "<code>globalBindings</code> or <code>property</code> customization." );
            unsetValues($delete.body());
        }
    }
}       
