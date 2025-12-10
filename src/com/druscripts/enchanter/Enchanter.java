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
import com.osmb.api.trackers.experience.XPTracker;
import com.osmb.api.ui.component.tabs.skill.SkillType;

import java.text.DecimalFormat;

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
    public boolean hyperEfficientMode = false;

    // Stats
    public int itemsEnchanted = 0;
    public boolean firstRoundComplete = false;
    public long lapStartTime = System.currentTimeMillis();
    public int startMagicLevel = 0;

    // Bank tracking
    public int bankUnenchanted = 0;
    public int bankEnchanted = 0;

    // Batch calculation (set during setup)
    public int runeSlots = 0;        // Number of inventory slots needed for runes
    public int maxBatchSize = 0;     // Max items we can enchant per trip (based on inventory space)

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

    @Override
    public boolean trackXP() {
        return true;
    }

    private final int WIDTH = 210;
    private final int NUM_LINES = 11;
    private static final DecimalFormat FORMAT = new DecimalFormat("#,###");

    @Override
    public void onPaint(com.osmb.api.visual.drawing.Canvas c) {
        long elapsed = System.currentTimeMillis() - startTime;
        String runtime = formatRuntime(elapsed);

        // Get XP tracker for magic level
        XPTracker magicTracker = getXPTrackers().get(SkillType.MAGIC);
        String levelStr = "-";
        if (magicTracker != null) {
            int currentLevel = magicTracker.getLevel();
            // Capture starting level on first valid read
            if (startMagicLevel == 0 && currentLevel > 0) {
                startMagicLevel = currentLevel;
            }
            int levelsGained = currentLevel - startMagicLevel;
            levelStr = levelsGained > 0 ? currentLevel + " (+" + levelsGained + ")" : String.valueOf(currentLevel);
        }

        PaintStyle.drawBackground(c, WIDTH, NUM_LINES);
        int y = PaintStyle.drawTitle(c, "Enchanter v" + getVersion());
        y = PaintStyle.drawLine(c, "Spell: " + enchantLevel.getSpellName(), y, PaintStyle.TEXT_COLOR_BRAND);
        y = PaintStyle.drawLine(c, "Item: " + enchantableItem.getUnenchantedName(), y, PaintStyle.TEXT_COLOR_TASK);
        y = PaintStyle.drawLine(c, "Stage: " + stage.getString(), y, PaintStyle.TEXT_COLOR_BODY);
        y = PaintStyle.drawLine(c, "Runtime: " + runtime, y, PaintStyle.TEXT_COLOR_BODY);
        y = PaintStyle.drawLine(c, "Magic Level: " + levelStr, y, PaintStyle.TEXT_COLOR_BODY);

        y = PaintStyle.drawLine(c, "Stats:", y, PaintStyle.TEXT_COLOR_TITLE);
        y = PaintStyle.drawLine(c, "  Enchanted: " + FORMAT.format(itemsEnchanted), y, PaintStyle.TEXT_COLOR_SUCCESS);

        y = PaintStyle.drawLine(c, "Bank:", y, PaintStyle.TEXT_COLOR_TITLE);
        y = PaintStyle.drawLine(c, "  Unenchanted: " + FORMAT.format(bankUnenchanted), y, PaintStyle.TEXT_COLOR_MUTED);
        PaintStyle.drawLine(c, "  Enchanted: " + FORMAT.format(bankEnchanted), y, PaintStyle.TEXT_COLOR_MUTED);
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
