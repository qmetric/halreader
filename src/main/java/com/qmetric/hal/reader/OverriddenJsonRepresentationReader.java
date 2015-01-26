package com.qmetric.hal.reader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.CharStreams;
import com.theoryinpractise.halbuilder.AbstractRepresentationFactory;
import com.theoryinpractise.halbuilder.api.ContentRepresentation;
import com.theoryinpractise.halbuilder.api.RepresentationException;
import com.theoryinpractise.halbuilder.api.RepresentationReader;
import com.theoryinpractise.halbuilder.impl.api.Support;
import com.theoryinpractise.halbuilder.impl.representations.ContentBasedRepresentation;
import com.theoryinpractise.halbuilder.impl.representations.MutableRepresentation;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;
import java.util.Map;

import static com.theoryinpractise.halbuilder.impl.api.Support.CURIES;
import static com.theoryinpractise.halbuilder.impl.api.Support.EMBEDDED;
import static com.theoryinpractise.halbuilder.impl.api.Support.HREF;
import static com.theoryinpractise.halbuilder.impl.api.Support.HREFLANG;
import static com.theoryinpractise.halbuilder.impl.api.Support.LINKS;
import static com.theoryinpractise.halbuilder.impl.api.Support.NAME;
import static com.theoryinpractise.halbuilder.impl.api.Support.PROFILE;
import static com.theoryinpractise.halbuilder.impl.api.Support.TITLE;

/**
 * Modified version of com.theoryinpractise.halbuilder.json.JsonRepresentationReader from https://github.com/HalBuilder/halbuilder-json (version 4.0.2).
 * Only difference is that this class returns a raw JSON string representation for properties containing array/ object type values.
 */
public class OverriddenJsonRepresentationReader implements RepresentationReader {
    private final ObjectMapper mapper;

    private final AbstractRepresentationFactory representationFactory;

    public OverriddenJsonRepresentationReader(AbstractRepresentationFactory representationFactory) {
        this.representationFactory = representationFactory;
        this.mapper = new ObjectMapper();
    }

    public ContentRepresentation read(Reader reader) {
        try {
            String source = CharStreams.toString(reader);

            JsonNode rootNode = mapper.readValue(new StringReader(source), JsonNode.class);

            return readResource(rootNode);
        } catch (Exception e) {
            throw new RepresentationException(e);
        }

    }

    private ContentRepresentation readResource(JsonNode rootNode) throws IOException {

        ContentBasedRepresentation resource = new ContentBasedRepresentation(representationFactory, rootNode.toString());

        readNamespaces(resource, rootNode);
        readLinks(resource, rootNode);
        readProperties(resource, rootNode);
        readResources(resource, rootNode);
        return resource;
    }

    private void readNamespaces(MutableRepresentation resource, JsonNode rootNode) {
        if (rootNode.has(LINKS)) {
            JsonNode linksNode = rootNode.get(LINKS);
            if (linksNode.has(CURIES)) {
                JsonNode curieNode = linksNode.get(CURIES);

                if (curieNode.isArray()) {
                    Iterator<JsonNode> values = curieNode.elements();
                    while (values.hasNext()) {
                        JsonNode valueNode = values.next();
                        resource.withNamespace(valueNode.get(NAME).asText(), valueNode.get(HREF).asText());
                    }
                } else {
                    resource.withNamespace(curieNode.get(NAME).asText(), curieNode.get(HREF).asText());
                }
            }
        }
    }

    private void readLinks(MutableRepresentation resource, JsonNode rootNode) {
        if (rootNode.has(LINKS)) {
            Iterator<Map.Entry<String, JsonNode>> fields = rootNode.get(LINKS).fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> keyNode = fields.next();
                if (!CURIES.equals((keyNode.getKey()))) {
                    if (keyNode.getValue().isArray()) {
                        Iterator<JsonNode> values = keyNode.getValue().elements();
                        while (values.hasNext()) {
                            JsonNode valueNode = values.next();
                            withJsonLink(resource, keyNode, valueNode);
                        }
                    } else {
                        withJsonLink(resource, keyNode, keyNode.getValue());
                    }
                }
            }
        }
    }

    private void withJsonLink(MutableRepresentation resource, Map.Entry<String, JsonNode> keyNode, JsonNode valueNode) {
        String rel = keyNode.getKey();
        String href = valueNode.get(HREF).asText();
        String name = optionalNodeValueAsText(valueNode, NAME);
        String title = optionalNodeValueAsText(valueNode, TITLE);
        String hreflang = optionalNodeValueAsText(valueNode, HREFLANG);
        String profile = optionalNodeValueAsText(valueNode, PROFILE);

        resource.withLink(rel, href, name, title, hreflang, profile);
    }

    String optionalNodeValueAsText(JsonNode node, String key) {
        JsonNode value = node.get(key);
        return value != null ? value.asText() : null;
    }

    private void readProperties(MutableRepresentation resource, JsonNode rootNode) throws IOException {
        Iterator<String> fieldNames = rootNode.fieldNames();
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            if (!Support.RESERVED_JSON_PROPERTIES.contains(fieldName)) {
                JsonNode field = rootNode.get(fieldName);
                resource.withProperty(fieldName, field.isNull()
                                                 ? null
                                                 : ( field.isContainerNode() ? field.toString() : field.asText()));
            }
        }

    }

    private void readResources(MutableRepresentation resource, JsonNode rootNode) throws IOException {
        if (rootNode.has(EMBEDDED)) {
            Iterator<Map.Entry<String, JsonNode>> fields = rootNode.get(EMBEDDED).fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> keyNode = fields.next();
                if (keyNode.getValue().isArray()) {
                    Iterator<JsonNode> values = keyNode.getValue().elements();
                    while (values.hasNext()) {
                        JsonNode valueNode = values.next();
                        resource.withRepresentation(keyNode.getKey(), readResource(valueNode));
                    }
                } else {
                    resource.withRepresentation(keyNode.getKey(), readResource(keyNode.getValue()));
                }
            }
        }
    }
}
