/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
import batch.qa.QATestBuilder;
    
/**
 * Runs the QA tests.
 */
public class QATestRunner extends BatchTestRunner
{
    public QATestRunner() {
        super( new QATestBuilder() );
    }
    
    protected void usage() {
        System.out.println("QA test runner");
        super.usage();
    }

    public static void main( String[] args ) throws Exception {
        System.exit(new QATestRunner().run(args));
    }
}
