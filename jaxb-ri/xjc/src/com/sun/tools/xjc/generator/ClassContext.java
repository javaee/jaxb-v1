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

/*
 * Use is subject to the license terms.
 */
package com.sun.tools.xjc.generator;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JDefinedClass;
import com.sun.tools.xjc.generator.cls.ImplStructureStrategy;
import com.sun.tools.xjc.grammar.ClassItem;

/**
 * Context object that provides per-{@link ClassItem} information
 * for filling in methods/fields for a class item.
 * 
 * This interface is accessible from {@link GeneratorContext}
 * 
 * @author Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public final class ClassContext {
    
    /**
     * A {@link GeneratorContext} encloses all the class contexts.
     */
    public final GeneratorContext parent;
    
    /**
     * {@link PackageContext} that hosts this class.
     */
    public final PackageContext _package;
    
    /**
     * This {@link ClassContext} holds information about this {@link ClassItem}.
     */
    public final ClassItem target;
    
    /**
     * The implementation class that shall be used for reference.
     * <p>
     * Usually this field holds the same value as the impl method,
     * but sometimes it holds the user-specified implementation class
     * when it is specified.
     */
    public final JClass implRef;
    
    /**
     * The default implementation class for a given ClassItem.
     * The actual place where fields/methods should be generated into.
     */
    public final JDefinedClass implClass;
    
    /**
     * The publicly visible type of the given ClassItem.
     * 
     * implClass is always assignable to this type. 
     * <p>
     * Usually this is the public content interface, but
     * it could be the same as the implClass. 
     */
    public final JDefinedClass ref;
    
    private final ImplStructureStrategy strategy; 
    
    
    public MethodWriter createMethodWriter() {
        return strategy.createMethodWriter(this);
    }
    
    protected ClassContext( GeneratorContext _parent, ImplStructureStrategy _strategy, ClassItem _target ) {
        this.parent = _parent;
        this.target = _target;
        this.strategy = _strategy;
        this.ref = _target.getTypeAsDefined();
        this._package = parent.getPackageContext(ref._package());
        this.implClass = _strategy.createImplClass(_target);
        
        if( target.getUserSpecifiedImplClass()!=null ) {
            // create a place holder for a user-specified class.
            JDefinedClass usr;
            try {
                usr = parent.getCodeModel()._class(target.getUserSpecifiedImplClass());
                // but hide that file so that it won't be generated.
                usr.hide();
            } catch( JClassAlreadyExistsException e ) {
                // it's OK for this to collide.
                usr = e.getExistingClass();
            }
            usr._extends(implClass);
            this.implRef = usr;
        } else
            this.implRef = implClass;
    }
}
