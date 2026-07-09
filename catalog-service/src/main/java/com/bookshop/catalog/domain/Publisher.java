package com.bookshop.catalog.domain;

public enum Publisher {
    Polar("Polar Publications"),
    O_Reilly("O'Reilly Media"),
    Manning("Manning Publications"),
    Addison_Wesley("Addison-Wesley Professional");

    private final String name;
    Publisher(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Publisher name cannot be null or blank");
        }
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
