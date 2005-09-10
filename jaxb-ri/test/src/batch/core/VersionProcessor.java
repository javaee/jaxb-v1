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
package batch.core;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.dom4j.Document;

import com.sun.tools.xjc.reader.xmlschema.parser.VersionNumber;

/**
 * Processes @since and @excludeFrom attributes and
 * helps the implementation of {@link JAXBTest#isApplicable(VersionNumber)}. 
 * 
 * <p>
 * FIXME: better name.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public final class VersionProcessor {
    /**
     * This test is only applicable to the RI of this version or later.
     * can be null.
     */
    private final VersionNumber since;
    
    /**
     * This test is only applicable to the RI of this version or younger.
     * can be null.
     */
    private final VersionNumber until;
    
    /**
     * This test shall be excluded from the RI of versions listed here.
     */
    private final Set excludeVersions;
    
    /**
     * Special version number constant to represent ALL in
     * {@link #excludeVersions}.
     */
    private static final Object ALL_VERSION = new Object();

    /**
     * Creates a default {@link VersionProcessor} that accepts
     * any version.
     */
    public VersionProcessor() {
        since = null;
        until = null;
        excludeVersions = null;
    }
    
    public VersionProcessor( String sinceValue, String untilValue, String excludeFromValue ) {
        if( sinceValue!=null )
            since = new VersionNumber( sinceValue );
        else
            since = null;
        
        if( untilValue!=null )
            until = new VersionNumber( untilValue );
        else
            until = null;
        
        if( excludeFromValue!=null ) {
            excludeVersions = new HashSet();
            String v = excludeFromValue.trim();
            if(v.equals("all")) {
                excludeVersions.add(ALL_VERSION);
            } else {
                StringTokenizer tokens = new StringTokenizer( v );
                while(tokens.hasMoreTokens())
                    excludeVersions.add( new VersionNumber( tokens.nextToken() ) );
            }
        } else
            excludeVersions = null;
    }
    
    public VersionProcessor( Document testSpecMeta ) {
        this(
            testSpecMeta.getRootElement().attributeValue("since",null),
            testSpecMeta.getRootElement().attributeValue("until",null),
            testSpecMeta.getRootElement().attributeValue("excludeFrom",null) );
    }

    /**
     * Checks if the test is valid against the JAXB RI of
     * the specified version.
     */
    public boolean isApplicable(VersionNumber v) {
        if( excludeVersions!=null ) {
            if( excludeVersions.contains(ALL_VERSION)
            ||  excludeVersions.contains(v) )
                return false;
        }
        
        if(since!=null && since.isNewerThan(v))
            return false;
        
        if(until!=null && v.isNewerThan(until))
            return false;
        
        return true;
    }
}
