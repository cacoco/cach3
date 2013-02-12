package org.flite.cach3.level2.aop;

import java.util.*;

public class AnnotationInfo {
    final Map<String, AnnotationDatum> data = new HashMap<String, AnnotationDatum>(10);

    public AnnotationInfo add(AnnotationDatum datum) {
        data.put(datum.getName(), datum);
        return this;
    }

    public AnnotationDatum get(final String name) {
        return data.get(name);
    }

    public <T> T getAsType(final String name, final T dflt) {
        final AnnotationDatum datum = data.get(name);
        return datum == null ? dflt : (T) datum.getValue();
    }
}
