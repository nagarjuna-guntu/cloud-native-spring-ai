package com.bookshop.catalog.domain;

public enum Publisher {
    Polar("Polar Publications"),
    O_Reilly("O'Reilly Media"),
    Manning("Manning Publications"),
    Addison_Wesley("Addison-Wesley Professional");

    private final String name;
    Publisher(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
