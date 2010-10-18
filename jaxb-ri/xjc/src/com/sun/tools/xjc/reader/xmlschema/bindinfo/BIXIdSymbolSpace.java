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

import javax.xml.namespace.QName;

import org.xml.sax.Locator;

import com.sun.tools.xjc.grammar.id.IDREFTransducer;
import com.sun.tools.xjc.grammar.id.SymbolSpace;
import com.sun.tools.xjc.grammar.xducer.Transducer;
import com.sun.tools.xjc.grammar.xducer.TransducerDecorator;
import com.sun.tools.xjc.reader.Const;

/**
 * ID symbol space customization.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class BIXIdSymbolSpace extends AbstractDeclarationImpl {
    
    /** Symbol space name. */
    private final String name;
    
    public BIXIdSymbolSpace(Locator _loc,String _name) {
        super(_loc);
        this.name = _name;
    }
    
    /**
     * Wraps the given trasnducer and apply the specified symbol space
     * semantics.
     */
    public Transducer makeTransducer(Transducer core) {
        markAsAcknowledged();   // this customizaion is used.
        final SymbolSpace ss = getBuilder().grammar.getSymbolSpace(name);
        
        if( core.isID() )
            return new TransducerDecorator(core) {
                public SymbolSpace getIDSymbolSpace() { return ss; }
            };
        else
            return new IDREFTransducer(getBuilder().grammar.codeModel,ss,true);
    }

    public QName getName() { return NAME; }
    
    /** Name of this declaration. */
    public static final QName NAME = new QName(
        Const.XJC_EXTENSION_URI, "idSymbolSpace" );
}
