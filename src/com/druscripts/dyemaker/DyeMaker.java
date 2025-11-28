package com.druscripts.dyemaker;

import com.druscripts.utils.FreeScript;
import com.druscripts.utils.Task;
import com.druscripts.dyemaker.tasks.*;
import com.osmb.api.script.ScriptDefinition;
import com.osmb.api.script.SkillCategory;
import com.osmb.api.visual.drawing.Canvas;
import com.osmb.api.walker.WalkConfig;

import com.osmb.api.item.ItemGroupResult;
import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.scene.RSTile;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

@ScriptDefinition(
    name = "DyeMaker.druscripts.com",
    description = "Makes dyes at Aggie's shop in Draynor Village.",
    skillCategory = SkillCategory.OTHER,
    version = 1.0,
    author = "dru"
)
public class DyeMaker extends FreeScript {

    // Script state
    public static DyeType selectedDyeType = null;
    public static long startTime = System.currentTimeMillis();
    public static long runStartTime = 0; // Tracks when current run started
    public static int dyesMade = 0;
    public static String task = "Starting...";

    public final WalkConfig walkConfig = new WalkConfig.Builder()
        .tileRandomisationRadius(0)
        .breakDistance(0)
        .build();

    // UI
    private static final Font FONT = new Font("Arial", Font.PLAIN, 12);
    private static final Font BOLD_FONT = new Font("Arial", Font.BOLD, 12);

    private List<Task> tasks;

    public DyeMaker(Object scriptCore) {
        super(scriptCore);
    }

    @Override
    public int[] regionsToPrioritise() {
        return new int[] { Constants.DRAYNOR_REGION };
    }

    @Override
    public void onStart() {
        super.onStart();
        startTime = System.currentTimeMillis();

        tasks = new ArrayList<>();
        tasks.add(new SetupTask(this));
        tasks.add(new MakeDyeTask(this));
        tasks.add(new WalkToBankTask(this));
        tasks.add(new BankTask(this));
        tasks.add(new WalkToAggieTask(this));
    }

    @Override
    public int poll() {
        for (Task t : tasks) {
            if (t.activate()) {
                t.execute();
                return 0;
            }
        }

        return 100;
    }

    @Override
    public void onPaint(Canvas c) {
        long elapsed = System.currentTimeMillis() - startTime;
        String runtime = formatRuntime(elapsed);

        double hours = Math.max(1.0E-9, (double) elapsed / 3600000.0);
        int perHour = (int) Math.round((double) dyesMade / hours);

        int x = 5;
        int y = 40;
        int width = 220;
        int height = 140;

        // Background
        c.fillRect(x, y, width, height, Color.decode("#01031C").getRGB(), 0.8);
        c.drawRect(x, y, width, height, Color.WHITE.getRGB());

        // Text
        int textY = y + 20;
        c.drawText("DyeMaker v" + getVersion(), x + 10, textY, Color.WHITE.getRGB(), BOLD_FONT);
        textY += 20;

        String dyeTypeStr = selectedDyeType != null ? selectedDyeType.getDisplayName() : "Selecting...";
        c.drawText("Making: " + dyeTypeStr, x + 10, textY, Color.CYAN.getRGB(), FONT);
        textY += 20;

        c.drawText("Runtime: " + runtime, x + 10, textY, Color.WHITE.getRGB(), FONT);
        textY += 20;

        c.drawText("Dyes made: " + dyesMade, x + 10, textY, Color.GREEN.getRGB(), FONT);
        textY += 20;

        c.drawText("Per hour: " + perHour, x + 10, textY, Color.CYAN.getRGB(), FONT);
        textY += 20;

        c.drawText("Task: " + task, x + 10, textY, Color.YELLOW.getRGB(), FONT);
    }

    private String formatRuntime(long millis) {
        long seconds = millis / 1000L;
        long hours = seconds / 3600L;
        long minutes = (seconds % 3600L) / 60L;
        long secs = seconds % 60L;
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }

    @Override
    public boolean promptBankTabDialogue() {
        return true;
    }

    // Shared helper methods for tasks

    public boolean hasMaterials() {
        if (selectedDyeType == null) return false;
        ItemGroupResult inv = getWidgetManager().getInventory().search(
            Set.of(selectedDyeType.getIngredientId(), Constants.COINS_ID)
        );
        if (inv == null) return false;
        if (!inv.contains(Constants.COINS_ID)) return false;
        if (!inv.contains(selectedDyeType.getIngredientId())) return false;
        int amount = inv.getAmount(new int[]{selectedDyeType.getIngredientId()});
        return amount >= selectedDyeType.getIngredientCount();
    }

    public boolean hasDyes() {
        if (selectedDyeType == null) return false;
        ItemGroupResult inv = getWidgetManager().getInventory().search(Set.of(selectedDyeType.getDyeId()));
        return inv != null && inv.contains(selectedDyeType.getDyeId());
    }

    public boolean isAtPosition(WorldPosition pos, WorldPosition target) {
        return pos != null && target != null &&
               pos.getX() == target.getX() &&
               pos.getY() == target.getY() &&
               pos.getPlane() == target.getPlane();
    }

    public boolean walkToTile(WorldPosition target) {
        RSTile tile = getSceneManager().getTile(target);
        if (tile == null) return false;
        if (!tile.interact("Walk here")) return false;
        return pollFramesHuman(() -> isAtPosition(getWorldPosition(), target), 5000, true);
    }

    public boolean isInBankArea(WorldPosition pos) {
        if (pos == null) return false;
        int px = pos.getX();
        int py = pos.getY();
        int pz = pos.getPlane();
        int ax = Constants.DRAYNOR_BANK_AREA.getX();
        int ay = Constants.DRAYNOR_BANK_AREA.getY();
        int width = Constants.DRAYNOR_BANK_AREA.getWidth();
        int height = Constants.DRAYNOR_BANK_AREA.getHeight();
        int plane = Constants.DRAYNOR_BANK_AREA.getPlane();
        return pz == plane && px >= ax && px < ax + width && py >= ay && py < ay + height;
    }

    public WorldPosition getRandomBankTile() {
        Random rand = new Random();
        int x = Constants.DRAYNOR_BANK_AREA.getX() + rand.nextInt(Constants.DRAYNOR_BANK_AREA.getWidth());
        int y = Constants.DRAYNOR_BANK_AREA.getY() + rand.nextInt(Constants.DRAYNOR_BANK_AREA.getHeight());
        return new WorldPosition(x, y, Constants.DRAYNOR_BANK_AREA.getPlane());
    }

    public boolean isInAggieShop(WorldPosition pos) {
        if (pos == null) return false;
        int px = pos.getX();
        int py = pos.getY();
        int pz = pos.getPlane();

        for (com.osmb.api.location.area.impl.RectangleArea area : Constants.AGGIE_SHOP_AREAS) {
            int ax = area.getX();
            int ay = area.getY();
            int width = area.getWidth();
            int height = area.getHeight();
            int plane = area.getPlane();

            if (pz == plane && px >= ax && px < ax + width && py >= ay && py < ay + height) {
                return true;
            }
        }
        return false;
    }
}
