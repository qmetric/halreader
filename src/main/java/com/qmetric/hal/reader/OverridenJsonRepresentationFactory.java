package com.qmetric.hal.reader;

import com.theoryinpractise.halbuilder.DefaultRepresentationFactory;
import com.theoryinpractise.halbuilder.json.JsonRepresentationWriter;

public class OverridenJsonRepresentationFactory extends DefaultRepresentationFactory
{
    public OverridenJsonRepresentationFactory() {
        withRenderer(HAL_JSON, JsonRepresentationWriter.class);
        withReader(HAL_JSON, JsonRepresentationReaderWithPrimitiveArraysBugFix.class);
    }
}