package com.druscripts.enchanter.data;

public enum Stage {
    SETUP("Setup"),
    BANKING("Banking"),
    ENCHANTING("Enchanting");

    private final String displayString;

    Stage(String displayString) {
        this.displayString = displayString;
    }

    public String getString() {
        return displayString;
    }
}
