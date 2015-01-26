package com.qmetric.hal.reader;

import com.theoryinpractise.halbuilder.DefaultRepresentationFactory;
import com.theoryinpractise.halbuilder.json.JsonRepresentationWriter;

public class OverriddenJsonRepresentationFactory extends DefaultRepresentationFactory
{
    public OverriddenJsonRepresentationFactory() {
        withRenderer(HAL_JSON, JsonRepresentationWriter.class);
        withReader(HAL_JSON, OverriddenJsonRepresentationReader.class);
    }
}