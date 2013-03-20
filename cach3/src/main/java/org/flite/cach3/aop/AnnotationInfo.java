package org.flite.cach3.aop;

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

    // * * * * * * * CONVENIENCE METHODS * * * * * * * //
    public String getAsString(final String name, final String dflt) {
        return this.<String>getAsType(name, dflt);
    }

    public String getAsString(final String name) {
        return getAsString(name, "");
    }

    public Integer getAsInteger(final String name, final Integer dflt) {
        return this.<Integer>getAsType(name, dflt);
    }

    public Integer getAsInteger(final String name) {
        return getAsInteger(name, Integer.MIN_VALUE);
    }

}
