/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader.decorator;

import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import com.sun.codemodel.JClass;
import com.sun.msv.datatype.xsd.BooleanType;
import com.sun.msv.datatype.xsd.StringType;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.OtherExp;
import com.sun.msv.reader.GrammarReader;
import com.sun.msv.reader.State;
import com.sun.msv.util.StartTagInfo;
import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.generator.field.ArrayFieldRenderer;
import com.sun.tools.xjc.generator.field.TypedListFieldRenderer;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.FieldItem;
import com.sun.tools.xjc.grammar.IgnoreItem;
import com.sun.tools.xjc.grammar.SuperClassItem;
import com.sun.tools.xjc.grammar.ext.DOMItemFactory;
import com.sun.tools.xjc.grammar.util.NameFinder;
import com.sun.tools.xjc.grammar.xducer.UserTransducer;
import com.sun.tools.xjc.reader.NameConverter;
import com.sun.tools.xjc.reader.PackageManager;
import com.sun.tools.xjc.reader.TypeUtil;
import com.sun.tools.xjc.util.CodeModelClassFactory;

/**
 * Uses Tahiti-style attribute-based customization specification
 * to decorate expressions.
 * 
 * <p>
 * This decorator recognizes attributes like "t:role".
 * 
 * @author  Kohsuke Kawaguchi
 */
public class RoleBasedDecorator extends DecoratorImpl
{
    private final CodeModelClassFactory classFactory;
    
    /**
     * This decorator is used when no customization specification
     * was found in the schema.
     * 
     * Typically, this decorator handles "default" decoration
     * (since it works when nothing is specified.)
     */
    private final Decorator defaultDecorator;
    
    private final PackageManager packageManager;
    
    
    
    public RoleBasedDecorator( GrammarReader _reader, ErrorReceiver _errorReceiver,
        AnnotatedGrammar _grammar, NameConverter _nc,
        PackageManager pkgMan, Decorator _defaultDecorator ) {
        
        super(_reader,_grammar,_nc);
        this.defaultDecorator = _defaultDecorator;
        this.packageManager = pkgMan;
        this.classFactory = new CodeModelClassFactory(_errorReceiver);
    }

    public Expression decorate(State state, Expression exp) {

        final StartTagInfo tag = state.getStartTag();

        // read the role attribute
        String role = getAttribute(tag, "role");

        if (role == null) {
            // there is no markup.
            // let the default decorator do the job.

            if (defaultDecorator != null)
                exp = defaultDecorator.decorate(state, exp);
            return exp;
        }
        
        role = role.intern();

        OtherExp roleExp;

        if (role=="none") {
            // do nothing. this role is used to override the default binding.
            return exp;
        } else
        if (role=="superClass") {
            roleExp = new SuperClassItem(null, state.getLocation());
        } else
        if (role=="class") {
            roleExp =
                grammar.createClassItem(
                    classFactory.createInterface(
                        packageManager.getCurrentPackage(),
                        decideName(state, exp, role, "", state.getLocation() ),
                        state.getLocation()),
                    null,
                    state.getLocation());
        } else
        if (role=="field") {
            // read the additional configuration.
            String collection = getAttribute(tag, "collection");
            String typeAtt = getAttribute(tag, "baseType");
            String delegation = getAttribute(tag,"delegate");

            JClass type = null;
            if (typeAtt != null)
                try {
                    type = codeModel.ref(typeAtt);
                } catch (ClassNotFoundException e) {
                    reportError(
                        Messages.format(Messages.ERR_CLASS_NOT_FOUND, typeAtt),
                        state.getLocation() );
                }

            FieldItem fi =
                new FieldItem(
                    decideName(state, exp, role, "", state.getLocation()),
                    null,
                    type,
                    reader.locator);
            roleExp = fi;
            
            if( delegation!=null && delegation.equals("true"))
                fi.setDelegation(true);

            if (collection != null) {
                if (collection.equals("array"))
                    fi.realization = ArrayFieldRenderer.theFactory;
                if (collection.equals("list"))
                    fi.realization = TypedListFieldRenderer.theFactory;
                if (fi.realization == null)
                    reportError(
                        Messages.format(Messages.ERR_INVALID_COLLECTION_TYPE, collection),
                        state.getLocation() );
            }
        } else
        if (role=="interface") {
            roleExp =
                grammar.createInterfaceItem(
                    classFactory.createInterface(
                        packageManager.getCurrentPackage(),
                        decideName(state, exp, role, "", state.getLocation() ),
                        state.getLocation()),
                    null,
                    state.getLocation());
        } else
        if( role=="primitive" ) {
            String name = getAttribute(tag, "name");
            String parseMethod = getAttribute(tag, "parseMethod");
            String printMethod = getAttribute(tag, "printMethod");
            boolean hasNsContext = BooleanType.load(getAttribute(tag, "hasNsContext", "false")).booleanValue();
            
            try {
                roleExp = grammar.createPrimitiveItem(
                    new UserTransducer(
                        TypeUtil.getType(codeModel,name,reader.controller,state.getLocation()),
                        parseMethod!=null?parseMethod:"new",
                        printMethod!=null?printMethod:"toString",
                        hasNsContext ),
                    StringType.theInstance,
                    null, state.getLocation() );
            } catch( SAXException e ) {
                // impossible
                roleExp = new OtherExp();
            } catch( IllegalArgumentException e ) {
                reportError( e.getMessage(), state.getLocation() );
                roleExp = new OtherExp();   // recover
            }
        } else
        if( role=="dom" ) {
            String type = getAttribute(tag, "type");
            if(type==null)      type="w3c";
            try {
                roleExp = DOMItemFactory.getInstance(type).create(
                    NameFinder.findElement(exp),
                    grammar, state.getLocation());
            } catch( DOMItemFactory.UndefinedNameException e ) {
                reportError( e.getMessage(), state.getLocation() );
                return exp;
            }
        }
        else
        if (role=="ignore") {
            roleExp = new IgnoreItem(state.getLocation());
        } else {
            reportError( Messages.format(Messages.ERR_UNDEFINED_ROLE, role), state.getLocation() );
            return exp;
        }

        // memorize where this expression is defined.
        reader.setDeclaredLocationOf(roleExp);

        // wrap the expression by the new expression
        roleExp.exp = exp;
        return roleExp;
    }
    
    
    private void reportError(String msg, Locator locator) {
        reader.controller.error(new Locator[]{locator}, msg, null );
    }
 }
