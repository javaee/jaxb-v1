Directory Structure
===================
log/
	Contain test result reports from JUnit
schemas/
	Test data file for JavaScript-based general purpose test.
src/
	Source code that doesn't depend on any XJC-generated files.
test_src/
	Test data file (and its Java source files) for Java-based
	general purpose test.
work/
	test_src/ will be compiled into this directory. Use this
	as the source directory for your debugger.



TO ADD A NEW .SSUITE FILE
=========================

modify test/build.xml
modify test/schemas/build.xml



TESTING ANOTHER VERSION OF THE RI
=================================

The unit test and the performance test can be run against another
version of the RI. This allows you to perform the backward compatibility
test with the old version, or measure the performance of the old version.

To do this, you need to have two copies of the RI. One is "subject",
which conducts the test, and the other is "object", which is tested.

Build two copies of the RI, and run the following command from
the subject JAXB RI:

    $ cd <jaxb-ri-subject-home>/test
    $ ant performance-test -Dobject.root=<jaxb-ri-object-home>

If the object.root option is not specified, tests will be run against
the same VM.

For trouble-shooting, debugging, and etc, see build.xml.