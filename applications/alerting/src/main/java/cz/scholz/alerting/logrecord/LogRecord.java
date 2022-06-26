package cz.scholz.alerting.logrecord;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.Date;

@RegisterForReflection
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LogRecord {
    private static final long serialVersionUID = 1L;

    private Date timestamp;
    private String stream;
    private String logtag;
    private String message;
    private KubernetesDetails kubernetes;

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getStream() {
        return stream;
    }

    public void setStream(String stream) {
        this.stream = stream;
    }

    public String getLogtag() {
        return logtag;
    }

    public void setLogtag(String logtag) {
        this.logtag = logtag;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public KubernetesDetails getKubernetes() {
        return kubernetes;
    }

    public void setKubernetes(KubernetesDetails kubernetes) {
        this.kubernetes = kubernetes;
    }
}
