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
package com.sun.tools.xjc.reader.annotator;

import java.util.Vector;

import org.xml.sax.Locator;

import com.sun.msv.grammar.Expression;
import com.sun.msv.reader.GrammarReader;
import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.reader.NameConverter;
import com.sun.tools.xjc.reader.PackageTracker;

/**
 * {@link AnnotatorController} implemented by using {@link GrammarReader}.
 * 
 * @author
 *    <a href="mailto:kohsuke.kawaguchi@sun.com">Kohsuke KAWAGUCHI</a>
 */
public class AnnotatorControllerImpl implements AnnotatorController
{
    public AnnotatorControllerImpl(GrammarReader _reader, ErrorReceiver _errorReceiver, PackageTracker _tracker) {
        this.reader = _reader;
        this.tracker = _tracker;
        this.errorReceiver = _errorReceiver;
    }

    private final GrammarReader reader;
    private final PackageTracker tracker;
    private final ErrorReceiver errorReceiver;

    public NameConverter getNameConverter() {
        return NameConverter.smart;
    }
    public PackageTracker getPackageTracker() {
        return tracker;
    }

    public void reportError(Expression[] srcs, String msg) {

        Vector locs = new Vector();
        for (int i = 0; i < srcs.length; i++) {
            Locator loc = reader.getDeclaredLocationOf(srcs[i]);
            if (loc != null)
                locs.add(loc);
        }

        reader.controller.error((Locator[])locs.toArray(new Locator[0]), msg, null);
    }

    public void reportError(Locator[] srcs, String msg) {
        reader.controller.error(srcs, msg, null);
    }

    public ErrorReceiver getErrorReceiver() {
        return errorReceiver;
    }
}
