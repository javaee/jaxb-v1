/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.codemodel;

import java.io.PrintWriter;


/**
 * This is a utility class for managing indentation and other basic
 * formatting for PrintWriter.
 */

public class JFormatter {

    /**
     * Current number of indentation strings to print
     */
    private int indentLevel;

    /**
     * String to be used for each indentation.
     * Defaults to four spaces.
     */
    private String indentSpace;

    /**
     * Stream associated with this JFormatter
     */
    private PrintWriter pw;

    /**
     * Creates a JFormatter.
     *
     * @param s
     *        PrintWriter to JFormatter to use.
     *
     * @param space
     *        Incremental indentation string, similar to tab value.
     */
    public JFormatter(PrintWriter s, String space) {
	pw = s;
	indentSpace = space;
    }

    /**
     * Creates a formatter with default incremental indentations of
     * four spaces.
     */
    public JFormatter(PrintWriter s) {
	this(s, "    ");
    }

    /**
     * Closes this formatter.
     */
    public void close() {
        pw.close();
    }

    /**
     * Decrement the indentation level.
     */
    public JFormatter o() {
	indentLevel--;
	return this;
    }

    /**
     * Increment the indentation level.
     */
    public JFormatter i() {
    	indentLevel++;
	return this;
    }

    private boolean needSpace(char c1, char c2) {
	if ((c1 == ']') && (c2 == '{')) return true;
	if (c1 == ';') return true;
	if ((c1 == ')') && (c2 == '{')) return true;
	if ((c1 == ',') || (c1 == '=')) return true;
	if (c2 == '=') return true;
	if (Character.isDigit(c1)) {
	    if ((c2 == '(') || (c2 == ')') || (c2 == ';') || (c2 == ','))
		return false;
	    return true;
	}
	if (Character.isJavaIdentifierPart(c1)) {
	    switch (c2) {
	    case '{':
	    case '}':
	    case '+':
	    case '>':
		return true;
	    default:
		return Character.isJavaIdentifierStart(c2);
	    }
	}
	if (Character.isJavaIdentifierStart(c2)) {
	    switch (c1) {
	    case ']':
	    case ')':
	    case '}':
	    case '+':
		return true;
	    default:
		return false;
	    }
	}
	if (Character.isDigit(c2)) {
	    if (c1 == '(') return false;
	    return true;
	}
	return false;
    }

    private char lastChar = 0;
    private boolean atBeginningOfLine = true;

    private void spaceIfNeeded(char c) {
        if (atBeginningOfLine) {
            for (int i = 0; i < indentLevel; i++)
                pw.print(indentSpace);
            atBeginningOfLine = false;
        } else if ((lastChar != 0) && needSpace(lastChar, c))
            pw.print(' ');
    }

    /**
     * Print a char into the stream
     *
     * @param c the char
     */
    public JFormatter p(char c) {
        spaceIfNeeded(c);
        pw.print(c);
        lastChar = c;
        return this;
    }

    /**
     * Print a String into the stream
     *
     * @param s the String
     */
    public JFormatter p(String s) {
        spaceIfNeeded(s.charAt(0));
        pw.print(s);
        lastChar = s.charAt(s.length() - 1);
        return this;
    }

    /**
     * Print a new line into the stream
     */
    public JFormatter nl() {
        pw.println();
        lastChar = 0;
        atBeginningOfLine = true;
        return this;
    }

    /**
     * Cause the JGenerable object to generate source for iteself
     *
     * @param g the JGenerable object
     */
    public JFormatter g(JGenerable g) {
        g.generate(this);
        return this;
    }

    /**
     * Cause the JDeclaration to generate source for itself
     *
     * @param d the JDeclaration object
     */
    public JFormatter d(JDeclaration d) {
	d.declare(this);
	return this;
    }

    /**
     * Cause the JStatement to generate source for itself
     *
     * @param s the JStatement object
     */
    public JFormatter s(JStatement s) {
	s.state(this);
	return this;
    }

    /**
     * Cause the JVar to generate source for itself
     *
     * @param v the JVar object
     */
    public JFormatter b(JVar v) {
	v.bind(this);
	return this;
    }

}
