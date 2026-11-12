package roxymod.powers;

import com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction;
import com.megacrit.cardcrawl.actions.unique.LoseEnergyAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import roxymod.RoxyMod;
import roxymod.util.PowerIconHelper;

public class ExhaustionPower extends AbstractPower {
    public static final String POWER_ID = RoxyMod.makeID("Exhaustion");
    public static final int MAX_AMOUNT = 2;

    private static final String NAME = "枯竭";

    public ExhaustionPower(AbstractCreature owner, int amount) {
        this.name = NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.amount = Math.min(MAX_AMOUNT, Math.max(0, amount));
        this.type = PowerType.DEBUFF;
        this.isTurnBased = true;

        this.region128 = PowerIconHelper.loadLarge("exhaustion");
        this.region48 = PowerIconHelper.loadSmall("exhaustion");
        this.updateDescription();
    }

    @Override
    public void stackPower(int stackAmount) {
        super.stackPower(stackAmount);
        this.amount = Math.min(MAX_AMOUNT, this.amount);
        this.updateDescription();
    }

    @Override
    public void atStartOfTurn() {
        if (this.amount > 0) {
            this.flash();
            this.addToBot(new LoseEnergyAction(this.amount));
        }
        this.addToBot(new RemoveSpecificPowerAction(this.owner, this.owner, POWER_ID));
    }

    @Override
    public void updateDescription() {
        this.description = "下回合开始时失去 " + this.amount + " 点能量，随后移除。最多 2 层。";
    }
}
