package com.pc1.backendrupay.domain;

public class TicketOptions {
    private String meal;
    private int options;

    public TicketOptions(String meal, int options) {
        this.meal = meal;
        this.options = options;
    }

    public String getMeal() {
        return meal;
    }

    public int getOptions() {
        return options;
    }
}
