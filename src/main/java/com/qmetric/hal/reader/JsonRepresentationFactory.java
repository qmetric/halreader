package com.qmetric.hal.reader;

import com.theoryinpractise.halbuilder.DefaultRepresentationFactory;
import com.theoryinpractise.halbuilder.json.JsonRepresentationWriter;

public class JsonRepresentationFactory extends DefaultRepresentationFactory
{
    public JsonRepresentationFactory()
    {
        withRenderer(HAL_JSON, JsonRepresentationWriter.class);
        withReader(HAL_JSON, JsonRepresentationReader.class);
    }
}
