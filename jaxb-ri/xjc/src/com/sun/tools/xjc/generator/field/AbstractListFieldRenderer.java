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

import java.util.List;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JExpressionImpl;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JForLoop;
import com.sun.codemodel.JFormatter;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JOp;
import com.sun.codemodel.JPrimitiveType;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.generator.ClassContext;
import com.sun.tools.xjc.generator.marshaller.FieldMarshallerGenerator;
import com.sun.tools.xjc.generator.util.BlockReference;
import com.sun.tools.xjc.grammar.DefaultValue;
import com.sun.tools.xjc.grammar.FieldUse;
import com.sun.xml.bind.util.ListImpl;

/**
 * Common code for property renderer that generates a List as
 * its underlying data structure.
 * 
 * <p>
 * For performance reaons, the actual list object used to store
 * data is lazily created.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
abstract class AbstractListFieldRenderer extends AbstractFieldRenderer {

    /**
     * Reference to the array of default values, if there is a default value.
     * Otherwise null.
     */
    protected JVar $defValues = null;
       
    /**
     * Concrete class that implements the List interface.
     * Used as the actual data storage.
     */
    private final JClass coreList; 
    
    /**
     * If this collection property is a collection of a primitive type,
     * this variable refers to that primitive type.
     */
    protected JPrimitiveType primitiveType;
    
    /**
     * Once the onSetHandler method is requested,
     * this field will store that JBlock.
     */
    private JBlock onSetHandler;
    /**
     * Expression object that represents how a new List object
     * should be built.
     */
    private JExpression newListObjectExp;
    
    
    /** The field that stores the list. */
    private JFieldVar field;
    
    /**
     * Function of the form:
     * {
     *      if(field==null)
     *          field = create new list;
     *      return field;
     * }
     */
    private JMethod internalGetter;
    
    
    protected AbstractListFieldRenderer( ClassContext context, FieldUse fu, JClass coreList ) {
        super(context,fu);
        this.coreList = coreList;
    
        if( fu.type instanceof JPrimitiveType )
            primitiveType = (JPrimitiveType)fu.type;
    }
    
    
    protected final JExpression unbox( JExpression exp ) {
        if(primitiveType==null) return exp;
        else                    return primitiveType.unwrap(exp);
    }
    protected final JExpression box( JExpression exp ) {
        if(primitiveType==null) return exp;
        else                    return primitiveType.wrap(exp);
    }
    

    public JBlock getOnSetEventHandler() {
        if(onSetHandler!=null)  return onSetHandler;
        
        // allocate the onSetHandler.
        // change the initializer to
        // [RESULT]
        // protected List _X = new ListImpl(core) {
        //   void setModified( boolean f ) {
        //     supser.setModified(f);
        //     if(f) {
        //       <modification handler>;
        //     }
        //   }
        // }; 
        
        JDefinedClass anonymousClass = codeModel.newAnonymousClass(
            codeModel.ref(ListImpl.class));
        newListObjectExp = JExpr._new(anonymousClass).arg(JExpr._new(coreList));
        
        JMethod method = anonymousClass.method(JMod.PUBLIC,codeModel.VOID,"setModified");
        JVar $f = method.param(codeModel.BOOLEAN,"f");
        
        method.body().invoke(JExpr._super(),"setModified").arg($f);
        onSetHandler = method.body()._if($f)._then();
        
        return onSetHandler;
    }

    public JClass getValueType() {
        return codeModel.ref(List.class);
    }

    
    public final void generate() {
        this.field=generateField();
        
        // create a method that lazily initializes a List
        internalGetter = context.implClass.method(JMod.PROTECTED,ListImpl.class,"_get"+fu.name);
        internalGetter.body()._if(field.eq(JExpr._null()))._then()
            .assign(field,lazyInitializer);
        internalGetter.body()._return(field);
        
        // generate the rest of accessors
        generateAccessors();
    }

    
    private JExpression lazyInitializer = new JExpressionImpl() {
        public void generate(JFormatter f) {
            newListObjectExp.generate(f);
        }
    };
    
    /**
     * Returns a reference to the List field that stores the data.
     * <p>
     * Using this method hides the fact that the list is lazily
     * created.
     * 
     * @param canBeNull
     *      if true, the returned expression may be null (this is
     *      when the list is still not constructed.) This could be
     *      useful when the caller can deal with null more efficiently.
     *      When the list is null, it should be treated as if the list
     *      is empty.
     * 
     *      if false, the returned expression will never be null.
     *      This is the behavior users would see.
     */
    protected final JExpression ref(boolean canBeNull) {
        if(canBeNull)
            return field;
        else
            return JExpr.invoke(internalGetter);
    }

    /** Generates accessor methods. */
    public abstract void generateAccessors();
    
    protected final JFieldVar generateField() {
        DefaultValue[] defaultValues = fu.getDefaultValues();
        
        // it's little less efficient but to detect a change in the list,
        // we need to use ListImpl. Since we don't know if someone wants to
        // install a hook or not until later, we can't tell if a normal list
        // is suffice or if we need a ListImpl.
        
        JClass list = codeModel.ref(ListImpl.class);
        JFieldVar ref = generateField(list);
        newListObjectExp = JExpr._new(list).arg(JExpr._new(coreList));
            
        // generate default values
        if(defaultValues!=null) {
            JInvocation initializer;
            JType arrayType = fu.type.array();
            // if there are default values, create an array for them.
            
            // [RESULT] static final protected T[] XX_defaultValues = new T[]{...}
            $defValues = context.implClass.field(JMod.STATIC|JMod.FINAL|JMod.PROTECTED,
                arrayType,fu.name+"_defaultValues",
                initializer=JExpr._new(arrayType));
            
            for( int i=0; i<defaultValues.length; i++ )
               initializer.arg( defaultValues[i].generateConstant() );
        }
        
        return ref;
    }

    public void setter( JBlock body, JExpression newValue ) {
        if( primitiveType!=null )
            newValue = primitiveType.wrap(newValue);
        body.invoke(ref(false),"add").arg(newValue);
    }

    public void toArray( JBlock block, JExpression $array ) {
        // if the list is null, no need to copy to the array
        block = block._if( field.ne(JExpr._null()) )._then();
        
        if( primitiveType==null ) {
            // [RESULT]
            // list.toArray( array );
            block.invoke( ref(true), "toArray" ).arg($array);
        } else {
            // [RESULT]
            // for( int idx=<length>-1; idx>=0; idx-- ) {
            //     array[idx] = <unbox>(list.get(<idx>));
            // }
            JForLoop $for = block._for();
            JVar $idx = $for.init(codeModel.INT,"q"+this.hashCode(), count().minus(JExpr.lit(1)) );
            $for.test( $idx.gte(JExpr.lit(0)) );
            $for.update( $idx.decr() );
            
            $for.body().assign( $array.component($idx),
                primitiveType.unwrap(
                    JExpr.cast( primitiveType.getWrapperClass(), ref(true).invoke("get").arg($idx) )));
        }
    }

    public JExpression count() {
        return JOp.cond( field.eq(JExpr._null()), JExpr.lit(0), field.invoke("size") );
    }
    public JExpression ifCountEqual( int i ) {
        return count().eq(JExpr.lit(i));
    }
    public JExpression ifCountGte( int i ) {
        return count().gte(JExpr.lit(i));
    }
    public JExpression ifCountLte( int i ) {
        return count().lte(JExpr.lit(i));
    }
    
    private class FMGImpl implements FieldMarshallerGenerator {
        FMGImpl( JVar _$idx, JVar _$len ) {
            $idx=_$idx; $len=_$len;
        }
        
        private final JVar $idx;
        private final JVar $len;
        
        public JExpression hasMore() {
            // [RESULT] idx!=len (or idx<len)
            return $idx.ne($len);
        }
        public JExpression peek(boolean increment) {
            // [RESULT] <var>.get(idx++);
            JExpression e = increment?$idx.incr():$idx;
            
            e = ref(true).invoke("get").arg(e);
            
            if(primitiveType!=null)
                // [RESULT] ((Integer)<var>.get(idx)).intValue();
                // the result is always typed.
                return primitiveType.unwrap(
                    JExpr.cast(primitiveType.getWrapperClass(),e));
            
            return e;
        }
        public void increment(BlockReference block) {
            block.get(true).assignPlus($idx,JExpr.lit(1));
        }
        public FieldMarshallerGenerator clone( JBlock block, String uniqueId ) {
            // [RESULT] int idx<newid> = idx<id>;
            JVar $newidx = block.decl(
                codeModel.INT, "idx"+uniqueId, $idx );
            return new FMGImpl($newidx,$len);
            // we can reuse the same length parameter
        }
        public FieldRenderer owner() { return AbstractListFieldRenderer.this; }
    }
    public FieldMarshallerGenerator createMarshaller(
            final JBlock block, final String uniqueId) {
        
        // [RESULT] int idx<id> = 0;
        //          #ifdef default value
        //              int len<id> = <var>!=null && <var>.isModified()?<var>.size():0;
        //          #else
        //              int len<id> = <var>.size();
        //          #endif
        JVar $idx = block.decl(
            codeModel.INT, "idx"+uniqueId, JExpr.lit(0) );
        JVar $len = block.decl( JMod.FINAL,
            codeModel.INT, "len"+uniqueId,
            ($defValues!=null)?
                JOp.cond( field.ne(JExpr._null()).cand(field.invoke("isModified")),
                    field.invoke("size"),
                    JExpr.lit(0))
            : count() );
        
        return new FMGImpl($idx,$len);
    }


    
    public void unsetValues( JBlock body ) {
        body = body._if(field.ne(JExpr._null()))._then();
        
        body.invoke( field, "clear" );
        
        // clear the list and turn off the setModified flag.
        // getter method will fill in default values later
        // when it is necessary. So don't need to do that here.
        body.invoke( field, "setModified" ).arg(JExpr.FALSE);
    }
    public JExpression hasSetValue() {
        return JOp.cond(field.eq(JExpr._null()),
            JExpr.FALSE, field.invoke("isModified") );
    }
    public JExpression getValue() {
        return ref(false);
    }

}
