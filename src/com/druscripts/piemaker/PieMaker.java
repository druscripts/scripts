package com.druscripts.piemaker;

import com.druscripts.piemaker.data.Constants;
import com.druscripts.piemaker.data.PieType;
import com.druscripts.piemaker.data.Stage;
import com.druscripts.piemaker.tasks.*;
import com.druscripts.piemaker.tasks.processing.*;
import com.druscripts.utils.paint.PaintStyle;
import com.druscripts.utils.script.FreeScript;

import com.osmb.api.script.ScriptDefinition;
import com.osmb.api.script.SkillCategory;

@ScriptDefinition(
    name = "PieMaker.druscripts.com",
    description = "All-in-one Lumbridge pie making - from flour to cooked pies",
    skillCategory = SkillCategory.COOKING,
    version = 0.1,
    author = "dru"
)
public class PieMaker extends FreeScript {

    public Stage stage = Stage.SETUP;
    public String task = "Starting...";
    public boolean allInOne = true;
    public long startTime = System.currentTimeMillis();
    public int itemsMade = 0;

    public int detectedRegion = -1;
    public int waterSourceId = Constants.JUG_OF_WATER;
    public PieType pieType = PieType.REDBERRY;

    public int bankFlour = 0;
    public int bankWater = 0;
    public int bankPastryDough = 0;
    public int bankPieDishes = 0;
    public int bankPieShells = 0;
    public int bankIngredients = 0;
    public int bankUncookedPies = 0;

    public PieMaker(Object scriptCore) {
        super(scriptCore);
    }

    @Override
    public int[] regionsToPrioritise() {
        return new int[] { Constants.LUMBRIDGE_REGION, Constants.GRAND_EXCHANGE_REGION };
    }

    @Override
    public void onStart() {
        super.onStart();
        log(getClass().getSimpleName(), "Starting PieMaker v" + getVersion());

        startTime = System.currentTimeMillis();
        tasks.add(new SetupTask(this));
    }

    @Override
    public boolean promptBankTabDialogue() {
        return true;
    }

    private final int WIDTH = 200;
    private final int NUM_LINES = 14;

    @Override
    public void onPaint(com.osmb.api.visual.drawing.Canvas c) {
        long elapsed = System.currentTimeMillis() - startTime;
        String runtime = formatRuntime(elapsed);

        double hours = Math.max(1.0E-9, (double) elapsed / 3600000.0);
        int perHour = (int) Math.round((double) itemsMade / hours);

        String modeName = allInOne ? "All-in-one" : "Step-by-step";

        PaintStyle.drawBackground(c, WIDTH, NUM_LINES);
        int y = PaintStyle.drawTitle(c, "PieMaker v" + getVersion());
        y = PaintStyle.drawLine(c, "Mode: " + modeName, y, PaintStyle.TEXT_COLOR_BRAND);
        y = PaintStyle.drawLine(c, "Stage: " + stage.getString(), y, PaintStyle.TEXT_COLOR_TASK);
        y = PaintStyle.drawLine(c, "Runtime: " + runtime, y, PaintStyle.TEXT_COLOR_BODY);
        y = PaintStyle.drawLine(c, "Items made: " + itemsMade, y, PaintStyle.TEXT_COLOR_SUCCESS);
        y = PaintStyle.drawLine(c, "Per hour: " + perHour, y, PaintStyle.TEXT_COLOR_BODY);

        y = PaintStyle.drawLine(c, "Bank Materials:", y, PaintStyle.TEXT_COLOR_TITLE);
        y = PaintStyle.drawLine(c, "  Flour: " + bankFlour, y, PaintStyle.TEXT_COLOR_MUTED);
        y = PaintStyle.drawLine(c, "  Water: " + bankWater, y, PaintStyle.TEXT_COLOR_MUTED);
        y = PaintStyle.drawLine(c, "  Pastry Dough: " + bankPastryDough, y, PaintStyle.TEXT_COLOR_MUTED);
        y = PaintStyle.drawLine(c, "  Pie Dishes: " + bankPieDishes, y, PaintStyle.TEXT_COLOR_MUTED);
        y = PaintStyle.drawLine(c, "  Pie Shells: " + bankPieShells, y, PaintStyle.TEXT_COLOR_MUTED);
        y = PaintStyle.drawLine(c, "  " + pieType.getIngredientName() + ": " + bankIngredients, y, PaintStyle.TEXT_COLOR_MUTED);
        PaintStyle.drawLine(c, "  Uncooked Pies: " + bankUncookedPies, y, PaintStyle.TEXT_COLOR_MUTED);
    }

    public void initializeProductionTasks() {
        tasks.clear();

        if (allInOne) {
            if (stage.ordinal() <= Stage.MAKE_DOUGH.ordinal()) {
                tasks.add(new MakePastryDoughTask(this, waterSourceId));
            }
            if (stage.ordinal() <= Stage.MAKE_SHELL.ordinal()) {
                tasks.add(new MakePieShellsTask(this));
            }
            if (stage.ordinal() <= Stage.MAKE_UNCOOKED.ordinal()) {
                tasks.add(new MakeUncookedPieTask(this));
            }
            if (stage.ordinal() <= Stage.COOK.ordinal() && isLumbridge()) {
                tasks.add(new TravelToRangeTask(this));
                tasks.add(new CookPiesTask(this));
                tasks.add(new TravelToBankTask(this));
            }
        } else {
            switch (stage) {
                case MAKE_DOUGH:
                    tasks.add(new MakePastryDoughTask(this, waterSourceId));
                    break;
                case MAKE_SHELL:
                    tasks.add(new MakePieShellsTask(this));
                    break;
                case MAKE_UNCOOKED:
                    tasks.add(new MakeUncookedPieTask(this));
                    break;
                case COOK:
                    if (isLumbridge()) {
                        tasks.add(new TravelToRangeTask(this));
                        tasks.add(new CookPiesTask(this));
                        tasks.add(new TravelToBankTask(this));
                    }
                    break;
            }
        }

        tasks.add(new BankAndManageStageTask(this));
        log(getClass().getSimpleName(), "Production tasks initialized");
    }

    private String formatRuntime(long millis) {
        long seconds = millis / 1000L;
        long hours = seconds / 3600L;
        long minutes = seconds % 3600L / 60L;
        long secs = seconds % 60L;
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }

    public boolean isLumbridge() {
        return detectedRegion == Constants.LUMBRIDGE_REGION;
    }

    public void increaseItemsMade(int count) {
        itemsMade += count;
    }
}
