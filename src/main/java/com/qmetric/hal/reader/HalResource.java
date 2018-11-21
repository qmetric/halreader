package com.qmetric.hal.reader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.reflect.TypeToken;
import com.theoryinpractise.halbuilder.api.ContentRepresentation;
import com.theoryinpractise.halbuilder.api.Link;
import com.theoryinpractise.halbuilder.api.ReadableRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter class for com.theoryinpractise.halbuilder.api.ReadableRepresentation allowing for easier JSON parsing.
 */
public class HalResource
{
    private static final Logger LOGGER = LoggerFactory.getLogger(HalResource.class);

    private final ObjectMapper objectMapper;

    private final ReadableRepresentation representation;

    public HalResource(final ObjectMapper objectMapper, final ReadableRepresentation representation)
    {
        this.objectMapper = objectMapper;
        this.representation = representation;
    }

    /**
     * Get self link.
     *
     * @return Self link
     */
    public Optional<Link> getResourceLink()
    {
        return Optional.ofNullable(representation.getResourceLink());
    }

    /**
     * Get link.
     *
     * @param rel Relation name
     * @return Link
     */
    public Optional<Link> getLinkByRel(final String rel)
    {
        return Optional.ofNullable(representation.getLinkByRel(rel));
    }

    /**
     * Get links.
     *
     * @param rel Relation name
     * @return Links
     */
    public List<Link> getLinksByRel(final String rel)
    {
        return representation.getLinksByRel(rel);
    }

    /**
     * Get embedded resources by relation
     *
     * @param rel Relation name
     * @return Embedded resources
     */
    public List<HalResource> getResourcesByRel(final String rel)
    {
        final List<? extends ReadableRepresentation> resources = representation.getResourcesByRel(rel);

        return resources.stream()
                .map(representation -> new HalResource(objectMapper, representation))
                .collect(Collectors.toList());
    }

    /**
     * Get property value by name.
     *
     * @param name property name
     * @return value as string
     */
    public Optional<String> getValueAsString(final String name)
    {
        return Optional.ofNullable((String) representation.getValue(name, null));
    }

    /**
     * Get property value by parsing raw JSON as an object/ array.
     *
     * @param name property name
     * @param type Type
     * @return Object representation of raw JSON
     */
    public <T> Optional<T> getValueAsObject(final String name, final TypeToken<T> type)
    {
        try
        {
            final Optional<String> raw = getValueAsString(name);

            if (raw.isPresent())
            {
                //noinspection unchecked
                return Optional.ofNullable((T) objectMapper.readValue(raw.get(), objectMapper.constructType(type.getType())));
            }
            else
            {
                return Optional.empty();
            }
        }
        catch (IOException e)
        {
            LOGGER.warn("failed to get value '{}' as object {} - will return an absent value, reason: {}", name, type, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Parse root resource as an object.
     *
     * @param type Type
     * @return Object representation as an object
     */
    public <T> T getResourceAsObject(final TypeToken<T> type)
    {
        try
        {
            //noinspection unchecked
            return (T) objectMapper.readValue(((ContentRepresentation) getUnderlyingRepresentation()).getContent(), objectMapper.constructType(type.getType()));
        }
        catch (IOException e)
        {
            LOGGER.warn("failed to parse resource as object: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Get underlying HalBuilder (https://github.com/HalBuilder) com.theoryinpractise.halbuilder.api.ReadableRepresentation.
     *
     * @return Underlying representation
     */
    public ReadableRepresentation getUnderlyingRepresentation()
    {
        return representation;
    }
}
