/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2001-2011 Oracle and/or its affiliates. All rights reserved.
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

package javax.xml.bind;

/**
 * Encapsulate the location of a ValidationEvent.
 *
 * <p>
 * The <tt>ValidationEventLocator</tt> indicates where the <tt>ValidationEvent
 * </tt> occurred.  Different fields will be set depending on the type of 
 * validation that was being performed when the error or warning was detected.  
 * For example, on-demand validation would produce locators that contained 
 * references to objects in the Java content tree while unmarshal-time 
 * validation would produce locators containing information appropriate to the 
 * source of the XML data (file, url, Node, etc).
 *
 * @author <ul><li>Ryan Shoemaker, Sun Microsystems, Inc.</li><li>Kohsuke Kawaguchi, Sun Microsystems, Inc.</li><li>Joe Fialli, Sun Microsystems, Inc.</li></ul> 
 * @version $Revision: 1.3 $
 * @see Validator
 * @see ValidationEvent
 * @since JAXB1.0
 */
public interface ValidationEventLocator {

    /**
     * Return the name of the XML source as a URL if available
     *
     * @return the name of the XML source as a URL or null if unavailable
     */
    public java.net.URL getURL();
    
    /**
     * Return the byte offset if available
     *
     * @return the byte offset into the input source or -1 if unavailable
     */
    public int getOffset();
    
    /**
     * Return the line number if available
     *
     * @return the line number or -1 if unavailable 
     */
    public int getLineNumber();
    
    /**
     * Return the column number if available
     *
     * @return the column number or -1 if unavailable
     */
    public int getColumnNumber();
    
    /**
     * Return a reference to the object in the Java content tree if available
     *
     * @return a reference to the object in the Java content tree or null if
     *         unavailable
     */
    public java.lang.Object getObject();
    
    /**
     * Return a reference to the DOM Node if available
     *
     * @return a reference to the DOM Node or null if unavailable 
     */
    public org.w3c.dom.Node getNode();
    
}
