/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader.xmlschema.cs;
import java.text.ParseException;

import javax.xml.bind.Element;

import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JJavaName;
import com.sun.codemodel.JPackage;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.TypeItem;
import com.sun.tools.xjc.reader.xmlschema.JClassFactory;
import com.sun.tools.xjc.reader.xmlschema.NameGenerator;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIClass;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BISchemaBinding;
import com.sun.xml.xsom.XSAnnotation;
import com.sun.xml.xsom.XSAttGroupDecl;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSNotation;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSWildcard;
/**
 * Default classBinder implementation. Honors &lt;jaxb:class> customizations
 * and default bindings.
 */
class DefaultClassBinder extends AbstractBinderImpl
{
    DefaultClassBinder(ClassSelector classSelector) {
        super(classSelector);
    }
    
    
    public Object attGroupDecl(XSAttGroupDecl decl) {
        return allow(decl,decl.getName());
    }

    public Object attributeDecl(XSAttributeDecl decl) {
        return allow(decl,decl.getName());
    }

    public Object modelGroup(XSModelGroup mgroup) {
        String defaultName;
        try {
            defaultName = NameGenerator.getName(this.owner.builder,mgroup);
        } catch( ParseException e ) {
            defaultName = null;
        }
        return allow(mgroup,defaultName);
    }

    public Object modelGroupDecl(XSModelGroupDecl decl) {
        return allow(decl,decl.getName());
    }

    public Object complexType(XSComplexType type) {
        ClassItem ci = allow(type,type.getName());
        if(ci!=null)    return ci;
        
        // all complex types get its own class
        if(type.isGlobal()) {
            // global ones use their own names
            
            JPackage pkg = owner.getPackage(type.getTargetNamespace());
            
            JDefinedClass clazz = owner.codeModelClassFactory.createInterface(
                pkg, deriveName(type), type.getLocator() );
            
            return wrapByClassItem( type, clazz );
        } else {
            // local ones use their parents' names.
            String className = builder.getNameConverter().toClassName(type.getScope().getName());
            
            BISchemaBinding sb = (BISchemaBinding)builder.getBindInfo(
                type.getOwnerSchema() ).get(BISchemaBinding.NAME);
            
            if(sb!=null)    className = sb.mangleAnonymousTypeClassName(className);
            else            className = className + "Type";
            
            return wrapByClassItem( type,
                getClassFactory(type).create( className, type.getLocator() ) );
        }
    }
    
    public Object elementDecl(XSElementDecl decl) {
        ClassItem r = allow(decl,decl.getName());
        
        if(r==null) {
            if(decl.isGlobal()) // global ones become classes
                r = wrapByClassItem( decl,
                    owner.codeModelClassFactory.createInterface(
                        owner.getPackage(decl.getTargetNamespace()),
                        deriveName(decl),
                        decl.getLocator() ));
        }
        
        if(r!=null) {
            // if an element declaration is mapped to a class,
            // implement the marker interface
            r.getTypeAsDefined()._implements(Element.class);
        }
        
        return r;
    }
    
    public Object empty( XSContentType ct ) { return null; }

    
    public Object attributeUse(XSAttributeUse use) {
        return never(use);
    }

    public Object simpleType(XSSimpleType type) {
        // UGLY CODE WARNING
        //builder.simpleTypeBuilder.referer = type;
        builder.simpleTypeBuilder.refererStack.push( type );

        builder.simpleTypeBuilder.build(type);

        builder.simpleTypeBuilder.refererStack.pop();

        return never(type);
    }

    public Object particle(XSParticle particle) {
        return never(particle);
    }

    public Object wildcard(XSWildcard wc) {
        return never(wc);
    }


    // these methods won't be used
    public Object annotation(XSAnnotation annon) {
        _assert(false);
        return null;
    }
    
    public Object notation(XSNotation not) {
        _assert(false);
        return null;
    }
    
