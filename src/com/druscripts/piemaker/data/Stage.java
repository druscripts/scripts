package com.druscripts.piemaker.data;

public enum Stage {
    // Order matters
    SETUP("Setup"),
    MAKE_DOUGH("Making Pastry Dough"),
    MAKE_SHELL("Making Pie Shells"),
    MAKE_UNCOOKED("Making Uncooked Pie"),
    COOK("Cooking");

    private final String string;

    Stage(String string) {
        this.string = string;
    }

    public String getString() {
        return string;
    }
}
