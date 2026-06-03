package org.karane.guitar.domain;

public record Finish(String color, Finish.Type type) {

    public enum Type { GLOSS, SATIN, MATTE }

    public Finish {
        if (color == null || color.isBlank()) throw new IllegalArgumentException("color must not be blank");
    }

    @Override
    public String toString() { return color + " " + type.name().toLowerCase(); }
}
