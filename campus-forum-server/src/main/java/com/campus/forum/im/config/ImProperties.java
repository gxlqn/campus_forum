package com.campus.forum.im.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@ConfigurationProperties(prefix = "im")
public class ImProperties {

    private String endpoint = "/ws-im";
    private Integer heartbeatServerMs = 10000;
    private Integer heartbeatClientMs = 10000;
    private Integer ackTimeoutMs = 10000;
    private Integer syncBatchSize = 50;
    private Integer retryScanIntervalMs = 5000;
    private Integer maxRetryAttempts = 6;
    private Integer rateLimitPerMinute = 120;
    private Cluster cluster = new Cluster();

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public Integer getHeartbeatServerMs() {
        return heartbeatServerMs;
    }

    public void setHeartbeatServerMs(Integer heartbeatServerMs) {
        this.heartbeatServerMs = heartbeatServerMs;
    }

    public Integer getHeartbeatClientMs() {
        return heartbeatClientMs;
    }

    public void setHeartbeatClientMs(Integer heartbeatClientMs) {
        this.heartbeatClientMs = heartbeatClientMs;
    }

    public Integer getAckTimeoutMs() {
        return ackTimeoutMs;
    }

    public void setAckTimeoutMs(Integer ackTimeoutMs) {
        this.ackTimeoutMs = ackTimeoutMs;
    }

    public Integer getSyncBatchSize() {
        return syncBatchSize;
    }

    public void setSyncBatchSize(Integer syncBatchSize) {
        this.syncBatchSize = syncBatchSize;
    }

    public Integer getRetryScanIntervalMs() {
        return retryScanIntervalMs;
    }

    public void setRetryScanIntervalMs(Integer retryScanIntervalMs) {
        this.retryScanIntervalMs = retryScanIntervalMs;
    }

    public Integer getMaxRetryAttempts() {
        return maxRetryAttempts;
    }

    public void setMaxRetryAttempts(Integer maxRetryAttempts) {
        this.maxRetryAttempts = maxRetryAttempts;
    }

    public Integer getRateLimitPerMinute() {
        return rateLimitPerMinute;
    }

    public void setRateLimitPerMinute(Integer rateLimitPerMinute) {
        this.rateLimitPerMinute = rateLimitPerMinute;
    }

    public Cluster getCluster() {
        return cluster;
    }

    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
    }

    public static class Cluster {
        private String mode = "local";
        private String channel = "im:dispatch";
        private String nodeId = UUID.randomUUID().toString();

        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }

        public String getChannel() {
            return channel;
        }

        public void setChannel(String channel) {
            this.channel = channel;
        }

        public String getNodeId() {
            return nodeId;
        }

        public void setNodeId(String nodeId) {
            this.nodeId = nodeId;
        }
    }
}
