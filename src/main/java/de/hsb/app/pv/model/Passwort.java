package de.hsb.app.pv.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Passwort {
    @Id
    @GeneratedValue
    private int id;
    private String pw;
    private String usedFor;
    private Nutzung nutzung;

    public Passwort() {
    }

    public Passwort(String pw, String usedFor, Nutzung nutzung) {
        this.pw = pw;
        this.usedFor = usedFor;
        this.nutzung = nutzung;
    }

    public String getPw() {
        return pw;
    }

    public void setPw(String pw) {
        this.pw = pw;
    }

    public String getUsedFor() {
        return usedFor;
    }

    public void setUsedFor(String usedFor) {
        this.usedFor = usedFor;
    }

    public int getId() {
        return id;
    }

    public Nutzung getNutzung() {
        return nutzung;
    }

    public void setNutzung(Nutzung nutzung) {
        this.nutzung = nutzung;
    }

    public Nutzung[] getNutzungValues() {
        return Nutzung.values();
    }
}
