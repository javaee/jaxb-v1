o = unmarshal();
assert(validate(o),"on-demand validation failed");
marshal(o);  // show it
eq = instance.compare(context,o); // test equivalence
assert(eq,"equivalence test failed");
