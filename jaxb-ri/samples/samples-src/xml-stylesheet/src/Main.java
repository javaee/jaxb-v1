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

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import primer.PurchaseOrder;

public class Main {
    public static void main( String[] args ) throws Exception {
        
        // create JAXBContext for the primer.xsd
        JAXBContext context = JAXBContext.newInstance("primer");
        
        // unmarshal a document, just to marshal it back again.
        PurchaseOrder po = (PurchaseOrder)context.createUnmarshaller().unmarshal(
            new File(args[0]));
        // we don't need to check the return value, because the unmarshal
        // method should haven thrown an exception if anything went wrong.
        
        
        // Here's the real meat.
        // we configure marshaller not to print out xml decl,
        // we then print out XML decl plus stylesheet header on our own,
        // then have the marshaller print the real meat.
        
        System.out.println("<?xml version='1.0'?>");
        System.out.println("<?xml-stylesheet type='text/xsl' href='foobar.xsl' ?>");
        // if you need to put DOCTYPE decl, it can be easily done here.
        
        // create JAXB marshaller.
        Marshaller marshaller = context.createMarshaller();
        // configure it
        marshaller.setProperty("com.sun.xml.bind.xmlDeclaration",Boolean.FALSE);
        // marshal
        marshaller.marshal(po,System.out);
    }

}
