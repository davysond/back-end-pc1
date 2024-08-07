package com.pc1.backendrupay.domain;

public class TicketOptions {
    private int LUNCH;
    private int DINNER;

    public TicketOptions(int LUNCH, int DINNER) {
        this.LUNCH = LUNCH;
        this.DINNER = DINNER;
    }

    public int getMeal() {
        return LUNCH;
    }

    public int getOptions() {
        return DINNER;
    }
}
