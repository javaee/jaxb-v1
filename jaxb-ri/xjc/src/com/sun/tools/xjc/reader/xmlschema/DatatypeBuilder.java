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

