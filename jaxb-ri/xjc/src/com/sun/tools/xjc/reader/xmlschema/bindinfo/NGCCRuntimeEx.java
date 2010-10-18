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

package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import java.util.StringTokenizer;

import javax.xml.namespace.QName;

import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.LocatorImpl;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JType;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.reader.TypeUtil;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.parser.NGCCRuntime;

/**
 * Extended NGCCRuntime for parsing XML Schema annotations.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke,kawaguchi@sun.com)
 */
public final class NGCCRuntimeEx extends NGCCRuntime {
    
    public final JCodeModel codeModel;
    public final ErrorHandler errorHandler;
    public final Options options;
    
    /**
     * BindInfo object to which currently parsed declarations
     * should go.
     */
    public BindInfo currentBindInfo;
    
    public NGCCRuntimeEx( JCodeModel _codeModel, Options opts, ErrorHandler _errorHandler ) {
        this.codeModel=_codeModel;
        this.options = opts;
        this.errorHandler = _errorHandler;
    }
    
    /**
     * Obtains a JType object for the given type.
     */
    public final  JType getType( String typeName ) throws SAXException {
        return TypeUtil.getType( codeModel, typeName, errorHandler, getLocator() );
    }
    
    public final  Locator copyLocator() {
        return new LocatorImpl(super.getLocator());
    }
    
    /**
     * Fixes up text found inside annotation so that it doesn't look
     * too terrible as javadoc. 
     */
    public final  String truncateDocComment( String s ) {
        StringBuffer buf = new StringBuffer(s.length());
        StringTokenizer tokens = new StringTokenizer(s,"\n");
        while(tokens.hasMoreTokens()) {
            buf.append( tokens.nextToken().trim() );
            if(tokens.hasMoreTokens())
                buf.append('\n');
        }
        return buf.toString();
    }
    
    /**
     * Escapes &lt; and &amp; in the input string.
     */
    public final  String escapeMarkup( String s ) {
        StringBuffer buf = new StringBuffer(s.length());
        for( int i=0; i<s.length(); i++ ) {
            char ch = s.charAt(i);
            switch(ch) {
            case '<':
                buf.append("&lt;");
                break;
            case '&':
                buf.append("&amp;");
                break;
            default:
                buf.append(ch);
                break;
            }
        }
        return buf.toString();
    }
    
    public final boolean parseBoolean( String str ) {
        str = str.trim();
        if( str.equals("true") || str.equals("1") )     return true;
        else                                            return false;
    }
    
    public final  QName parseQName( String str ) throws SAXException {
        int idx = str.indexOf(':');
        if(idx<0) {
            String uri = resolveNamespacePrefix("");
            // this is guaranteed to resolve
            return new QName(uri,str);
        } else {
            String prefix = str.substring(0,idx);
            String uri = resolveNamespacePrefix(prefix);
            if(uri==null) {
                // prefix failed to resolve.
                errorHandler.error( new SAXParseException(
                    Messages.format(ERR_UNDEFINED_PREFIX,prefix),getLocator()));
                uri="undefined"; // replace with a dummy
            }
            return new QName( uri, str.substring(idx+1) );
        }
    }
    
    /** Tells the user that a feature is not implemented yet. */
    public void reportUnimplementedFeature( String name ) throws SAXException {
        errorHandler.warning( new SAXParseException(
            Messages.format(ERR_UNIMPLEMENTED,name), getLocator() ));
    }    
    
    /** Tells the user that a feature is not supported. */
    public void reportUnsupportedFeature( String name ) throws SAXException {
        errorHandler.warning( new SAXParseException(
            Messages.format(ERR_UNSUPPORTED,name), getLocator() ));
    }    

//    protected void unexpectedXXX(String token) throws SAXException {
//        SAXParseException e = new SAXParseException(MessageFormat.format(
//            "Unexpected {0} appears at line {1} column {2}",
//            new Object[]{
//                token,
//                new Integer(getLocator().getLineNumber()),
//                new Integer(getLocator().getColumnNumber()) }),
//            getLocator());
//            
//        parser.errorHandler.fatalError(e);
//        throw e;    // we will abort anyway
//    }


    static final String ERR_UNIMPLEMENTED =
        "NGCCRuntimeEx.Unimplemented"; // arg:1
    static final String ERR_UNSUPPORTED =
        "NGCCRuntimeEx.Unsupported"; // arg:1
    static final String ERR_UNDEFINED_PREFIX =
        "NGCCRuntimeEx.UndefinedPrefix";  // arg:1
}

