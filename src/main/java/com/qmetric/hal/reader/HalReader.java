package com.qmetric.hal.reader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theoryinpractise.halbuilder.api.ContentRepresentation;
import com.theoryinpractise.halbuilder.api.RepresentationFactory;
import com.theoryinpractise.halbuilder.json.JsonRepresentationFactory;

import java.io.Reader;

/**
 * Hal reader used for reading HAL resources
 */
public class HalReader
{
    private final ObjectMapper objectMapper;

    private final RepresentationFactory representationFactory;

    /**
     * Construct new instance with default com.fasterxml.jackson.databind.ObjectMapper used for parsing HAL resource properties.
     */
    public HalReader()
    {
        this(new ObjectMapper());
    }

    /**
     * Construct new instance with given com.fasterxml.jackson.databind.ObjectMapper used for parsing HAL resource properties.
     * @param objectMapper Jackson object mapper
     */
    public HalReader(final ObjectMapper objectMapper)
    {
        this.objectMapper = objectMapper;
        this.representationFactory = new JsonRepresentationFactory();
    }

    /**
     * Read and return HalResource
     * @param reader Reader
     * @return Hal resource
     */
    public HalResource read(final Reader reader)
    {
        final ContentRepresentation readableRepresentation = representationFactory.readRepresentation(RepresentationFactory.HAL_JSON, reader);

        return new HalResource(objectMapper, readableRepresentation);
    }
}
