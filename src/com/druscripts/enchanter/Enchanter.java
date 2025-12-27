package com.druscripts.enchanter;

import com.druscripts.enchanter.data.Constants;
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
    version = 1.1,
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

    public int bankUnenchanted = 0;
    public int bankEnchanted = 0;

    // Batch calculation
    public int runeSlots = 0;        // Number of inventory slots needed for runes
    public int maxBatchSize = 0;

    public Enchanter(Object scriptCore) {
        super(scriptCore);
    }

    @Override
    public int[] regionsToPrioritise() {
        return new int[] { Constants.GRAND_EXCHANGE_REGION };
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
    private final int NUM_LINES = 10;
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

        paintUpdateNotice(c, WIDTH, NUM_LINES);
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

    // Track XP and items for the current lap (to send when completing lap)
    private int lapItemsEnchanted = 0;
    private int lapXpGained = 0;
    private int lastKnownMagicLevel = 0;

    public void increaseItemsEnchanted(int count) {
        itemsEnchanted += count;
        lapItemsEnchanted += count;
        // Calculate XP gained
        int xpPerItem = Constants.XP_PER_LEVEL[enchantLevel.getLevel()];
        lapXpGained += count * xpPerItem;
    }

    /**
     * Gets the current magic level from the XP tracker.
     */
    private int getCurrentMagicLevel() {
        XPTracker magicTracker = getXPTrackers().get(SkillType.MAGIC);
        if (magicTracker != null) {
            return magicTracker.getLevel();
        }
        return 0;
    }

    /**
     * Called when banking completes - sends stats for the lap.
     * Stats are sent in format: lvl_{level}_{mode}_{stat}
     * e.g., lvl_1_xp, lvl_1_hyper_xp, lvl_1_items, lvl_1_hyper_items
     */
    public void completeLap() {
        // Check for level ups
        int currentMagicLevel = getCurrentMagicLevel();
        int levelsGainedThisLap = 0;

        if (lastKnownMagicLevel > 0 && currentMagicLevel > lastKnownMagicLevel) {
            levelsGainedThisLap = currentMagicLevel - lastKnownMagicLevel;
        }

        // Update last known level (or initialize it)
        if (currentMagicLevel > 0) {
            lastKnownMagicLevel = currentMagicLevel;
        }

        if (firstRoundComplete && lapItemsEnchanted > 0) {
            // Build stat keys based on level and mode
            int level = enchantLevel.getLevel();
            String prefix = hyperEfficientMode
                ? "lvl_" + level + "_hyper_"
                : "lvl_" + level + "_";

            // Send XP
            sendStat(prefix + "xp", lapXpGained);

            // Send items enchanted
            sendStat(prefix + "items", lapItemsEnchanted);

            // Send lap time (runtime for this lap)
            long lapTimeMs = System.currentTimeMillis() - lapStartTime;
            sendStat(prefix + "runtime_ms", lapTimeMs);

            // Send levels gained (if any)
            if (levelsGainedThisLap > 0) {
                sendStat(prefix + "lvls", levelsGainedThisLap);
            }
        }

        firstRoundComplete = true;
        lapStartTime = System.currentTimeMillis();
        lapItemsEnchanted = 0;
        lapXpGained = 0;
    }
}
