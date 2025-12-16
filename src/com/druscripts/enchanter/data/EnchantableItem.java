package com.druscripts.enchanter.data;

public enum EnchantableItem {
    // Level 1 - Sapphire
    SAPPHIRE_RING_ITEM(EnchantLevel.LEVEL_1, Constants.SAPPHIRE_RING, Constants.RING_OF_RECOIL, "Sapphire ring", "Ring of recoil"),
    SAPPHIRE_NECKLACE_ITEM(EnchantLevel.LEVEL_1, Constants.SAPPHIRE_NECKLACE, Constants.GAMES_NECKLACE, "Sapphire necklace", "Games necklace"),
    SAPPHIRE_BRACELET_ITEM(EnchantLevel.LEVEL_1, Constants.SAPPHIRE_BRACELET, Constants.BRACELET_OF_CLAY, "Sapphire bracelet", "Bracelet of clay"),
    SAPPHIRE_AMULET_ITEM(EnchantLevel.LEVEL_1, Constants.SAPPHIRE_AMULET, Constants.AMULET_OF_MAGIC, "Sapphire amulet", "Amulet of magic"),

    // Level 1 - Opal
    OPAL_RING_ITEM(EnchantLevel.LEVEL_1, Constants.OPAL_RING, Constants.RING_OF_PURSUIT, "Opal ring", "Ring of pursuit"),
    OPAL_NECKLACE_ITEM(EnchantLevel.LEVEL_1, Constants.OPAL_NECKLACE, Constants.DODGY_NECKLACE, "Opal necklace", "Dodgy necklace"),
    OPAL_BRACELET_ITEM(EnchantLevel.LEVEL_1, Constants.OPAL_BRACELET, Constants.EXPEDITIOUS_BRACELET, "Opal bracelet", "Expeditious bracelet"),
    OPAL_AMULET_ITEM(EnchantLevel.LEVEL_1, Constants.OPAL_AMULET, Constants.AMULET_OF_BOUNTY, "Opal amulet", "Amulet of bounty"),

    // Level 2 - Emerald
    EMERALD_RING_ITEM(EnchantLevel.LEVEL_2, Constants.EMERALD_RING, Constants.RING_OF_DUELING, "Emerald ring", "Ring of dueling"),
    EMERALD_NECKLACE_ITEM(EnchantLevel.LEVEL_2, Constants.EMERALD_NECKLACE, Constants.BINDING_NECKLACE, "Emerald necklace", "Binding necklace"),
    EMERALD_BRACELET_ITEM(EnchantLevel.LEVEL_2, Constants.EMERALD_BRACELET, Constants.CASTLE_WARS_BRACELET, "Emerald bracelet", "Castle wars bracelet"),
    EMERALD_AMULET_ITEM(EnchantLevel.LEVEL_2, Constants.EMERALD_AMULET, Constants.AMULET_OF_DEFENCE, "Emerald amulet", "Amulet of defence"),

    // Level 2 - Jade
    JADE_RING_ITEM(EnchantLevel.LEVEL_2, Constants.JADE_RING, Constants.RING_OF_RETURNING, "Jade ring", "Ring of returning"),
    JADE_NECKLACE_ITEM(EnchantLevel.LEVEL_2, Constants.JADE_NECKLACE, Constants.NECKLACE_OF_PASSAGE, "Jade necklace", "Necklace of passage"),
    JADE_BRACELET_ITEM(EnchantLevel.LEVEL_2, Constants.JADE_BRACELET, Constants.FLAMTAER_BRACELET, "Jade bracelet", "Flamtaer bracelet"),
    JADE_AMULET_ITEM(EnchantLevel.LEVEL_2, Constants.JADE_AMULET, Constants.AMULET_OF_CHEMISTRY, "Jade amulet", "Amulet of chemistry"),

    // Level 3 - Ruby
    RUBY_RING_ITEM(EnchantLevel.LEVEL_3, Constants.RUBY_RING, Constants.RING_OF_FORGING, "Ruby ring", "Ring of forging"),
    RUBY_NECKLACE_ITEM(EnchantLevel.LEVEL_3, Constants.RUBY_NECKLACE, Constants.DIGSITE_PENDANT, "Ruby necklace", "Digsite pendant"),
    RUBY_BRACELET_ITEM(EnchantLevel.LEVEL_3, Constants.RUBY_BRACELET, Constants.INOCULATION_BRACELET, "Ruby bracelet", "Inoculation bracelet"),
    RUBY_AMULET_ITEM(EnchantLevel.LEVEL_3, Constants.RUBY_AMULET, Constants.AMULET_OF_STRENGTH, "Ruby amulet", "Amulet of strength"),

    // Level 3 - Topaz
    TOPAZ_RING_ITEM(EnchantLevel.LEVEL_3, Constants.TOPAZ_RING, Constants.EFARITAYS_AID, "Topaz ring", "Efaritay's aid"),
    TOPAZ_NECKLACE_ITEM(EnchantLevel.LEVEL_3, Constants.TOPAZ_NECKLACE, Constants.NECKLACE_OF_FAITH, "Topaz necklace", "Necklace of faith"),
    TOPAZ_BRACELET_ITEM(EnchantLevel.LEVEL_3, Constants.TOPAZ_BRACELET, Constants.BRACELET_OF_SLAUGHTER, "Topaz bracelet", "Bracelet of slaughter"),
    TOPAZ_AMULET_ITEM(EnchantLevel.LEVEL_3, Constants.TOPAZ_AMULET, Constants.BURNING_AMULET, "Topaz amulet", "Burning amulet"),

