package io.prometheus.api;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

public class ValueDeserializer extends JsonDeserializer<Value> {

    @Override
    public Value deserialize(JsonParser p, DeserializationContext cxt) throws IOException, JsonProcessingException {
        JsonNode node = p.getCodec().readTree(p);
        Double timeStamp = Double.valueOf(node.get(0).asText());
        Double value = Double.valueOf(node.get(1).asText());
        return new Value(timeStamp, value);
    }
    
}
