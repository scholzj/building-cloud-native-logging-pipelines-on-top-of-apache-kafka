package cz.scholz.alerting.logrecord;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.logging.Log;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class LogRecordSerializer implements Serializer<LogRecord> {
    @Override
    public void configure(Map map, boolean b) {
        // Nothing to configure
    }

    @Override
    public byte[] serialize(String s, LogRecord o) {
        byte[] retVal = null;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            retVal = objectMapper.writeValueAsBytes(o);
        } catch (Exception e) {
            Log.error("Failed to serialize LogRecord", e);
        }
        return retVal;
    }

    @Override
    public void close() {
        // Nothing to do
    }
}
