u = context.createUnmarshaller();
u.setValidating(true);
o = u.unmarshal(instance.document);
assert(validate(o),"on-demand validation failure");
marshal(o);  // show it
eq = instance.compare(context,o); // test equivalence
assert(eq,"equivalence test failure");

// extra test of marshalling to DOM.
context.createMarshaller().marshal( o, new javax.xml.transform.dom.DOMResult() );
