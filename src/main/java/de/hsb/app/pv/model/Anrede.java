package de.hsb.app.pv.model;

public enum Anrede {
    HERR("Herr"), FRAU("Frau"), FIRMA("Firma");
    private final String label;
    Anrede(String label) {
        this.label= label;
    }

    public String getLabel() {
    	
        return label;
    }
}
