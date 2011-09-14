Introduction
============

Distributed caching can be a big, hairy, intricate, and complex proposition when using it extensively.

Simple-Spring-Memcached (SSM) attempts to simplify implementation for several basic use cases.

This project enables caching in Spring-managed beans, by using Java 5 Annotations and Spring/AspectJ AOP on top of the spymemcached client. Using Simple-Spring-Memcached requires only a little bit of configuration and the addition of some specific annotations on the methods whose output or input is being cached.

Dependencies
============
The first thing you'll need, of course, is at least one running Memcached server. (Installation and usage instructions may be found on the memcached project page.)

Here is the dependency list from my pom.xml file:

    <dependency>
      <groupId>org.springframework</groupId>
      <artifactId>spring</artifactId>
      <version>2.5.4</version>
      <scope>provided</scope>
    </dependency>
    <dependency>
      <groupId>org.aspectj</groupId>
      <artifactId>aspectjrt</artifactId>
      <version>1.6.0</version>
      <scope>provided</scope>
    </dependency>
    <dependency>
      <groupId>org.aspectj</groupId>
      <artifactId>aspectjweaver</artifactId>
      <version>1.6.0</version>
      <scope>provided</scope>
    </dependency>
    <dependency>
      <groupId>log4j</groupId>
      <artifactId>log4j</artifactId>
      <version>1.2.11</version>
      <scope>provided</scope>
    </dependency>
    <dependency>
      <groupId>spy</groupId>
      <artifactId>memcached</artifactId>
      <version>2.3.1</version>
    </dependency>
In addition to the Simple-Spring-Memcached JAR file, the following libraries must be accessible:

Spring is an assumed dependency, and this project was built against the 2.5.4 version. A Spring library that is 2.5.4 compatible must included be on the classpath.
Two AspectJ libraries are also required, and this project was built against the 1.6.0 versions. These libraries may have version dependencies with your Spring version, so you are free to add whatever versions are necessary to the classpath.
This project uses standard Apache Commons Logging, and is built against Log4J, though you are welcome to provide any other implementation.
The spymemcached client (version 2.3.1) is required. You may get it yourself from that project's page, or alternately it is included in the jar-with-dependencies version of SSM that is provided from the download area.

Configuration
=============
You will need to inform your ApplicationContext of Simple-Spring-Memcached's configuration. This is done with an import configuration directive:

        <import resource="simplesm-context.xml" />
Simple-Spring-Memcached also requires an annotation to enable AOP access, which in turn requires the AOP namespace being defined:

<beans xmlns="http://www.springframework.org/schema/beans" 
    xmlns:aop="http://www.springframework.org/schema/aop"
    xsi:schemaLocation="http://www.springframework.org/schema/aop http://www.springframework.org/schema/aop/spring-aop-2.5.xsd">

...

        <aop:aspectj-autoproxy />
In order to tell Simple-Spring-Memcached about the specifics of your environment, you need to create a bean:

        <bean id="memcachedConnectionBean" class="net.nelz.simplesm.config.MemcachedConnectionBean">
                <property name="consistentHashing" value="true" />
                <property name="nodeList" value="127.0.0.1:11211 127.0.0.1:11311" />
        </bean>
The consistentHashing parameter refers to a method of choosing to which node a value is written, as implemented in the spymemcached client. The nodeList parameter is how you tell the Simple-Spring-Memcached library upon which IPs and ports your Memcached server(s) are listening.

Usage
=====
Any discussion of Memcached usage requires an understanding of keys and values. To get an understanding of this, see this helpful tutorial.

@CacheKeyMethod
---------------
In SSM, you are able to identify 'key objects'. These are the objects that Simple-Spring-Memcached will rely upon to generate a (non-namespaced) unique key for referring to values within the cache. For any given key object, it will first be checked if any of its methods are annotated with @CacheKeyMethod. If a @CacheKeyMethod is found, and it conforms to the required signature (no-arg, with an output of type String), this is the method that will be relied upon to generate a unique key. If there is no conforming @CacheKeyMethod, then the basic Object.toString() method will be used.

The Big 9 Annotations
The next 9 annotations are the real meat of this project. These are the annotations that mark where caching is to be applied. There are two dimensions that these annotations cover: cardinality (are we working with a single value with an assigned key, a single value with a calculated key, or a 'multiplexed' set of values with calculated keys); and usage pattern (read-through, update, or invalidate).

