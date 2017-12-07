package fr.troopy28.replication;

/**
 * @author troopy28
 * @since 1.0.0
 */
public class ReplicationConfig {
    private int redisPort;
    private String redisHost;
    private String redisPassword;

    private String replicationPermissionName;
    private boolean debug;

    public int getRedisPort() {
        return redisPort;
    }

    public void setRedisPort(int redisPort) {
        this.redisPort = redisPort;
    }

    public String getRedisHost() {
        return redisHost;
    }

    public void setRedisHost(String redisHost) {
        this.redisHost = redisHost;
    }

    public String getRedisPassword() {
        return redisPassword;
    }

    public void setRedisPassword(String redisPassword) {
        this.redisPassword = redisPassword;
    }

    public String getReplicationPermissionName() {
        return replicationPermissionName;
    }

    public void setReplicationPermissionName(String replicationPermissionName) {
        this.replicationPermissionName = replicationPermissionName;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }
}
