package org.flite.cach3.aop;

import org.flite.cach3.annotations.*;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.security.*;

/**
Copyright (c) 2011 Flite, Inc

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 */
class AnnotationDataBuilder {

    static AnnotationData buildAnnotationData(final Annotation annotation,
                                              final Class expectedAnnotationClass,
                                              final String targetMethodName) {
        final AnnotationData data = new AnnotationData();

        if (annotation == null) {
            throw new InvalidParameterException(String.format(
                    "No annotation of type [%s] found.",
                    expectedAnnotationClass.getName()
            ));
        }

        final Class clazz = annotation.annotationType();
        if (!expectedAnnotationClass.equals(clazz)) {
            throw new InvalidParameterException(String.format(
                    "No annotation of type [%s] found, class was of type [%s].",
                    expectedAnnotationClass.getName(),
                    clazz.getName()
            ));
        }
        data.setClassName(clazz.getName());

        try {
            if (expectedAnnotationClass != ReadThroughAssignCache.class
                    && expectedAnnotationClass != InvalidateAssignCache.class
                    && expectedAnnotationClass != UpdateAssignCache.class) {
                final Method keyIndexMethod = clazz.getDeclaredMethod("keyIndex", null);
                final int keyIndex = (Integer) keyIndexMethod.invoke(annotation, null);
                if (keyIndex < -1) {
                    throw new InvalidParameterException(String.format(
                            "KeyIndex for annotation [%s] must be -1 or greater on [%s]",
                            expectedAnnotationClass.getName(),
                            targetMethodName
                    ));
                }
                data.setKeyIndex(keyIndex);

                final Method keyPrefixMethod = clazz.getDeclaredMethod("keyPrefix", null);
                final String keyPrefix = (String) keyPrefixMethod.invoke(annotation, null);
                if (!AnnotationConstants.DEFAULT_STRING.equals(keyPrefix)
                        && keyPrefix != null
                        && keyPrefix.length() > 0) {
                    data.setKeyPrefix(keyPrefix);
                }
            }

            if (expectedAnnotationClass == UpdateSingleCache.class
                    || expectedAnnotationClass == UpdateMultiCache.class
                    || expectedAnnotationClass == UpdateAssignCache.class) {
                final Method dataIndexMethod = clazz.getDeclaredMethod("dataIndex", null);
                final int dataIndex = (Integer) dataIndexMethod.invoke(annotation, null);
                if (dataIndex < -1) {
                    throw new InvalidParameterException(String.format(
                            "DataIndex for annotation [%s] must be -1 or greater on [%s]",
                            expectedAnnotationClass.getName(),
                            targetMethodName
                    ));
                }
                data.setDataIndex(dataIndex);
            }

            if (expectedAnnotationClass != InvalidateSingleCache.class
                    && expectedAnnotationClass != InvalidateMultiCache.class
                    && expectedAnnotationClass != InvalidateAssignCache.class) {
                final Method expirationMethod = clazz.getDeclaredMethod("expiration", null);
                final int expiration = (Integer) expirationMethod.invoke(annotation, null);
                if (expiration < 0) {
                    throw new InvalidParameterException(String.format(
                            "Expiration for annotation [%s] must be 0 or greater on [%s]",
                            expectedAnnotationClass.getName(),
                            targetMethodName
                    ));
                }
                data.setExpiration(expiration);
            }

            final Method namespaceMethod = clazz.getDeclaredMethod("namespace", null);
            final String namespace = (String) namespaceMethod.invoke(annotation, null);
            if (AnnotationConstants.DEFAULT_STRING.equals(namespace)
                    || namespace == null
                    || namespace.length() < 1) {
                throw new InvalidParameterException(String.format(
                        "Namespace for annotation [%s] must be defined on [%s]",
                        expectedAnnotationClass.getName(),
                        targetMethodName
                ));
            }
            data.setNamespace(namespace);

            if (expectedAnnotationClass == ReadThroughAssignCache.class
                    || expectedAnnotationClass == InvalidateAssignCache.class
                    || expectedAnnotationClass == UpdateAssignCache.class) {
                final Method assignKeyMethod = clazz.getDeclaredMethod("assignedKey", null);
                final String assignKey = (String) assignKeyMethod.invoke(annotation, null);
                if (AnnotationConstants.DEFAULT_STRING.equals(assignKey)
                        || assignKey == null
                        || assignKey.length() < 1) {
                    throw new InvalidParameterException(String.format(
                            "AssignedKey for annotation [%s] must be defined on [%s]",
                            expectedAnnotationClass.getName(),
                            targetMethodName
                    ));
                }
                data.setAssignedKey(assignKey);
            }

        } catch (NoSuchMethodException ex) {
            throw new RuntimeException("Problem assembling Annotation information.", ex);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException("Problem assembling Annotation information.", ex);
        } catch (InvocationTargetException ex) {
            throw new RuntimeException("Problem assembling Annotation information.", ex);
        }

        return data;
    }
}
