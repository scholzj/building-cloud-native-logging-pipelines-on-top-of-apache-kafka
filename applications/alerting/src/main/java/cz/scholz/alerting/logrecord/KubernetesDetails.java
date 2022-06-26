package cz.scholz.alerting.logrecord;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class KubernetesDetails {
    private static final long serialVersionUID = 1L;

    private String podName;
    private String namespaceName;
    private String podId;
    private String host;
    private String containerName;
    private String dockerId;
    private String containerHash;
    private String containerImage;

    @JsonProperty("pod_name")
    public String getPodName() {
        return podName;
    }

    public void setPodName(String podName) {
        this.podName = podName;
    }

    @JsonProperty("namespace_name")
    public String getNamespaceName() {
        return namespaceName;
    }

    public void setNamespaceName(String namespaceName) {
        this.namespaceName = namespaceName;
    }

    @JsonProperty("pod_id")
    public String getPodId() {
        return podId;
    }

    public void setPodId(String podId) {
        this.podId = podId;
    }

    @JsonProperty("host")
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    @JsonProperty("container_name")
    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    @JsonProperty("docker_id")
    public String getDockerId() {
        return dockerId;
    }

    public void setDockerId(String dockerId) {
        this.dockerId = dockerId;
    }

    @JsonProperty("container_hash")
    public String getContainerHash() {
        return containerHash;
    }

    public void setContainerHash(String containerHash) {
        this.containerHash = containerHash;
    }

    @JsonProperty("container_image")
    public String getContainerImage() {
        return containerImage;
    }

    public void setContainerImage(String containerImage) {
        this.containerImage = containerImage;
    }
}
