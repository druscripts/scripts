package com.druscripts.enchanter;

import com.druscripts.enchanter.data.EnchantableItem;
import com.druscripts.enchanter.data.EnchantLevel;
import com.druscripts.enchanter.data.Stage;
import com.druscripts.enchanter.tasks.BankTask;
import com.druscripts.enchanter.tasks.EnchantTask;
import com.druscripts.enchanter.tasks.SetupTask;
import com.druscripts.utils.paint.PaintStyle;
import com.druscripts.utils.script.FreeScript;

import com.osmb.api.script.ScriptDefinition;
import com.osmb.api.script.SkillCategory;

@ScriptDefinition(
    name = "Enchanter.druscripts.com",
    description = "Enchants jewellery using the standard spellbook - supports all enchant levels",
    skillCategory = SkillCategory.MAGIC,
    version = 0.1,
    author = "dru"
)
public class Enchanter extends FreeScript {

    public Stage stage = Stage.SETUP;
    public String task = "Starting...";
    public long startTime = System.currentTimeMillis();

    // Configuration
    public EnchantLevel enchantLevel = EnchantLevel.LEVEL_1;
    public EnchantableItem enchantableItem = EnchantableItem.SAPPHIRE_RING_ITEM;

    // Stats
    public int itemsEnchanted = 0;
    public boolean firstRoundComplete = false;
    public long lapStartTime = System.currentTimeMillis();

    // Bank tracking
    public int bankUnenchanted = 0;
    public int bankEnchanted = 0;

    private static final int GRAND_EXCHANGE_REGION = 12598;

    public Enchanter(Object scriptCore) {
        super(scriptCore);
    }

    @Override
    public int[] regionsToPrioritise() {
        return new int[] { GRAND_EXCHANGE_REGION };
    }

    @Override
    public void onStart() {
        super.onStart();
        log(getClass().getSimpleName(), "Starting Enchanter v" + getVersion());

        startTime = System.currentTimeMillis();
        tasks.add(new SetupTask(this));
    }

    @Override
    public boolean promptBankTabDialogue() {
        return true;
    }

    private final int WIDTH = 200;
    private final int NUM_LINES = 10;

    @Override
    public void onPaint(com.osmb.api.visual.drawing.Canvas c) {
        long elapsed = System.currentTimeMillis() - startTime;
        String runtime = formatRuntime(elapsed);

        PaintStyle.drawBackground(c, WIDTH, NUM_LINES);
        int y = PaintStyle.drawTitle(c, "Enchanter v" + getVersion());
        y = PaintStyle.drawLine(c, "Level: " + enchantLevel.getDisplayName(), y, PaintStyle.TEXT_COLOR_BRAND);
        y = PaintStyle.drawLine(c, "Item: " + enchantableItem.getUnenchantedName(), y, PaintStyle.TEXT_COLOR_TASK);
        y = PaintStyle.drawLine(c, "Stage: " + stage.getString(), y, PaintStyle.TEXT_COLOR_BODY);
        y = PaintStyle.drawLine(c, "Runtime: " + runtime, y, PaintStyle.TEXT_COLOR_BODY);

        y = PaintStyle.drawLine(c, "Stats:", y, PaintStyle.TEXT_COLOR_TITLE);
        y = PaintStyle.drawLine(c, "  Enchanted: " + itemsEnchanted, y, PaintStyle.TEXT_COLOR_SUCCESS);

        y = PaintStyle.drawLine(c, "Bank:", y, PaintStyle.TEXT_COLOR_TITLE);
        y = PaintStyle.drawLine(c, "  Unenchanted: " + bankUnenchanted, y, PaintStyle.TEXT_COLOR_MUTED);
        PaintStyle.drawLine(c, "  Enchanted: " + bankEnchanted, y, PaintStyle.TEXT_COLOR_MUTED);
    }

    public void initializeTasks() {
        tasks.clear();
        tasks.add(new EnchantTask(this));
        tasks.add(new BankTask(this));
        log(getClass().getSimpleName(), "Tasks initialized");
    }

    private String formatRuntime(long millis) {
        long seconds = millis / 1000L;
        long hours = seconds / 3600L;
        long minutes = seconds % 3600L / 60L;
        long secs = seconds % 60L;
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }

    // Stat names for server
    private static final String STAT_ITEMS_ENCHANTED = "items_enchanted";
    private static final String STAT_LAP_TIME = "enchant_lap_time";

    public void increaseItemsEnchanted(int count) {
        itemsEnchanted += count;
        if (firstRoundComplete) {
            sendStat(STAT_ITEMS_ENCHANTED, count);
        }
    }

    public void completeLap() {
        if (firstRoundComplete) {
            long lapTimeMs = System.currentTimeMillis() - lapStartTime;
            sendStat(STAT_LAP_TIME, lapTimeMs);
        }
        firstRoundComplete = true;
        lapStartTime = System.currentTimeMillis();
    }
}
