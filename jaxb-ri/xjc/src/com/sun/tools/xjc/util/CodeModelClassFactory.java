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
package com.sun.tools.xjc.util;

import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;

import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JClassContainer;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMod;
import com.sun.tools.xjc.ErrorReceiver;

/**
 * Create new {@link JDefinedClass} and report class collision errors,
 * if necessary.
 * 
 * This is just a helper class that simplifies the class name collision
 * detection. This object maintains no state, so it is OK to use
 * multiple instances of this.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public final class CodeModelClassFactory {
    
    /** errors are reported to this object. */
    private ErrorReceiver errorReceiver;
    
    /** unique id generator. */
    private int ticketMaster = 0;
    
    
    public CodeModelClassFactory( ErrorReceiver _errorReceiver ) {
        this.errorReceiver = _errorReceiver;
    }
    
    public JDefinedClass createClass( JClassContainer parent, String name, Locator source ) {
        return createClass( parent, JMod.PUBLIC, name, source );
    }
    public JDefinedClass createClass( JClassContainer parent, int mod, String name, Locator source ) {
        return createClass(parent,mod,name,source,false);
    }
        
    public JDefinedClass createInterface( JClassContainer parent, String name, Locator source ) {
        return createInterface( parent, JMod.PUBLIC, name, source );
    }
    public JDefinedClass createInterface( JClassContainer parent, int mod, String name, Locator source ) {
        return createClass(parent,mod,name,source,true);
    }
    private JDefinedClass createClass(
        JClassContainer parent, int mod, String name, Locator source, boolean isInterface ) {
        
        try {
            JDefinedClass r = parent._class(mod,name,isInterface);
            // use the metadata field to store the source location,
            // so that we can report class name collision errors.
            r.metadata = source;
            
            return r;
        } catch( JClassAlreadyExistsException e ) {
            // class collision.
            JDefinedClass cls = e.getExistingClass();
            
            // report the error
            errorReceiver.error( new SAXParseException(
                Messages.format( Messages.ERR_CLASSNAME_COLLISION, cls.fullName() ),
                (Locator)cls.metadata ));
            errorReceiver.error( new SAXParseException(
                Messages.format( Messages.ERR_CLASSNAME_COLLISION_SOURCE, name ),
                source ));
            
            if( !name.equals(cls.name()) ) {
                // on Windows, FooBar and Foobar causes name collision
                errorReceiver.error( new SAXParseException(
                    Messages.format( Messages.ERR_CASE_SENSITIVITY_COLLISION,
                        name, cls.name() ), null ) );
            }
            
            // recovery from this error by returning a dummy class.
            // we won't generate the code, so the client will never see this class
            // getting generated.
            try {
                return parent.owner()._class("$$$garbage$$$"+(ticketMaster++));
            } catch( JClassAlreadyExistsException ee ) {
                return ee.getExistingClass();
            }
        }
    }
    
    public void setErrorHandler(ErrorReceiver errorReceiver) {
        this.errorReceiver = errorReceiver;
    }

}
