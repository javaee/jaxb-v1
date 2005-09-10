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
package com.sun.tools.xjc.reader.xmlschema.bindinfo;


/**
 * This customization will enable serialization support on XJC.
 * This is used as a child of a {@link BIGlobalBinding} object,
 * and this doesn't implement BIDeclaration by itself.
 * 
 * @author
 *  Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class BIXSerializable {

    /** serial version UID. */
    private final long uid;
    
    public BIXSerializable( long _uid ) {
        uid = _uid;
    }
    
    /** Gets the serial version UID to be used. */
    public long getUID() { return uid; }
}
