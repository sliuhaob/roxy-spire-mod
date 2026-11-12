package roxymod.powers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.actions.common.ReducePowerAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.AbstractPower;
import roxymod.RoxyMod;
import roxymod.patches.FreezeIntentPatch;
import roxymod.patches.FreezeMonsterTurnPatch;
import roxymod.util.PowerIconHelper;
import roxymod.util.DevCombatLog;

/**
 * Freeze skips a monster's turn once its stacks reach that monster's counter.
 * The skipped move is not re-rolled: it stays pending and runs on the next turn,
 * so scripted openers (Bellow, Incantation, Activate, Haste) are delayed, not lost.
 */
public class FreezePower extends AbstractPower {
    public static final String POWER_ID = RoxyMod.makeID("Freeze");
    public static final String NAME = "冰冻";
    public static final int STARTING_COUNTER = 3;
    private static final float COUNTER_TEXT_SCALE = 0.8f;
    private static final float COUNTER_TEXT_DROP = 4.0f;

    private final Color countingColor = new Color(0.0f, 1.0f, 0.0f, 1.0f);
    private final Color frozenColor = new Color(0.55f, 0.90f, 1.0f, 1.0f);
    private boolean removed;

    public FreezePower(AbstractCreature owner, int amount) {
        this.name = NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.amount = amount;
        this.type = PowerType.DEBUFF;
        this.isTurnBased = false;

        this.region128 = PowerIconHelper.loadLarge("freeze");
        this.region48 = PowerIconHelper.loadSmall("freeze");
        this.updateDescription();
    }

    /** True when this monster's next turn will be skipped. */
    public static boolean isFrozen(AbstractMonster monster) {
        if (monster == null || monster.isDeadOrEscaped()) {
            return false;
        }
        AbstractPower power = monster.getPower(POWER_ID);
        return power instanceof FreezePower && ((FreezePower) power).shouldSkipTurn();
    }

    public boolean shouldSkipTurn() {
        return !this.removed
            && this.owner instanceof AbstractMonster
            && this.amount >= getCounter((AbstractMonster) this.owner);
    }

    public void onSkipTurn() {
        if (!(this.owner instanceof AbstractMonster)) {
            return;
        }

        AbstractMonster monster = (AbstractMonster) this.owner;
        int counter = getCounter(monster);
        DevCombatLog.freezeSkip(monster, counter);
        this.flash();
        setCounter(monster, counter + 1);
        this.updateDescription();
        // No re-roll: the pending move keeps its intent and runs next turn.
        // The intent display switches back once the reduction below resolves.
        AbstractDungeon.actionManager.addToBottom(new ReducePowerAction(monster, monster, POWER_ID, counter));
    }

    @Override
    public void onInitialApplication() {
        if (this.owner instanceof AbstractMonster) {
            AbstractMonster monster = (AbstractMonster) this.owner;
            DevCombatLog.freezeApplied(monster, this.amount, this.amount, getCounter(monster));
        }
        this.refreshIntent();
    }

    @Override
    public void stackPower(int stackAmount) {
        super.stackPower(stackAmount);
        this.updateDescription();
        if (this.owner instanceof AbstractMonster) {
            AbstractMonster monster = (AbstractMonster) this.owner;
            DevCombatLog.freezeApplied(monster, stackAmount, this.amount, getCounter(monster));
        }
        this.refreshIntent();
    }

    @Override
    public void reducePower(int reduceAmount) {
        super.reducePower(reduceAmount);
        this.updateDescription();
        this.refreshIntent();
    }

    @Override
    public void onRemove() {
        // Vanilla calls onRemove before taking the power off the monster.
        this.removed = true;
        this.refreshIntent();
    }

    private void refreshIntent() {
        if (this.owner instanceof AbstractMonster) {
            FreezeIntentPatch.refresh((AbstractMonster) this.owner);
        }
    }

    @Override
    public void renderAmount(SpriteBatch sb, float x, float y, Color c) {
        if (this.amount <= 0 || !(this.owner instanceof AbstractMonster)) {
            super.renderAmount(sb, x, y, c);
            return;
        }
        Color color = this.shouldSkipTurn() ? this.frozenColor : this.countingColor;
        color.a = c.a;
        String text = this.amount + "/" + getCounter((AbstractMonster) this.owner);
        // Three characters instead of one or two: draw smaller and a little lower so the
        // text keeps the vanilla bottom-right position and covers less of the icon.
        FontHelper.renderFontRightTopAligned(sb, FontHelper.powerAmountFont, text, x,
            y - COUNTER_TEXT_DROP * Settings.scale, this.fontScale * COUNTER_TEXT_SCALE, color);
    }

    @Override
    public void updateDescription() {
        int counter = this.owner instanceof AbstractMonster ? getCounter((AbstractMonster) this.owner) : STARTING_COUNTER;
        String state = this.amount >= counter
            ? "已达到计数器，下个回合将被跳过，原定行动顺延。"
            : "再获得 #b" + (counter - this.amount) + " 层将跳过下个回合。";
        this.description = "冰冻层数达到计数器时，跳过该怪物的回合，原定行动顺延到之后的回合。"
            + "触发后减少等同于计数器的冰冻层数，计数器增加 #b1 。"
            + "当前：#b" + this.amount + " / #b" + counter + " 。" + state;
    }

    public static int getCounter(AbstractMonster monster) {
        Integer counter = FreezeMonsterTurnPatch.FreezeCounterField.counter.get(monster);
        return counter == null ? STARTING_COUNTER : counter;
    }

    public static void setCounter(AbstractMonster monster, int counter) {
        FreezeMonsterTurnPatch.FreezeCounterField.counter.set(monster, Math.max(STARTING_COUNTER, counter));
    }
}
