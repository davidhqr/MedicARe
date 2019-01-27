package dev.medicare.models;

import java.util.Date;

public class Dosage {
    private String name;
    private Date timestamp;
    private int numPills;
    private int strength;

    public Dosage(String name, int numPills, int strength) {
        this.name = name;
        this.timestamp = new Date();
        this.numPills = numPills;
        this.strength = strength;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public int getNumPills() {
        return numPills;
    }

    public void setNumPills(int numPills) {
        this.numPills = numPills;
    }

    public int getStrength() {
        return strength;
    }

    public void setStrength(int strength) {
        this.strength = strength;
    }
}
