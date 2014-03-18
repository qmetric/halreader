package com.qmetric.hal.reader

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.base.Optional
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import spock.lang.Specification
import spock.lang.Unroll

class HalReaderTest extends Specification {

    final halReader = new HalReader(new ObjectMapper())

    def "should get self link"()
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

    def "should parse property value as string"()
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

    @Unroll def "should parse property containing complex object"()
    {
        when:
        final resource = halReader.read(reader("/fixtures/$resourcePath"))

        then:
        resource.getValueAsObject(propertyName, type) == expected

        where:
        resourcePath                         | propertyName | type                                        | expected
        "halWithObjectProperty.json"         | "obj"        | new TypeReference<TestObject>() {}          | Optional.of(new TestObject("abc", 1, true))
        "halWithNestedObjectProperty.json"   | "parent"     | new TypeReference<TestParentObject>() {}    | Optional.of(new TestParentObject(new TestObject("abc", 1, true)))
        "halWithObjectProperty.json"         | "missing"    | new TypeReference<TestObject>() {}          | Optional.absent()
        "halWithObjectProperty.json"         | "obj"        | new TypeReference<Map<String, Object>>() {} | Optional.of([text: "abc", num: 1, bool: true])
        "halWithArrayProperty.json"          | "array"      | new TypeReference<List<String>>() {}        | Optional.of(["a", "b"])
        "halWithEmptyArrayProperty.json"     | "array"      | new TypeReference<List<String>>() {}        | Optional.of([])
        "halWithArrayOfObjectsProperty.json" | "array"      | new TypeReference<List<TestObject>>() {}    | Optional.of([new TestObject("abc", 1, true), new TestObject("def", 2, false)])
    }

    def "should parse embedded resources"()
    {
        given:
        final resource = halReader.read(reader("/fixtures/halWithEmbeddedResource.json"))

        when:
        final embedded = resource.getResourcesByRel("embeddedResource").first()

        then:
        embedded.getResourceLink().get().href == "https://localhost/embedded/1"
        embedded.getValueAsObject("array", new TypeReference<List<String>>() {}) == Optional.of(["a", "b"])
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

        @SuppressWarnings("GroovyUnusedDeclaration") TestObject()
        {}

        TestObject(final String text, final Integer num, final Boolean bool)
        {

            this.bool = bool
            this.num = num
            this.text = text
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
