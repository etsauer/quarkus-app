package io.prometheus.api;

import java.io.IOException;
import java.util.Date;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.TextNode;

public class ValueDeserializer extends JsonDeserializer<Value> {

    @Override
    public Value deserialize(JsonParser p, DeserializationContext cxt) throws IOException, JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode node = p.getCodec().readTree(p);
        double timeStamp = (Double) ((DoubleNode) node.get(0)).doubleValue();
        Date time = new Date((long)timeStamp*1000);
        int value = Integer.valueOf(node.get(1).asText());
        return new Value(time, value);
    }
    
}
