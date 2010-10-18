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

package com.sun.tools.xjc.reader.xmlschema;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.relaxng.datatype.DatatypeException;

import com.sun.msv.datatype.xsd.DatatypeFactory;
import com.sun.msv.datatype.xsd.ErrorType;
import com.sun.msv.datatype.xsd.TypeIncubator;
import com.sun.msv.datatype.xsd.XSDatatype;
import com.sun.tools.xjc.reader.*;
import com.sun.xml.bind.JAXBAssertionError;
import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSListSimpleType;
import com.sun.xml.xsom.XSRestrictionSimpleType;
import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSUnionSimpleType;
import com.sun.xml.xsom.visitor.XSSimpleTypeFunction;

/**
 * Builds {@link XSDatatype} from {@link XSSimpleType}.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class DatatypeBuilder implements XSSimpleTypeFunction {
    
    private final BGMBuilder builder;
    
    DatatypeBuilder( BGMBuilder builder, XSSchemaSet schemas ) {
        this.builder = builder;
        
        // initialize the cache by all the built-in types.
        try {
            for( int i=0; i<Const.builtinTypeNames.length; i++ ) {
                XSSimpleType type = schemas.getSimpleType(
                    Const.XMLSchemaNSURI,Const.builtinTypeNames[i]);
                _assert(type!=null);
                
                cache.put( type, DatatypeFactory.getTypeByName(Const.builtinTypeNames[i]) );
            }
        } catch ( DatatypeException e ) {
            e.printStackTrace();
            _assert(false);
        }
    }
    
    /**
     * Stores all the generated XSDatatype for future reuse.
     * A map from {@link XSSimpleType} to {@link XSDatatype}.
     */
    private final Map cache = new HashMap();
    
    /**
     * Just a type-safe wrapper method.
     */
    public XSDatatype build( XSSimpleType type ) {
        return (XSDatatype)type.apply(this);
    }

    public Object restrictionSimpleType(XSRestrictionSimpleType type) {
        XSDatatype dt = (XSDatatype)cache.get(type);
        if(dt!=null)    return dt;
        
        try {
            
            
            TypeIncubator ti = new TypeIncubator(
                build(type.getSimpleBaseType()));
            Iterator itr = type.iterateDeclaredFacets();
            while(itr.hasNext()) {
                XSFacet facet = (XSFacet)itr.next();
                ti.addFacet( facet.getName(), facet.getValue(), facet.isFixed(), facet.getContext() );
            }
            dt = ti.derive(type.getTargetNamespace(),type.getName());
            
            
            cache.put(type,dt);
            return dt;
        } catch( DatatypeException e ) {
            builder.errorReporter.error(type.getLocator(),
                Messages.ERR_DATATYPE_ERROR,e.getMessage());
            return ErrorType.theInstance;
        }
    }

    public Object listSimpleType(XSListSimpleType type) {
        XSDatatype dt = (XSDatatype)cache.get(type);
        if(dt!=null)    return dt;
        
        try {
            
            
            dt = DatatypeFactory.deriveByList(
                type.getTargetNamespace(),
                type.getName(),
                build(type.getItemType()));
                
                
            cache.put(type,dt);
            return dt;
        } catch( DatatypeException e ) {
            builder.errorReporter.error(type.getLocator(),
                Messages.ERR_DATATYPE_ERROR,e.getMessage());
            return ErrorType.theInstance;
        }
    }

    public Object unionSimpleType(XSUnionSimpleType type) {
        XSDatatype dt = (XSDatatype)cache.get(type);
        if(dt!=null)    return dt;
        
        try {
            
            XSDatatype[] members = new XSDatatype[type.getMemberSize()];
            for( int i=0; i<members.length; i++ )
                members[i] = build(type.getMember(i));
            
            dt = DatatypeFactory.deriveByUnion(
                    type.getTargetNamespace(),
                    type.getName(),members);
                
            cache.put(type,dt);
            return dt;
        } catch( DatatypeException e ) {
            builder.errorReporter.error(type.getLocator(),
                Messages.ERR_DATATYPE_ERROR,e.getMessage());
            return ErrorType.theInstance;
        }
    }
    
    
    
    private final static void _assert( boolean b ) {
        if(!b)
            throw new JAXBAssertionError();
    }
    
    
}

