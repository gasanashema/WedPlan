package rw.ac.auca.wedplan.model;

public enum Side {
    BRIDE("Bride Side"),
    GROOM("Groom Side");

    private final String label;

    Side(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
