/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
    
/**
 * 
 * @deprecated
 *      Left for the historical reasons. Use {@link QATestRunner} instead.
 */
public class TestCaseRunner
{
    public static void main( String[] args ) throws Exception {
        System.exit(new QATestRunner().run(args));
    }
}
