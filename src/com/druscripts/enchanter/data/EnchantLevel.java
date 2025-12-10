package com.druscripts.enchanter.data;

import static com.druscripts.enchanter.data.Constants.*;

public enum EnchantLevel {
    LEVEL_1(
        1, 7, "sapphire/opal",
        new RuneRequirement(COSMIC_RUNE, 1),
        new RuneRequirement(WATER_RUNE, 1)
    ),
    LEVEL_2(
        2, 27, "emerald/jade",
        new RuneRequirement(COSMIC_RUNE, 1),
        new RuneRequirement(AIR_RUNE, 3)
    ),
    LEVEL_3(
        3, 49, "ruby/topaz",
        new RuneRequirement(COSMIC_RUNE, 1),
        new RuneRequirement(FIRE_RUNE, 5)
    ),
    LEVEL_4(
        4, 57, "diamond",
        new RuneRequirement(COSMIC_RUNE, 1),
        new RuneRequirement(EARTH_RUNE, 10)
    ),
    LEVEL_5(
        5, 68, "dragonstone",
        new RuneRequirement(COSMIC_RUNE, 1),
        new RuneRequirement(WATER_RUNE, 15),
        new RuneRequirement(EARTH_RUNE, 15)
    ),
    LEVEL_6(
        6, 87, "onyx",
        new RuneRequirement(COSMIC_RUNE, 1),
        new RuneRequirement(FIRE_RUNE, 20),
        new RuneRequirement(EARTH_RUNE, 20)
    ),
    LEVEL_7(
        7, 93, "zenyte",
        new RuneRequirement(COSMIC_RUNE, 1),
        new RuneRequirement(BLOOD_RUNE, 20),
        new RuneRequirement(SOUL_RUNE, 20)
    );

    private final int level;
    private final int magicLevel;
    private final String gemType;
    private final RuneRequirement[] runes;

    EnchantLevel(int level, int magicLevel, String gemType, RuneRequirement... runes) {
        this.level = level;
        this.magicLevel = magicLevel;
        this.gemType = gemType;
        this.runes = runes;
    }

    public int getLevel() {
        return level;
    }

    public int getMagicLevel() {
        return magicLevel;
    }

    public String getGemType() {
        return gemType;
    }

    public RuneRequirement[] getRunes() {
        return runes;
    }

    public String getDisplayName() {
        return "Lvl-" + level + " Enchant (" + gemType + ")";
    }

    public String getRuneString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < runes.length; i++) {
            if (i > 0) sb.append(" + ");
            sb.append(runes[i].getAmount()).append("x ").append(runes[i].getRuneName());
        }
        return sb.toString();
    }

    public int getSpriteId() {
        // Sprite IDs start at 1765 for level 1 and increment by 1
        return Constants.ENCHANT_SPRITE_LVL_1 + (level - 1);
    }

    public static class RuneRequirement {
        private final int runeId;
        private final int amount;

        public RuneRequirement(int runeId, int amount) {
            this.runeId = runeId;
            this.amount = amount;
        }

        public int getRuneId() {
            return runeId;
        }

        public int getAmount() {
            return amount;
        }

        public String getRuneName() {
            switch (runeId) {
                case COSMIC_RUNE: return "Cosmic";
                case WATER_RUNE: return "Water";
                case AIR_RUNE: return "Air";
                case FIRE_RUNE: return "Fire";
                case EARTH_RUNE: return "Earth";
                case BLOOD_RUNE: return "Blood";
                case SOUL_RUNE: return "Soul";
                default: return "Unknown";
            }
        }
    }
}
