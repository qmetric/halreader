halreader
=========

[![Build Status](https://travis-ci.org/qmetric/halreader.png)](https://travis-ci.org/qmetric/halreader)

HAL+JSON parser Java library for wrapping [HalBuilder](https://github.com/HalBuilder). The underlying [HalBuilder](https://github.com/HalBuilder) is an
excellent library and attempts to generify XML and JSON parsing and building of Hal resources through abstract interfaces.

This library wraps the reading facilities of HalBuilder to allow greater explicit control of certain JSON configuration features.

This library may become obsolete based on the solution to this HalBuilder [issue](https://github.com/HalBuilder/halbuilder-json/issues/4).

Features
--------

* Reading of HAL properties containing JSON objects or arrays
* [Jackson](https://github.com/FasterXML) configuration exposed for allowing control of JSON parsing


Usage
-----

```java
  final HalReader halReader = new HalReader(new ObjectMapper());
  final HalResource hal = halReader.read(reader);
```

See Javadoc for available HalResource methods.