    public Object facet(XSFacet decl) {
        _assert(false);
        return null;
    }
    public Object schema(XSSchema schema) {
        _assert(false);
        return null;
    }
    
    
    /**
     * Finds a {@link JClassFactory} that represents the package
     * + outer class in which a class generated from the given
     * component should be created.
     */
    private JClassFactory getClassFactory( XSComponent component ) {
        JClassFactory cf = owner.getClassFactory();
        
        if( component instanceof XSComplexType ) {
            XSComplexType xsct = (XSComplexType)component;
            if( xsct.isLocal() )  {
                TypeItem parent = owner.bindToType(xsct.getScope());
                if( parent instanceof ClassItem )
                    // if the parent element declaration is mapped to a class,
                    // promote this interface one step above.
                    return new JClassFactoryImpl( owner,
                        ((ClassItem)parent).getTypeAsDefined().parentContainer() );
            }
        }
        return cf;
    }


    
    
    
    /**
     * Makes sure that the component doesn't carry a {@link BIClass}
     * customization.
     * 
     * @return
     *      return value is unused. Since most of the caller needs to
     *      return null, to make the code a little bit shorter, this
     *      method always return null (so that the caller can always
     *      say <code>return never(sc);</code>.
     */
    private ClassItem never( XSComponent component ) {
        // all we need to do here is just not to acknowledge
        // any class customization. Then this class customization
        // will be reported as an error later when we check all
        // unacknowledged customizations.
        
        
//        BIDeclaration cust=builder.getBindInfo(component).get(BIClass.NAME);
//        if(cust!=null) {
//            // error
//            builder.errorReporter.error(
//                cust.getLocation(),
//                "test {0}", NameGetter.get(component) );
//        }
        return null;
    }
    
    /**
     * Checks if a component carries a customization to map it to a class.
     * If so, make it a class.
     * 
     * @param defaultBaseName
     *      The token which will be used as the basis of the class name
     *      if the class name is not specified in the customization.
     *      This is usually the name of an element declaration, and so on.
     *      
     *      This parameter can be null, in that case it would be an error
     *      if a name is not given by the customization.
     */
    private ClassItem allow( XSComponent component, String defaultBaseName ) {
        BIClass decl=(BIClass)builder.getBindInfo(component).get(BIClass.NAME);
        if(decl==null)  return null;
        
        decl.markAsAcknowledged();
        
        // determine the package to put this class in.
        
        String clsName = decl.getClassName();
        if(clsName==null) {
            // if the customiztion doesn't give us a name, derive one
            // from the current component.
            if( defaultBaseName==null ) {
                builder.errorReceiver.error( decl.getLocation(),
                    Messages.format(Messages.ERR_CLASS_NAME_IS_REQUIRED) );
                
                // recover by generating a pseudo-random name
                defaultBaseName = "undefined"+component.hashCode();
            }
            clsName = deriveName( defaultBaseName, component );
        } else {
            if( !JJavaName.isJavaIdentifier(clsName) ) {
                // not a valid Java class name
                builder.errorReceiver.error( decl.getLocation(),
                    Messages.format( Messages.ERR_INCORRECT_CLASS_NAME, clsName ));
                // recover by a dummy name
                clsName = "Undefined"+component.hashCode();
            }
        }
        
        JDefinedClass r = getClassFactory(component).create( clsName, decl.getLocation() );
        
        // set javadoc class comment.
        if( decl.getJavadoc()!=null )
            r.javadoc().appendComment(decl.getJavadoc()+"\n\n");
            // add extra blank lines so that the schema fragment
            // and user-specified javadoc would be separated
        
        ClassItem ci = wrapByClassItem( component, r );
        
        // if the implClass is given, set it to ClassItem
        String implClass = decl.getUserSpecifiedImplClass();
        if( implClass!=null )
            ci.setUserSpecifiedImplClass( implClass );
        
        return ci;
    }
};