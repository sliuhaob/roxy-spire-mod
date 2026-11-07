package startermod.powers;

import com.megacrit.cardcrawl.actions.common.ReducePowerAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import startermod.StarterMod;
import startermod.util.PowerIconHelper;

public class WetPower extends AbstractPower {
    public static final String POWER_ID = StarterMod.makeID("Wet");
    public static final int MAX_AMOUNT = 3;

    private static final String NAME = "湿润";

    public WetPower(AbstractCreature owner, int amount) {
        this.name = NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.amount = Math.min(MAX_AMOUNT, Math.max(0, amount));
        this.type = PowerType.DEBUFF;
        this.isTurnBased = true;

        this.region128 = PowerIconHelper.loadLarge("wet");
        this.region48 = PowerIconHelper.loadSmall("wet");
        this.updateDescription();
    }

    @Override
    public void stackPower(int stackAmount) {
        super.stackPower(stackAmount);
        this.amount = Math.min(MAX_AMOUNT, this.amount);
        this.updateDescription();
    }

    @Override
    public void reducePower(int reduceAmount) {
        super.reducePower(reduceAmount);
        this.updateDescription();
    }

    @Override
    public void atEndOfRound() {
        if (this.amount > 0) {
            this.addToBot(new ReducePowerAction(this.owner, this.owner, POWER_ID, 1));
        }
    }

    @Override
    public void updateDescription() {
        this.description = "受到冰冻时，消耗 1 层湿润并额外获得 1 层冰冻。"
            + "雷光会消耗全部湿润，每层额外造成 5 点伤害。"
            + "敌人回合结束时减少 1 层。最多 3 层。";
    }
}
