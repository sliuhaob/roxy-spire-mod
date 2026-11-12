package roxymod.powers;

import com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import roxymod.RoxyMod;
import roxymod.util.PowerIconHelper;

public class ContinuationPower extends AbstractPower {
    public static final String POWER_ID = RoxyMod.makeID("Continuation");

    private static final String NAME = "接续";
    private static final String DESCRIPTION = "下一张结式牌会消耗接续并触发额外效果。回合结束时移除。";

    public ContinuationPower(AbstractCreature owner) {
        this.name = NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.amount = 1;
        this.type = PowerType.BUFF;

        this.region128 = PowerIconHelper.loadLarge("continuation");
        this.region48 = PowerIconHelper.loadSmall("continuation");
        this.updateDescription();
    }

    @Override
    public void stackPower(int stackAmount) {
        super.stackPower(stackAmount);
        this.amount = 1;
        this.updateDescription();
    }

    @Override
    public void atEndOfTurn(boolean isPlayer) {
        if (isPlayer) {
            this.addToBot(new RemoveSpecificPowerAction(this.owner, this.owner, POWER_ID));
        }
    }

    @Override
    public void updateDescription() {
        this.description = DESCRIPTION;
    }
}
