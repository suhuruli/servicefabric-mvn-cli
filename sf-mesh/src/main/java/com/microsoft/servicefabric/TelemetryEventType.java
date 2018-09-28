package com.microsoft.servicefabric;

public enum TelemetryEventType {
    InitMojo(0), AddServiceMojo(1), AddVolumeMojo(2), DeployMojo(3), RemoveMojo(4), AddNetworkMojo(5);
    private final int value;

    private TelemetryEventType(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}
