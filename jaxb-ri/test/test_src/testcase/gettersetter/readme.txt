These are the compiled these unit testcases (schema files) to test 
handling of binding info and other constraints and generation of getter/setter
methods:

* optional1.xsd
  -------------
  Schema file to test getter/setter generation when the element is declared 
  with 1,1 mutiplicity.

* optional2.xsd
  -------------
  Schema file to test handling of optioanl element (multiplicity 0,1).

* collection.xsd
  --------------
  Schema file to test collection type handling.

* choice.xsd
  ----------
  Schema file to test the handling of choice handling.

* nilllable1.xsd
  --------------
  Schema file to test the handling of nillable handling.

* nilllable2.xsd
  --------------
  Schema file to test the handling when an element is optioanl and nillable.

* restriction.xsd
  ---------------
  Schema file to test the handling of derive by restriction of simple type, 
  for range and pattern.

* all.xsd
  -------
  Schema file to test 'all' handling.

* unbox.xsd
  ---------
  Tests interaction between required/optional and unboxed types wrt
  the getter/setter generation.
  