@ReadThroughSingleCache, @ReadThroughMultiCache, @ReadThroughAssignCache
------------------------------------------------------------------------
These are the most frequently used annotations. They follow the standard caching flow as follows:

Check if value exists in the cache. If it does, return it and exit cleanly.
If not found in the cache, then ask the underlying method for the data.
Before returning the data, write it out to the cache for future access.
Return the calculated data.
@InvalidateSingleCache, @InvalidateMultiCache, @InvalidateAssignCache
Whereas the ReadThrough*Cache annotations are involved with populating the cache when asked for information, the Invalidate*Cache annotations effectively take information out of the cache. These annotations are useful when you know that a value has been made stale and you want to force it to refresh on the next time it is referenced in a ReadThrough*Cache method.

@UpdateSingleCache, @UpdateMultiCache, @UpdateAssignCache
---------------------------------------------------------
The Update*Cache annotations can serve as a bit of performance improvement. Unlike the Invalidate*Cache annotations which remove a value from the cache to be looked up again in the future, the Update*Cache annotations force an update to the value in the cache which obviates the need to check the underlying method in a ReadThrough*Cache method.

Examples
========
@ReadThroughSingleCache
-----------------------
In this example, we are working with a fairly standard DAO-like pattern:

    @ReadThroughSingleCache(namespace = "CplxObj", keyIndex = 0, expiration = 3600)
    public ComplexObject getComplexObjectFromDB(Long complexObjectPk) {
        ...
        return result;
    }
The basic usage is that you've got a primary key (complexObjectPk) and you want to get the corresponding ComplexObject from the DB. This is kind of an expensive operation, and you'd like to do it less frequently because the data rarely changes. So, you slap on the Simple-Spring-Memcached annotation. You assign a (fairly arbitrary) namespace of "CplxObj?". (Namespacing this way helps ensure that you don't have cache collisions with another type of object that may have the same primary key.) You tell Simple-Spring-Memcached that they key object (complexObjectPk) is in index 0 in the parameter array. And lastly, you tell Simple-Spring-Memcached that you'd like for the value to stick around in the cache for at most 1 hour (3600 seconds).

@InvalidateMultiCache
---------------------
Here, we deal with another fairly common DAO-like pattern:

    @InvalidateMultiCache(namespace = "CplxObj", keyIndex = 1)
    public String saveModifiedComplexObjectsToDB(Credentials principal, List<ComplexObject> complexObjects) {
       ...
    }
This example show off the multiplexing capabilities of SSM. Let's say you made a common change to several of the ComplexObjects, and you want to write them out to the database. This means any previously cache-stored versions of these ComplexObjects will be stale (out-of-date). We use this @InvalidateMultiCache annotation to remove cache copies of that data, which will hopefully require future calls to an @ReadThrough*Cache method to hit the underlying method for a fresh copy of the data. You'll notice that the keyIndex is set to 1. This is because (in this made-up example) the ComplexObjects either have a method with an @CacheKeyMethod annotation that outputs the primary key, or an implementation of toString() that returns the primary key.

@UpdateAssignCache
------------------
This use-case is a bit more contrived than the above DAO-ish examples, but it gets the point across:

    @UpdateAssignCache(namespace = "CplxObj", assignedKey = "MyFav", dataIndex = -1, expiration = 7200)
    public List<Long> resetMyFavoriteComplexObjectPksFromDB() {
        ...
        return results;
    }
This @Update*Cache annotations are designed to force an update to the cache. This may be preferable in many cases rather than the invalidate-readthrough pattern, which ends up requiring a later trip back to the underlying method/db. This example also shows how the @*AssignCache annotations work. Instead of relying on input/output values for the key, use an annotation-declared key. The data that gets cached in this scenario is the output data. The list of primary keys is stored as a single value under the id "MyFav?" within the namespace of "CplxObj?".

Additional Information
======================
Exceptions
----------
Great care was taken while writing SSM to preserve the functionality of any surrounding code. If an exception is thrown anywhere in the SSM code, we've gone to great lengths to catch and log (jakarta-commons and/or log4j) as much information as possible from the exception, but we refrain from passing that exception up to the end user.

Externalizable
--------------
Any of the objects being saved to the cache must implement java.io.Serializable in the very least, but it is highly recommended that you implement java.io.Externalizable in as many classes of the cache data as is possible.




TODO List
=========
* Variable Page Sizes (LongList support for Multi's)
* Velocity (or other?) Templating for ID generation
