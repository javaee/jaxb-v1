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

package com.sun.tools.xjc.generator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JVar;
import com.sun.msv.datatype.xsd.XSDatatype;
import com.sun.msv.grammar.ValueExp;
import com.sun.tools.xjc.generator.util.BlockReference;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.xducer.BuiltinDatatypeTransducerFactory;
import com.sun.tools.xjc.grammar.xducer.DeserializerContext;
import com.sun.tools.xjc.grammar.xducer.SerializerContext;
import com.sun.tools.xjc.grammar.xducer.Transducer;
import com.sun.tools.xjc.runtime.UnmarshallingContext;
import com.sun.tools.xjc.runtime.ValidatableObject;
import com.sun.xml.bind.ProxyGroup;

/**
 * List of {@link Entry} objects.
 * 
 * Used to enhance the performance of a choice based on a
 * signal attribute (such as xsi:type.)
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class LookupTable {
    
    static class Entry {
        private final ClassItem target;
        private final ValueExp valueExp;
//        private final QName datatypeName;
//        private final Object value;
//        
//        private final Datatype datatype;
        
        Entry(ClassItem _target, ValueExp _value) {
            this.target = _target;
            this.valueExp = _value;
//            this.datatypeName = _datatypeName;
//            this.value = _value;
//            
//            this.datatype = _datatype;
        }
        
        public int hashCode() {
            return target.hashCode() ^ valueExp.name.hashCode() ^ valueExp.dt.valueHashCode(valueExp.value);
        }
        
        public boolean equals( Object o ) {
            Entry rhs = (Entry)o;
            return this.target==rhs.target
                && this.valueExp.name.equals(rhs.valueExp.name)
                && this.valueExp.dt.sameValue(this.valueExp.value,rhs.valueExp.value);
        }
        
        /**
         * Returns true if this entry and the given entry are
         * inconsistent with each other. A table maynot contain
         * any inconsistent pair of Entries.
         */
        public boolean isConsistentWith( Entry rhs ) {
            if( this.equals(rhs) )
                return true;   // "same" entries are consistent
            
            // assuming they are not equal...
            if( this.target==rhs.target )
                return false;    // every entry in a table has to have a different value
            if( !this.valueExp.name.equals(rhs.valueExp.name) )
                return false;    // table must consists of one datatype alone
            if( this.valueExp.dt.sameValue(this.valueExp.value,rhs.valueExp.value) )
                return false;    // every entry in a table has to have a different value
            
            return true;    // otherwise consistent
        }
    }
    

    /**
     * {@link Entry}s of this table.
     */
    private final Set entries = new HashSet();
    
    /**
     * Unique number for each {@link LookupTable}.
     */
    private final int id;
    private final LookupTableFactory owner;
    
    // these fields will be set once the code is generated
    private JMethod $lookup,$reverseLookup,$add;
    private JFieldVar $map,$rmap;
    private Transducer xducer;
    private GeneratorContext genContext;
    
    LookupTable( LookupTableFactory _owner, int _id ) {
        this.owner = _owner;
        this.id = _id;
        
    }
    
    /**
     * Returns true if this table is consistent with the given table.
     * Consistent tables can be merged into one.
     */
    public boolean isConsistentWith(LookupTable rhs) {
        for (Iterator itr = entries.iterator(); itr.hasNext();) {
            Entry a = (Entry)itr.next();
            if(!rhs.isConsistentWith(a))
                return false;
        }

        return true;
    }
    
    public boolean isConsistentWith(Entry e) {
        for (Iterator itr = entries.iterator(); itr.hasNext();) {
            Entry a = (Entry)itr.next();
            if(!a.isConsistentWith(e))
                return false;
        }
        return true;
    }
    
    /**
     * Adds a new entry.
     * 
     * @param e
     *      must be consistent with this table.
     */
    public void add( Entry e ) {
        entries.add(e);
        if( $lookup!=null )    generateEntry(e);
    }
    
    /**
     * Merged the given table into this table.
     * 
     * @param rhs
     *      Two tables must be consistent. 
     */
    public void absorb(LookupTable rhs) {
        for (Iterator itr = rhs.entries.iterator(); itr.hasNext();) {
            Entry e = (Entry)itr.next();
            add(e);
        }
    }
    
    /**
     * Generated code fragment that looks up the table by the value.
     * 
     * @param literal
     *      evaluates to the value of the switch attribute.
     * @param unmContext
     *      evaluates to an UnmarshallingContext object.
     */
    public JExpression lookup( GeneratorContext context, JExpression literal, JExpression unmContext ) {
        if( $lookup==null ) generateCode(context);
        
        return owner.getTableClass().staticInvoke($lookup).arg(literal).arg(unmContext);
    }
    
    /**
     * Looks up the value of the switch attribute from an instance of the object.
     * 
     * @param obj
     *      evaluates to an object created from this table look-up dispatching.
     * @return
     *      evaluates to the lexical representation of the switch att value.
     */
    public JExpression reverseLookup( JExpression obj, SerializerContext serializer ) {
        return xducer.generateSerializer(
            owner.getTableClass().staticInvoke($reverseLookup).arg(obj),
            serializer );
    }
    
    /**
     * Declares namespace URIs if the value of the switch attribute needs them.
     */
    public void declareNamespace( BlockReference body, JExpression value, SerializerContext serializer ) {
        xducer.declareNamespace(
            body,
            owner.getTableClass().staticInvoke($reverseLookup).arg(value),
            serializer );
    }

    
    /**
     * Generates the table definition and the look-up function into CodeModel.
     */
    private void generateCode( GeneratorContext context ) {
        this.genContext = context;
        
        
        JDefinedClass table = owner.getTableClass();
        JCodeModel codeModel = table.owner();
        
        $map = table.field(JMod.PRIVATE|JMod.STATIC|JMod.FINAL,Map.class,"table"+id,
            JExpr._new(codeModel.ref(HashMap.class)));
        $rmap = table.field(JMod.PRIVATE|JMod.STATIC|JMod.FINAL,Map.class,"rtable"+id,
            JExpr._new(codeModel.ref(HashMap.class)));
        
        Entry[] e = (Entry[]) entries.toArray(new Entry[entries.size()]);
        
        // all the datatypes must be the same, so we can use the same Xducer.
        xducer = BuiltinDatatypeTransducerFactory.get(
            context.getGrammar(),(XSDatatype)e[0].valueExp.dt);
        
        for( int i=0; i<e.length; i++ )
            generateEntry(e[i]);
            
        // [RESULT]
        // public Class lookup<id>( String literal, UnmarshallingContext context ) {
        //     return (Class)table<id>.get(<deserialize>(literal));
        // }
        {
            $lookup = table.method(JMod.PUBLIC|JMod.STATIC|JMod.FINAL,Class.class,"lookup"+id);
            JVar $literal = $lookup.param(String.class,"literal");
            DeserializerContext dc = new XMLDeserializerContextImpl(
                $lookup.param(context.getRuntime(UnmarshallingContext.class),"context"));
            
            $lookup.body()._return(JExpr.cast(codeModel.ref(Class.class),
                $map.invoke("get").arg(
                    xducer.generateDeserializer($literal,dc))));
        }
        
        // [RESULT]
        // template < class T >
        // public T reverseLookup<id>( Object o ) {
        //    return (T)rtable<id>.get( ProxyGroup.blindWrap(o,...).getClass() );
        // }
        {
            $reverseLookup = table.method(JMod.PUBLIC|JMod.STATIC|JMod.FINAL,xducer.getReturnType(),"reverseLookup"+id);
            JVar $o = $reverseLookup.param(Object.class,"o");
            
            $reverseLookup.body()._return(
                JExpr.cast( xducer.getReturnType(),
                    $rmap.invoke("get").arg(
                        codeModel.ref(ProxyGroup.class).staticInvoke("blindWrap")
                            .arg($o)
                            .arg(context.getRuntime(ValidatableObject.class).dotclass())
                            .arg(JExpr._null())
                        .invoke("getClass") )));
        }
                
        // [RESULT]
        // private static void add<id>( Object key, Object value ) {
        //    map.put(key,value);
        //    rmap.put(value,key);
        // }
        {
            $add = table.method(JMod.PRIVATE|JMod.STATIC,codeModel.VOID,"add"+id);
            JVar $key = $add.param(Object.class,"key"); 
            JVar $value = $add.param(Object.class,"value"); 

            $add.body().invoke($map,"put").arg($key).arg($value);
            $add.body().invoke($rmap,"put").arg($value).arg($key);
        }
    }
    
    private void generateEntry( Entry e ) {
        owner.getTableClass().init().invoke("add"+id)
            .arg(xducer.generateConstant(e.valueExp))
            .arg(genContext.getClassContext(e.target).implRef.dotclass());
    }
}
