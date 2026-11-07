package startermod.powers;

import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import startermod.StarterMod;
import startermod.util.PowerIconHelper;

public class StartTurnChantPower extends AbstractPower {
    public static final String POWER_ID = StarterMod.makeID("StartTurnChant");

    private static final String NAME = "\u9b54\u529b\u5faa\u73af";

    public StartTurnChantPower(AbstractCreature owner, int amount) {
        this.name = NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.amount = amount;
        this.type = PowerType.BUFF;

        this.region128 = PowerIconHelper.loadLarge("magic_cycle");
        this.region48 = PowerIconHelper.loadSmall("magic_cycle");
        this.updateDescription();
    }

    @Override
    public void atStartOfTurn() {
        this.flash();
        this.addToBot(new ApplyPowerAction(this.owner, this.owner, new ChantPower(this.owner, this.amount), this.amount));
    }

    @Override
    public void updateDescription() {
        this.description = "\u6bcf\u56de\u5408\u5f00\u59cb\u65f6\uff0c\u83b7\u5f97 #b" + this.amount
            + " \u5c42\u548f\u5531\u3002";
    }
}

