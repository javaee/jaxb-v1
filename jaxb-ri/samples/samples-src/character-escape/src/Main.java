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
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.XMLReader;

import simple.*;

public class Main {
    public static void main( String[] args ) throws Exception {
        
        // create JAXBContext for the primer.xsd
        ObjectFactory of = new ObjectFactory();
        
        // \u00F6 is o with diaeresis
        E e = of.createE("G\u00F6del & his friends");
        
        
        // set up a normal marshaller
        Marshaller marshaller = of.createMarshaller();
        marshaller.setProperty( "jaxb.encoding", "US-ASCII" );
        
        // check out the console output
        marshaller.marshal( e, System.out );
        
        
        // set up a marshaller with a custom character encoding handler
        marshaller = of.createMarshaller();
        marshaller.setProperty( "jaxb.encoding", "US-ASCII" );
        marshaller.setProperty(
          "com.sun.xml.bind.characterEscapeHandler",
          new CustomCharacterEscapeHandler() );
        
        // check out the console output
        marshaller.marshal( e, System.out );
    }

}
