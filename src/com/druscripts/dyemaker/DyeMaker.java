package com.druscripts.dyemaker;

import com.druscripts.utils.location.AreaUtils;
import com.druscripts.utils.widget.InventoryUtils;
import com.druscripts.utils.widget.exception.CannotOpenWidgetException;
import com.druscripts.utils.script.FreeScript;
import com.druscripts.utils.paint.PaintStyle;
import com.druscripts.utils.script.Task;
import com.druscripts.dyemaker.data.Constants;
import com.druscripts.dyemaker.data.DyeType;
import com.druscripts.dyemaker.tasks.*;

import com.osmb.api.script.ScriptDefinition;
import com.osmb.api.script.SkillCategory;
import com.osmb.api.visual.drawing.Canvas;
import com.osmb.api.walker.WalkConfig;
import com.osmb.api.item.ItemGroupResult;
import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.scene.RSTile;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

@ScriptDefinition(
    name = "DyeMaker.druscripts.com",
    description = "Makes dyes at Aggie's shop in Draynor Village.",
    skillCategory = SkillCategory.OTHER,
    version = 1.2,
    author = "dru"
)
public class DyeMaker extends FreeScript {

    // Script state
    public DyeType selectedDyeType = null;
    public long startTime = System.currentTimeMillis();
    public long lapStartTime = System.currentTimeMillis();
    public int dyesMade = 0;
    public String task = "Starting...";
    public boolean firstRoundComplete = false;

    private final Random random = new Random();

    public final WalkConfig walkConfig = new WalkConfig.Builder()
        .tileRandomisationRadius(0)
        .breakDistance(0)
        .build();


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

    private final int WIDTH = 200;
    private final int NUM_LINES = 5;

    @Override
    public void onPaint(Canvas c) {
        long elapsed = System.currentTimeMillis() - startTime;
        String runtime = formatRuntime(elapsed);

        double hours = Math.max(1/3600.0, (double) elapsed / 3600000.0);
        int perHour = (int) Math.floor((double) dyesMade / hours);

        String dyeTypeStr = selectedDyeType != null ? selectedDyeType.getDisplayName() : "Selecting...";

        PaintStyle.drawBackground(c, WIDTH, NUM_LINES);
        int y = PaintStyle.drawTitle(c, "DyeMaker v" + getVersion());
        y = PaintStyle.drawLine(c, "Making: " + dyeTypeStr, y, PaintStyle.TEXT_COLOR_BRAND);
        y = PaintStyle.drawLine(c, "Task: " + task, y, PaintStyle.TEXT_COLOR_TASK);
        y = PaintStyle.drawLine(c, "Runtime: " + runtime, y, PaintStyle.TEXT_COLOR_BODY);
        y = PaintStyle.drawLine(c, "Dyes made: " + dyesMade, y, PaintStyle.TEXT_COLOR_SUCCESS);
        PaintStyle.drawLine(c, "Per hour: " + perHour, y, PaintStyle.TEXT_COLOR_BODY);
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

    public boolean hasMaterials() {
        if (selectedDyeType == null) return false;

        ItemGroupResult inv = getWidgetManager().getInventory().search(
            Set.of(selectedDyeType.getIngredientId(), Constants.COINS_ID)
        );
        if (inv == null) return false;

        if (!inv.contains(Constants.COINS_ID)) return false;
        if (!inv.contains(selectedDyeType.getIngredientId())) return false;

        int ingredientAmount = inv.getAmount(selectedDyeType.getIngredientId());
        int coinAmount = inv.getAmount(Constants.COINS_ID);

        return ingredientAmount >= selectedDyeType.getIngredientCount() &&
            coinAmount >= Constants.COINS_PER_DYE;
    }

    public boolean hasDyes() {
        if (selectedDyeType == null) return false;

        try {
            return InventoryUtils.hasItem(this, selectedDyeType.getDyeId());
        } catch (CannotOpenWidgetException e) {
            log(getClass(), e.getMessage());
            return false;
        }
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
        return AreaUtils.isInArea(pos, Constants.DRAYNOR_BANK_AREA);
    }

    public WorldPosition getRandomBankTile() {
        int x = Constants.DRAYNOR_BANK_AREA.getX() + random.nextInt(Constants.DRAYNOR_BANK_AREA.getWidth());
        int y = Constants.DRAYNOR_BANK_AREA.getY() + random.nextInt(Constants.DRAYNOR_BANK_AREA.getHeight());
        return new WorldPosition(x, y, Constants.DRAYNOR_BANK_AREA.getPlane());
    }

    public boolean isInAggieShop(WorldPosition pos) {
        return AreaUtils.isInAnyArea(pos, Constants.AGGIE_SHOP_AREAS);
    }
}
