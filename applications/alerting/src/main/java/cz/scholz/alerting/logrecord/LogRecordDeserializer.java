package cz.scholz.alerting.logrecord;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.logging.Log;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

public class LogRecordDeserializer implements Deserializer<LogRecord> {
    @Override
    public void configure(Map map, boolean b) {
        // Nothing to do
    }

    @Override
    public LogRecord deserialize(String s, byte[] bytes) {
        ObjectMapper mapper = new ObjectMapper();
        LogRecord obj = null;

        try {
            obj = mapper.readValue(bytes, LogRecord.class);
        } catch (Exception e) {
            Log.error("Failed to deserialize LogRecord", e);
        }

        return obj;
    }

    @Override
    public void close() {
        // Nothing to close
    }
}
