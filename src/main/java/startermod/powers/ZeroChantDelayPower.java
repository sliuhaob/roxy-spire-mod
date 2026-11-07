package startermod.powers;

import com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import startermod.StarterMod;
import startermod.util.PowerIconHelper;

public class ZeroChantDelayPower extends AbstractPower {
    public static final String POWER_ID = StarterMod.makeID("ZeroChantDelay");

    private static final String NAME = "零式咏唱";

    public ZeroChantDelayPower(AbstractCreature owner) {
        this.name = NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.amount = -1;
        this.type = PowerType.DEBUFF;

        this.region128 = PowerIconHelper.loadLarge("silent_chant");
        this.region48 = PowerIconHelper.loadSmall("silent_chant");
        this.updateDescription();
    }

    @Override
    public void atStartOfTurn() {
        this.flash();
        if (this.owner.hasPower(ChantPower.POWER_ID)) {
            this.addToBot(new RemoveSpecificPowerAction(this.owner, this.owner, ChantPower.POWER_ID));
        }
        this.addToBot(new RemoveSpecificPowerAction(this.owner, this.owner, this));
    }

    @Override
    public void updateDescription() {
        this.description = "下回合开始时，失去所有咏唱。";
    }
}
