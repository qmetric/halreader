package com.qmetric.hal.reader

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.base.Optional
import com.google.common.reflect.TypeToken
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import spock.lang.Specification
import spock.lang.Unroll

class HalReaderTest extends Specification {

    static final TestObject testObject1 = new TestObject("abc", 1, true, [1, 2])

    static final TestObject testObject2 = new TestObject("def", 2, false, [3, 4])

    final halReader = new HalReader(new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES))

    def "should read links"()
    {
        when:
        final resource = halReader.read(reader("/fixtures/halWithLinks.json"))

        then:
        resource.getResourceLink().get().href == "https://localhost/self/1"
        resource.getLinkByRel("link").get().href == "https://localhost/link/1"
        resource.getLinkByRel("missing") == Optional.absent()
        resource.getLinksByRel("link").collect { it.href } == ["https://localhost/link/1"]
        resource.getLinksByRel("missing").collect { it.href } == []
        resource.getLinksByRel("links").collect { it.href } == ["https://localhost/links/1", "https://localhost/links/2"]
    }

    def "should read property values as strings"()
    {
        when:
        final resource = halReader.read(reader("/fixtures/halWithPrimitiveProperties.json"))

        then:
        resource.getValueAsString("textVal") == Optional.of("str")
        resource.getValueAsString("numVal") == Optional.of("100")
        resource.getValueAsString("boolVal") == Optional.of("true")
        resource.getValueAsString("nullVal") == Optional.absent()
        resource.getValueAsString("missing") == Optional.absent()
        resource.getValueAsString("emptyVal") == Optional.of("")
    }

    @Unroll def "should read property values as complex objects"()
    {
        when:
        final resource = halReader.read(reader("/fixtures/$resourcePath"))

        then:
        resource.getValueAsObject(propertyName, type) == expected

        where:
        resourcePath                       | propertyName            | type                                    | expected
        "halWithObjectProperty.json"       | "obj"                   | TypeToken.of(TestObject.class)          | Optional.of(testObject1)
        "halWithNestedObjectProperty.json" | "parent"                | TypeToken.of(TestParentObject.class)    | Optional.of(new TestParentObject(testObject1))
        "halWithObjectProperty.json"       | "missing"               | TypeToken.of(TestObject.class)          | Optional.absent()
        "halWithObjectProperty.json"       | "obj"                   | new TypeToken<Map<String, Object>>() {} | Optional.of([text: "abc", num: 1, bool: true, primitiveNumericArray: [1, 2]])
        "halWithEmptyArrayProperty.json"   | "array"                 | new TypeToken<List<String>>() {}        | Optional.of([])
        "halWithArrayProperties.json"      | "array"                 | new TypeToken<List<TestObject>>() {}    | Optional.of([testObject1, testObject2])
        "halWithArrayProperties.json"      | "primitiveArray"        | new TypeToken<List<String>>() {}        | Optional.of(["a", "b"])
        "halWithArrayProperties.json"      | "primitiveNumericArray" | new TypeToken<List<Integer>>() {}       | Optional.of([1, 2])
    }

    def "should read resource as object"()
    {
        when:
        final resource = halReader.read(reader("/fixtures/halWithProperties.json"))

        then:
        resource.getResourceAsObject(TypeToken.of(TestObject.class)) == testObject1
    }

    def "should read embedded resources"()
    {
        given:
        final resource = halReader.read(reader("/fixtures/halWithEmbeddedResource.json"))

        when:
        final embedded = resource.getResourcesByRel("embeddedResource").first()

        then:
        embedded.getResourceLink().get().href == "https://localhost/embedded/1"
        embedded.getValueAsObject("array", new TypeToken<List<TestObject>>() {}) == Optional.of([testObject1, testObject2])
    }

    def "should return nothing when embedded resources not found"()
    {
        when:
        final resource = halReader.read(reader("/fixtures/halWithEmbeddedResource.json"))

        then:
        resource.getResourcesByRel("missing").isEmpty()
    }

    private Reader reader(final String path)
    {
        this.getClass().getResource(path).newReader()
    }

    static class TestParentObject {
        final TestObject obj

        @SuppressWarnings("GroovyUnusedDeclaration") TestParentObject()
        {}

        TestParentObject(final TestObject obj)
        {
            this.obj = obj
        }

        @Override public int hashCode()
        {
            return HashCodeBuilder.reflectionHashCode(this);
        }

        @Override public boolean equals(final Object obj)
        {
            return EqualsBuilder.reflectionEquals(this, obj);
        }
    }

    static class TestObject {

        final String text

        final int num

        final boolean bool

        final List<Integer> primitiveNumericArray

        @SuppressWarnings("GroovyUnusedDeclaration") TestObject()
        {}

        TestObject(final String text, final Integer num, final Boolean bool, final List<Integer> primitiveNumericArray)
        {

            this.bool = bool
            this.num = num
            this.text = text
            this.primitiveNumericArray = primitiveNumericArray
        }

        @Override public int hashCode()
        {
            return HashCodeBuilder.reflectionHashCode(this);
        }

        @Override public boolean equals(final Object obj)
        {
            return EqualsBuilder.reflectionEquals(this, obj);
        }
    }
}
