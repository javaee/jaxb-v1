o=unmarshal();
assert(validator.validate(o),"on-demand validation failed");
marshal(o);
