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
import java.util.Iterator;
import java.util.List;


/**
 * Try statement with Catch and/or Finally clause
 */

public class JTryBlock implements JStatement {

    private JBlock body = new JBlock();
    private List catches = new ArrayList();
    private JBlock _finally = null;

    JTryBlock() { }

    public JBlock body() {
	return body;
    }

    public JCatchBlock _catch(JClass exception) {
	JCatchBlock cb = new JCatchBlock(exception);
	catches.add(cb);
	return cb;
    }

    public JBlock _finally() {
	if (_finally == null) _finally = new JBlock();
	return _finally;
    }

    public void state(JFormatter f) {
	f.p("try").g(body);
	for (Iterator i = catches.iterator(); i.hasNext();)
	    f.g((JCatchBlock)(i.next()));
	if (_finally != null)
	    f.p("finally").g(_finally);
	f.nl();
    }

}