    // Level 4 - Diamond
    DIAMOND_RING_ITEM(EnchantLevel.LEVEL_4, Constants.DIAMOND_RING, Constants.RING_OF_LIFE, "Diamond ring", "Ring of life"),
    DIAMOND_NECKLACE_ITEM(EnchantLevel.LEVEL_4, Constants.DIAMOND_NECKLACE, Constants.PHOENIX_NECKLACE, "Diamond necklace", "Phoenix necklace"),
    DIAMOND_BRACELET_ITEM(EnchantLevel.LEVEL_4, Constants.DIAMOND_BRACELET, Constants.ABYSSAL_BRACELET, "Diamond bracelet", "Abyssal bracelet"),
    DIAMOND_AMULET_ITEM(EnchantLevel.LEVEL_4, Constants.DIAMOND_AMULET, Constants.AMULET_OF_POWER, "Diamond amulet", "Amulet of power"),

    // Level 5 - Dragonstone
    DRAGONSTONE_RING_ITEM(EnchantLevel.LEVEL_5, Constants.DRAGONSTONE_RING, Constants.RING_OF_WEALTH_UNCHARGED, "Dragonstone ring", "Ring of wealth (uncharged)"),
    DRAGONSTONE_NECKLACE_ITEM(EnchantLevel.LEVEL_5, Constants.DRAGONSTONE_NECKLACE, Constants.SKILLS_NECKLACE_UNCHARGED, "Dragonstone necklace", "Skills necklace (uncharged)"),
    DRAGONSTONE_BRACELET_ITEM(EnchantLevel.LEVEL_5, Constants.DRAGONSTONE_BRACELET, Constants.COMBAT_BRACELET_UNCHARGED, "Dragonstone bracelet", "Combat bracelet (uncharged)"),
    DRAGONSTONE_AMULET_ITEM(EnchantLevel.LEVEL_5, Constants.DRAGONSTONE_AMULET, Constants.AMULET_OF_GLORY_UNCHARGED, "Dragonstone amulet", "Amulet of glory (uncharged)"),

    // Level 6 - Onyx
    ONYX_RING_ITEM(EnchantLevel.LEVEL_6, Constants.ONYX_RING, Constants.RING_OF_STONE, "Onyx ring", "Ring of stone"),
    ONYX_NECKLACE_ITEM(EnchantLevel.LEVEL_6, Constants.ONYX_NECKLACE, Constants.BERSERKER_NECKLACE, "Onyx necklace", "Berserker necklace"),
    ONYX_BRACELET_ITEM(EnchantLevel.LEVEL_6, Constants.ONYX_BRACELET, Constants.REGEN_BRACELET, "Onyx bracelet", "Regen bracelet"),
    ONYX_AMULET_ITEM(EnchantLevel.LEVEL_6, Constants.ONYX_AMULET, Constants.AMULET_OF_FURY, "Onyx amulet", "Amulet of fury"),

    // Level 7 - Zenyte
    ZENYTE_RING_ITEM(EnchantLevel.LEVEL_7, Constants.ZENYTE_RING, Constants.RING_OF_SUFFERING, "Zenyte ring", "Ring of suffering"),
    ZENYTE_NECKLACE_ITEM(EnchantLevel.LEVEL_7, Constants.ZENYTE_NECKLACE, Constants.NECKLACE_OF_ANGUISH, "Zenyte necklace", "Necklace of anguish"),
    ZENYTE_BRACELET_ITEM(EnchantLevel.LEVEL_7, Constants.ZENYTE_BRACELET, Constants.TORMENTED_BRACELET, "Zenyte bracelet", "Tormented bracelet"),
    ZENYTE_AMULET_ITEM(EnchantLevel.LEVEL_7, Constants.ZENYTE_AMULET, Constants.AMULET_OF_TORTURE, "Zenyte amulet", "Amulet of torture");

    private final EnchantLevel level;
    private final int unenchantedId;
    private final int enchantedId;
    private final String unenchantedName;
    private final String enchantedName;

    EnchantableItem(EnchantLevel level, int unenchantedId, int enchantedId,
                    String unenchantedName, String enchantedName) {
        this.level = level;
        this.unenchantedId = unenchantedId;
        this.enchantedId = enchantedId;
        this.unenchantedName = unenchantedName;
        this.enchantedName = enchantedName;
    }

    public EnchantLevel getLevel() {
        return level;
    }

    public int getUnenchantedId() {
        return unenchantedId;
    }

    public int getEnchantedId() {
        return enchantedId;
    }

    public String getUnenchantedName() {
        return unenchantedName;
    }

    public String getEnchantedName() {
        return enchantedName;
    }

    public String getDisplayString() {
        return unenchantedName + " (" + enchantedName + ")";
    }

    public static EnchantableItem[] getItemsForLevel(EnchantLevel level) {
        return java.util.Arrays.stream(values())
            .filter(item -> item.level == level)
            .toArray(EnchantableItem[]::new);
    }
}
