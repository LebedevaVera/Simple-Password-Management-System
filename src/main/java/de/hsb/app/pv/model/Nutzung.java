package de.hsb.app.pv.model;

public enum Nutzung {
    SEHR_HAEUFIG("Sehr häufig"), HAEUFIG("Häufig"), OEFTERS("Öfters"), SELTEN("Selten"), SEHR_SELTEN("Sehr selten");
    private final String label;
    Nutzung(String label) {
        this.label= label;
    }

    public String getLabel() {

        return label;
    }
}
