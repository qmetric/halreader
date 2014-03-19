package com.qmetric.hal.reader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.reflect.TypeToken;
import com.theoryinpractise.halbuilder.api.Link;
import com.theoryinpractise.halbuilder.api.ReadableRepresentation;

import java.io.IOException;
import java.util.List;

import static com.google.common.collect.FluentIterable.from;
import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * Adapter class for com.theoryinpractise.halbuilder.api.ReadableRepresentation allowing for easier JSON parsing.
 */
public class HalResource
{
    private final ObjectMapper objectMapper;

    private final ReadableRepresentation representation;

    public HalResource(final ObjectMapper objectMapper, final ReadableRepresentation representation)
    {
        this.objectMapper = objectMapper;
        this.representation = representation;
    }

    /**
     * Get self link.
     * @return Self link
     */
    public Optional<Link> getResourceLink()
    {
        return Optional.fromNullable(representation.getResourceLink());
    }

    /**
     * Get link.
     * @param rel Relation name
     * @return Link
     */
    public Optional<Link> getLinkByRel(final String rel)
    {
        return Optional.fromNullable(representation.getLinkByRel(rel));
    }

    /**
     * Get links.
     * @param rel Relation name
     * @return Links
     */
    public List<Link> getLinksByRel(final String rel)
    {
        return representation.getLinksByRel(rel);
    }

    /**
     * Get embedded resources by relation
     * @param rel Relation name
     * @return Embedded resources
     */
    public List<HalResource> getResourcesByRel(final String rel)
    {
        final List<? extends ReadableRepresentation> resources = representation.getResourcesByRel(rel);

        return from(resources).transform(new Function<ReadableRepresentation, HalResource>()
        {
            @Override public HalResource apply(final ReadableRepresentation representation)
            {
                return new HalResource(objectMapper, representation);
            }
        }).toList();
    }

    /**
     * Get property value by name.
     * @param name property name
     * @return value as string
     */
    public Optional<String> getValueAsString(final String name)
    {
        return Optional.fromNullable((String) representation.getValue(name, null));
    }

    /**
     * Get property value by parsing raw JSON as an object/ array.
     * @param name property name
     * @param type Type
     * @return Object representation of raw JSON
     */
    public <T> Optional<T> getValueAsObject(final String name, final TypeToken<T> type)
    {
        try
        {
            final String raw = getValueAsString(name).or(EMPTY);
            final T object = objectMapper.readValue(raw, objectMapper.constructType(type.getType()));
            return Optional.fromNullable(object);
        }
        catch (IOException e)
        {
            return Optional.absent();
        }
    }

    /**
     * Get underlying HalBuilder (https://github.com/HalBuilder) com.theoryinpractise.halbuilder.api.ReadableRepresentation.
     * @return Underlying representation
     */
    public ReadableRepresentation getUnderlyingRepresentation()
    {
        return representation;
    }
}
