/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.xml.sax.Locator;

import com.sun.codemodel.JCodeModel;
import com.sun.tools.xjc.generator.field.DefaultFieldRendererFactory;
import com.sun.tools.xjc.generator.field.FieldRendererFactory;
import com.sun.tools.xjc.reader.Const;
import com.sun.tools.xjc.reader.NameConverter;
import com.sun.xml.xsom.XSDeclaration;
import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.XSSimpleType;

/**
 * Global binding customization. The code is highly temporary.
 * 
 * <p>
 * One of the information contained in a global customization
 * is the default binding for properties. This object contains a
 * BIProperty object to keep this information.
 * 
 * @author
 *  Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public final class BIGlobalBinding extends AbstractDeclarationImpl {
    

    private final NameConverter nameConverter;
    private final boolean enableJavaNamingConvention;
    private final boolean modelGroupBinding;
    private final BIProperty property;
    private final boolean generateEnumMemberName;
    private final boolean choiceContentPropertyWithModelGroupBinding;
    /**
     * Set of datatype names. For a type-safe enum class
     * to be generated, the underlying XML datatype must be derived from
     * one of the types in this set.
     * 
     * <p>
     * This set contains type names as StringPairs.
     */
    private final Set enumBaseTypes;
    private final BIXSerializable serializable;
    private final BIXSuperClass superClass;
    
    /**
     * True if the default binding of the wildcard should use DOM.
     * This feature is not publicly available.
     */
    public final boolean smartWildcardDefaultBinding;
    
    private final boolean enableTypeSubstitutionSupport;
    
    private static Set createSet() {
        Set s = new HashSet();
        s.add(new QName(Const.XMLSchemaNSURI,"NCName"));
        return s;
    }
    
    /**
     * Creates a bind info object with the default values
     */
    public BIGlobalBinding( JCodeModel codeModel ) {
        this(
            codeModel, new HashMap(), NameConverter.standard,
            false, false, true, false, false, false,
            createSet(),
            null, null, null, false, false, null );
    }
    
    public BIGlobalBinding(
        JCodeModel codeModel,
        Map _globalConvs,
        NameConverter nconv,
        boolean _modelGroupBinding,
        boolean _choiceContentPropertyWithModelGroupBinding,
        boolean _enableJavaNamingConvention,
        boolean _fixedAttrToConstantProperty,
        boolean _needIsSetMethod,
        boolean _generateEnumMemberName,
        Set _enumBaseTypes,
        FieldRendererFactory collectionFieldRenderer,   // default collection type. can be null.
        BIXSerializable _serializable,
        BIXSuperClass _superClass,
        boolean _enableTypeSubstitutionSupport,
        boolean _smartWildcardDefaultBinding,
        Locator _loc ) {
        
        super(_loc);
        
        this.globalConversions = _globalConvs;
        this.nameConverter = nconv;
        this.modelGroupBinding = _modelGroupBinding;
        this.choiceContentPropertyWithModelGroupBinding = _choiceContentPropertyWithModelGroupBinding;
        this.enableJavaNamingConvention = _enableJavaNamingConvention;
        this.generateEnumMemberName = _generateEnumMemberName;
        this.enumBaseTypes = _enumBaseTypes;
        this.serializable = _serializable;
        this.superClass = _superClass;
        this.enableTypeSubstitutionSupport = _enableTypeSubstitutionSupport;
        this.smartWildcardDefaultBinding = _smartWildcardDefaultBinding;
        
        this.property = new BIProperty(_loc,null,null,null,null,
            (collectionFieldRenderer==null)
                ?new DefaultFieldRendererFactory(codeModel)
                :new DefaultFieldRendererFactory(collectionFieldRenderer),
            _fixedAttrToConstantProperty  ?Boolean.TRUE:Boolean.FALSE,
            _needIsSetMethod  ?Boolean.TRUE:Boolean.FALSE );
    }
    
    
    
    /**
     * Gets the name converter that will govern the XML->Java
     * name conversion process for this compilation.
     * 
     * <p>
     * The "underscoreBinding" customization will determine
     * the exact object returned from this method. The rest of XJC
     * should just use the NameConverter interface.
     * 
     * @return
     *      Always return non-null valid object even in the absence
     *      of the customization.
     */
    public NameConverter getNameConverter() { return nameConverter; }
    
    
    
    /**
     * Returns true if the "enableJavaNamingConvention" option is turned on.
     * 
     * In this mode, the compiler is expected to apply XML-to-Java name
     * conversion algorithm even to names given by customizations.
     * 
     * This method is intended to be called by other BIXXX classes.
     * The effect of this switch should be hidden inside this package.
     * IOW, the reader.xmlschema package shouldn't be aware of this switch.
     */
    boolean isJavaNamingConventionEnabled() { return enableJavaNamingConvention; }
    
    /**
     * Returns true if the "modelGroupBinding" option is turned on.
     */
    public boolean isModelGroupBinding() { return modelGroupBinding; }
    
    /**
     * Returns true if the "choiceContentProperty" option is turned on.
     * This option takes effect only in the model group binding mode.
     */
    public boolean isChoiceContentPropertyModelGroupBinding() {
        return choiceContentPropertyWithModelGroupBinding;
    }
    
    /**
     * Returns true if our experimental type substitution support
     * is enabled.
     * 
     * Since the customization is defined in our vendor extension,
     * obviously it cannot be turned on in the strict mode.
     */
    public boolean isTypeSubstitutionSupportEnabled() {
        return enableTypeSubstitutionSupport;
    }
    
    /**
     * Gets the default property customization.
     */
    public BIProperty getDefaultProperty() {
        return property;
    }

    public void setParent(BindInfo parent) {
        super.setParent(parent);
        property.setParent(parent); // don't forget to initialize the property
        
    }
    
    /**
     * Moves global BIConversion to the right object.
     */
    public void dispatchGlobalConversions( XSSchemaSet schema ) {
        // also set parent to the global conversions
        for (Iterator itr = globalConversions.entrySet().iterator(); itr.hasNext();) {
            Map.Entry e = (Map.Entry) itr.next();
            
            QName name = (QName)e.getKey();
            BIConversion conv = (BIConversion)e.getValue();
            
            XSSimpleType st = schema.getSimpleType(name.getNamespaceURI(),name.getLocalPart());
            if(st==null) {
                getBuilder().errorReceiver.error(
                    getLocation(),
                    Messages.format(Messages.ERR_UNDEFINED_SIMPLE_TYPE,name)
                );
                continue; // abort
            }
            
            getBuilder().getOrCreateBindInfo(
                st)
                .addDecl(conv);
        }
    }
    
    
    /**
     * Checks if the given XML Schema built-in type can be mapped to
     * a type-safe enum class.
     * 
     * @param typeName
     */
    public boolean canBeMappedToTypeSafeEnum( QName typeName ) {
        return enumBaseTypes.contains(typeName);
    }

    public boolean canBeMappedToTypeSafeEnum( String nsUri, String localName ) {
        return canBeMappedToTypeSafeEnum(new QName(nsUri,localName));
    }

    public boolean canBeMappedToTypeSafeEnum( XSDeclaration decl ) {
        return canBeMappedToTypeSafeEnum( decl.getTargetNamespace(), decl.getName() );
    }
    
    /**
     * Returns true if the compiler needs to generate type-safe enum
     * member names when enumeration values cannot be used as constant names.
     */
    public boolean needsToGenerateEnumMemberName() {
        return generateEnumMemberName;
    }

    /**
     * Returns {@link BIXSerializable} if the extension is specified,
     * or null otherwise.
     */
    public BIXSerializable getSerializableExtension() {
        return serializable;
    }

    /**
     * Returns {@link BIXSuperClass} if the extension is specified,
     * or null otherwise.
     */
    public BIXSuperClass getSuperClassExtension() {
        return superClass;
    }

    
    
    /**
     * Globally-defined conversion customizations.
     * Map from QName to BIConversion.
     */
    private final Map globalConversions;
    
//    private BIConversion getGlobalConversion( QName name ) {
//        return (BIConversion)globalConversions.get(name);
//    }
//    public BIConversion getGlobalConversion( XSSimpleType name ) {
//        // global customization will never contain conversions for anonymous types
//        if( name.isLocal() )    return null;
//        
//        return getGlobalConversion(new QName(name.getTargetNamespace(),name.getName()));
//    }
    
    public QName getName() { return NAME; }
    public static final QName NAME = new QName(
        Const.JAXB_NSURI, "globalBinding" );
}