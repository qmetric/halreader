halreader
=========

[![Build Status](https://travis-ci.org/qmetric/halreader.png)](https://travis-ci.org/qmetric/halreader)

[HAL+JSON](http://stateless.co/hal_specification.html) Java library wrapper for [HalBuilder](https://github.com/HalBuilder). The underlying [HalBuilder](https://github.com/HalBuilder) is an
excellent library and attempts to generify XML and JSON parsing/building of HAL resources through abstract interfaces.

This library wraps the reading facilities of HalBuilder to allow greater explicit control of certain JSON configuration features.

This library may become obsolete based upon the solution to this HalBuilder [issue](https://github.com/HalBuilder/halbuilder-json/issues/4).

Features
--------

* Reading of HAL properties containing JSON objects or arrays
* [Jackson](https://github.com/FasterXML) configuration exposed for allowing greater control of JSON deserialisation
* Delegates to other common HalBuilder read functionality

Usage
-----

```java
  final HalReader halReader = new HalReader(new ObjectMapper());
  final HalResource hal = halReader.read(ioReader);
```

See Javadoc for available com.qmetric.hal.reader.HalResource methods.

Library available from [Maven central repository](http://search.maven.org/)

```
<dependency>
    <groupId>com.qmetric</groupId>
    <artifactId>halreader</artifactId>
    <version>${VERSION}</version>
</dependency>
```
