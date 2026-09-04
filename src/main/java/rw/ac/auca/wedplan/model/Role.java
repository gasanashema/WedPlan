package rw.ac.auca.wedplan.model;

public enum Role {
    BRIDE("Bride"),
    GROOM("Groom"),
    FAMILY_MEMBER("Family Member");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
