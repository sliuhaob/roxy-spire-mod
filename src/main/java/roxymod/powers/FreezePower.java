package roxymod.powers;

import com.megacrit.cardcrawl.actions.common.ReducePowerAction;
import com.megacrit.cardcrawl.actions.common.RollMoveAction;
import com.megacrit.cardcrawl.actions.utility.TextAboveCreatureAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.AbstractPower;
import roxymod.RoxyMod;
import roxymod.patches.FreezeMonsterTurnPatch;
import roxymod.util.PowerIconHelper;

public class FreezePower extends AbstractPower {
    public static final String POWER_ID = RoxyMod.makeID("Freeze");

    private static final String NAME = "冰冻";

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

    public boolean shouldSkipTurn() {
        return this.owner instanceof AbstractMonster && this.amount >= getCounter((AbstractMonster) this.owner);
    }

    public void onSkipTurn() {
        if (!(this.owner instanceof AbstractMonster)) {
            return;
        }

        AbstractMonster monster = (AbstractMonster) this.owner;
        int counter = getCounter(monster);
        this.flash();
        setCounter(monster, counter + 1);
        this.updateDescription();
        AbstractDungeon.actionManager.addToBottom(new TextAboveCreatureAction(monster, "冰冻"));
        AbstractDungeon.actionManager.addToBottom(new ReducePowerAction(monster, monster, POWER_ID, counter));
        AbstractDungeon.actionManager.addToBottom(new RollMoveAction(monster));
    }

    @Override
    public void stackPower(int stackAmount) {
        super.stackPower(stackAmount);
        this.updateDescription();
    }

    @Override
    public void reducePower(int reduceAmount) {
        super.reducePower(reduceAmount);
        this.updateDescription();
    }

    @Override
    public void updateDescription() {
        int counter = this.owner instanceof AbstractMonster ? getCounter((AbstractMonster) this.owner) : 3;
        this.description = "该怪物的冰冻层数大于等于计数器时，跳过它的回合。"
            + "触发后减少等同于当前计数器的冰冻层数，计数器增加 #b1 。"
            + "当前计数器：#b" + counter + " 。";
    }

    public static int getCounter(AbstractMonster monster) {
        return FreezeMonsterTurnPatch.FreezeCounterField.counter.get(monster);
    }

    public static void setCounter(AbstractMonster monster, int counter) {
        FreezeMonsterTurnPatch.FreezeCounterField.counter.set(monster, Math.max(3, counter));
    }
}
