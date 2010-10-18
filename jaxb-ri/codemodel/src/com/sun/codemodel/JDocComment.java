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

package com.sun.codemodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * JavaDoc comment.
 * 
 * TODO: it would be nice if we have JComment class and we can derive
 * this class from there.
 */
public class JDocComment implements JGenerable {
    /** contents of the comment, without the prefixes and suffixes. */
    private String comment="";

    /** list of @param tags */
    private final Map atParams = new HashMap();
    
    /** list of @throws tags */
    private final Map atThrows = new HashMap();
    
    /** The @return tag */
    private String atReturn = null;
    
    /** The @deprecated tag */
    private String atDeprecated = null;

    /** list of xdoclets */
    private final List xdoclets = new ArrayList();
    
    /** Gets the body of the comment. */
    public String getComment() {
        return comment;
    }

    /** Sets the body of the comment. */
    public JDocComment setComment(String comment) {
        this.comment = comment;
        return this;
    }
    
    /** Appends text to the body of the comment. */
    public JDocComment appendComment( String comment ) {
        this.comment += comment;
        return this;
    }
    
    /**
     * add an @param tag to the javadoc
     */
    public JDocComment addParam( String param, String comment ) {
        String s = (String)atParams.get(param);
        if( s!=null )   comment = s+comment;
        
        atParams.put( param, comment );
        return this;
    }
    
    /**
     * add an @param tag to the javadoc
     */
    public JDocComment addParam( JVar param, String comment ) {
        return addParam( param.name, comment );
    }
    
    /**
     * add an @throws tag to the javadoc
     */
    public JDocComment addThrows( String exception, String comment ) {
        String s = (String)atThrows.get(exception);
        if( s!=null )   comment = s+comment;
        
        atThrows.put( exception, comment );
        return this;
    }
    
    /**
     * add an @throws tag to the javadoc
     */
    public JDocComment addThrows( Class exception, String comment ) {
        return addThrows( exception.getName(), comment );
    }
    
    /**
     * add an @throws tag to the javadoc
     */
    public JDocComment addThrows( JClass exception, String comment ) {
        return addThrows( exception.fullName(), comment );
    }
    
    /**
     * add an @return tag to the javadoc
     */
    public JDocComment addReturn( String comment ) {
        if(atReturn==null)      atReturn = comment;
        else                    atReturn += comment;
        return this;
    }
    

    /**
     * add an @deprecated tag to the javadoc, with the associated message.
     */
    public void setDeprecated( String comment ) {
        atDeprecated = comment;
    }

    /**
     * add an xdoclet tag to the javadoc
     */
    public JXDoclet addXDoclet( String name ) {
        final JXDoclet xdoclet = new JXDoclet(name);
        xdoclets.add(xdoclet);
        return xdoclet;
    }

    public void generate(JFormatter f) {
        // I realized that we can't use StringTokenizer because
        // this will recognize multiple \n as one token.
//        StringTokenizer tokens = new StringTokenizer(comment,"\n");
        
        f.p("/**").nl();
        
//        while(tokens.hasMoreTokens()) {
//            String nextLine = tokens.nextToken();
//            f.p(" * "+nextLine).nl();
//        }
        format(f,comment);
        
        f.p(" * ").nl();
        for( Iterator i = atParams.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry e = (Map.Entry)i.next();
            format( f, "@param "+e.getKey(), (String)e.getValue() );
        }
        if( atReturn != null )
            format( f, "@return", atReturn );
        for( Iterator i = atThrows.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry e = (Map.Entry)i.next();
            format( f, "@throws "+e.getKey(), (String)e.getValue() );
        }
        if( atDeprecated != null )
            format( f, "@deprecated", atDeprecated );
        for( final Iterator i = xdoclets.iterator(); i.hasNext(); ) {
            final JXDoclet x = (JXDoclet)i.next();
            x.generate(f);
        }
        f.p(" */").nl();
    }
    
    private void format( JFormatter f, String key, String s ) {
        int idx;
        f.p(" * "+key).nl();
        while( (idx=s.indexOf('\n'))!=-1 ) {
            f.p(" *     "+ s.substring(0,idx)).nl();
            s = s.substring(idx+1);
        }
        if(s.length()!=0)
            f.p(" *     "+s).nl();
    }
    
    private void format( JFormatter f, String s ) {
        int idx;
        while( (idx=s.indexOf('\n'))!=-1 ) {
            f.p(" * "+ s.substring(0,idx)).nl();
            s = s.substring(idx+1);
        }
        if(s.length()!=0)
            f.p(" * "+s).nl();
    }
}
