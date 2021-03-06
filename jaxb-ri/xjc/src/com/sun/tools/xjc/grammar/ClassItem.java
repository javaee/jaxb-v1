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

package com.sun.tools.xjc.grammar;

import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.xml.sax.Locator;

import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JType;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ReferenceExp;

/**
 * Represents a generated class.
 * 
 * @author
 *    <a href="mailto:kohsuke.kawaguchi@sun.com">Kohsuke KAWAGUCHI</a>
 */
public final class ClassItem extends TypeItem {
    
    /**
     * This object should be created only by {@link AnnotatedGrammar}.
     */
    protected ClassItem( AnnotatedGrammar _owner, JDefinedClass _type, Expression exp, Locator loc ) {
        super(_type.name(),loc);
        this.type = _type;
        this.owner = _owner;
        this.exp = exp;
    }
    
    private final JDefinedClass type;
    public JType getType() { return type; }
    
    /**
     * The same as getType (except that the retuend type is JDefinedClass).
     */
    public JDefinedClass getTypeAsDefined() {
        // ClassItem is guaranteed to have a JDefinedClass
        return type;
    }
    
    /**
     * If this field is specified, the backend should instanciate
     * this class instead of the default implementation.
     */
    private String userSpecifiedImplClass;



    
    
    /** This ClassItem is owned by this AnnotatedGrammar. */
    public final AnnotatedGrammar owner;
    
    /** a map of field name to FieldUse */
    private final Map fields = new java.util.HashMap();
    
    /** Gets the field use declared in this class with a given name. */
    public final FieldUse getDeclaredField( String name ) {
        return (FieldUse)fields.get(name);
    }
    
    /**
     * Gets the field use declared in this class or its ancestor classes.
     */
    public final FieldUse getField( String name ) {
        FieldUse fu = getDeclaredField(name);
        if(fu!=null)    return fu;
        
        if(superClass!=null)
            return getSuperClass().getField(name);
        return null;
    }
    
    /**
     * Enumerates all {@link FieldUse}s declared in this class.
     * This doesn't include FieldUses declared in ancestor classes.
     */
    public final FieldUse[] getDeclaredFieldUses() {
        return (FieldUse[])fields.values().toArray(new FieldUse[fields.size()]);
    }
    
    /** Internal use only. */
    public FieldUse getOrCreateFieldUse( String name ) {
        FieldUse r = (FieldUse)fields.get(name);
        if(r==null)    fields.put(name,r=new FieldUse(name,this));
        return r;
    }
    
    /**
     * Removes FieldUses that are already realized through the ancestor types.
     * This method is called by RelationNormalizer.
     */
    public void removeDuplicateFieldUses() {
        ClassItem superClass = getSuperClass();
        if(this.superClass==null)   return;
        
        FieldUse[] fu = getDeclaredFieldUses();
        for( int i=0; i<fu.length; i++ ) {
            if(superClass.getField(fu[i].name)!=null)
                // if this field is already defined in the ancestor classes,
                // remove it
                fields.remove(fu[i].name);
        }
    }
    
	
    /**
     * Expression for AGM.
     * <p>
     * Sometimes, to simply the marshaller/unmarshaller the front-end
     * can choose to produce a simpler form of BGM by cutting certain
     * details. For example, maxOccurs="2" is essentially equivalent to
     * maxOccurs="unbounded" as far as the code generation is concerned.
     * In fact, this would reduce the size of unmarshaller/marshaller.
     * <p>
     * As a result, the <code>exp</code> field of a ClassItem may not
     * necessarily contain the precise expression of the source schema.
     * But this is problematic to the validator, which needs the exact
     * source schema.
     * <p>
     * This field keeps a reference to the precise AGM expression (plus
     * child ClassItems) that this class needs to follow. If <code>agm.exp</code>
     * is null, the <code>exp</code> field will be used.
     */
    public final ReferenceExp agm = new ReferenceExp(null);
    
    
    /** Constructor declarations. array of {@link Constructor}s. */
    private final Vector constructors = new Vector();
    
    /** Creates a new constructor declaration and adds it. */
    public void addConstructor( String[] fieldNames ) {
        constructors.add(new Constructor(fieldNames));
    }
    
    /** Iterates all constructor declarations. */
    public Iterator iterateConstructors() {
        return constructors.iterator();
    }
    
    
    /**
     * Set this field to true to get the backend generate the "getContent"
     * field.
     */
    public boolean hasGetContentMethod = false;
    
    
    /** Super class definition, if any. */
    public SuperClassItem superClass;
    
    public ClassItem getSuperClass() {
        if(superClass==null)    return null;
        return superClass.definition;
    }
    
    
    public Object visitJI( JavaItemVisitor visitor ) {
        return visitor.onClass(this);
    }


    protected boolean calcEpsilonReducibility() {
        // Even if the body of the class is null,
        // its presence is significant to its parent.
        // thus we always return false to make sure that
        // the parent believes this pattern is significant
        // and cannot be nullable.
        return false;
    }


    public String getUserSpecifiedImplClass() {
        return userSpecifiedImplClass;
    }

    public void setUserSpecifiedImplClass(String userSpecifiedImplClass) {
        this.userSpecifiedImplClass = userSpecifiedImplClass;
    }
}
