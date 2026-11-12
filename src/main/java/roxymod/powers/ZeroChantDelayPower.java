package roxymod.powers;

import com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import roxymod.RoxyMod;
import roxymod.util.PowerIconHelper;
import roxymod.util.DevCombatLog;

public class ZeroChantDelayPower extends AbstractPower {
    public static final String POWER_ID = RoxyMod.makeID("ZeroChantDelay");

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
            DevCombatLog.chantLost(this.owner, this.owner.getPower(ChantPower.POWER_ID).amount, "zero_chant");
            this.addToBot(new RemoveSpecificPowerAction(this.owner, this.owner, ChantPower.POWER_ID));
        }
        this.addToBot(new RemoveSpecificPowerAction(this.owner, this.owner, this));
    }

    @Override
    public void updateDescription() {
        this.description = "下回合开始时，失去所有咏唱。";
    }
}
