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
package com.sun.tools.xjc.grammar;

import java.util.List;
import java.util.ArrayList;

import org.xml.sax.Locator;

import com.sun.msv.grammar.OtherExp;

/**
 * the base class of all special OtherExps
 * that are used to annotate tahiti data-binding information
 * to AGM.
 * 
 * @author
 *    <a href="mailto:kohsuke.kawaguchi@sun.com">Kohsuke KAWAGUCHI</a>
 */
public abstract class JavaItem extends OtherExp {
    public JavaItem( String name, Locator loc ) {
        this.name = name;
        this.locator = loc;
    }
    
    public String name;
    
    public abstract Object visitJI( JavaItemVisitor visitor );

    /**
     * The source location information that points the position
     * where this field was defined.
     * 
     * This field can be null if the location information is unavailable
     * for some reason.
     */
    public final Locator locator;

    public final List declarations = new ArrayList();
}
