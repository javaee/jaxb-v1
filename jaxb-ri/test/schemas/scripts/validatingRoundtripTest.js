u = context.createUnmarshaller();
u.setValidating(true);
o = u.unmarshal(instance.document);
assert(validate(o),"on-demand validation failed");
marshal(o);
