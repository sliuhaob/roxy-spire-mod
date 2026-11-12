package roxymod.powers;

import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import roxymod.RoxyMod;
import roxymod.relics.RoxyChantConsumedRelic;
import roxymod.util.PowerIconHelper;
import roxymod.util.DevCombatLog;

public class ChantPower extends AbstractPower {
    public static final String POWER_ID = RoxyMod.makeID("Chant");

    private static final String NAME = "\u548f\u5531";
    private static final String DESCRIPTION =
        "\u53ef\u88ab\u6d1b\u742a\u5e0c\u7684\u90e8\u5206\u5361\u724c\u6d88\u8017\uff0c\u4ee5\u83b7\u5f97\u989d\u5916\u6548\u679c\u3002";

    public ChantPower(AbstractCreature owner, int amount) {
        this.name = NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.amount = amount;
        this.type = PowerType.BUFF;

        this.region128 = PowerIconHelper.loadLarge("chant");
        this.region48 = PowerIconHelper.loadSmall("chant");
        this.updateDescription();
    }

    @Override
    public void updateDescription() {
        this.description = DESCRIPTION;
    }

    @Override
    public void onInitialApplication() {
        int chantGained = this.amount;
        int sageTimeBonus = SageTimePower.getBonusChant(this.owner, chantGained);
        if (sageTimeBonus > 0) {
            this.amount += sageTimeBonus;
            chantGained += sageTimeBonus;
            this.updateDescription();
        }
        DevCombatLog.chantGained(this.owner, chantGained, this.amount);
        this.triggerCrystalStaff(chantGained);
    }

    @Override
    public void stackPower(int stackAmount) {
        int chantGained = stackAmount + SageTimePower.getBonusChant(this.owner, stackAmount);
        super.stackPower(chantGained);
        DevCombatLog.chantGained(this.owner, chantGained, this.amount);
        this.triggerCrystalStaff(chantGained);
    }

    @Override
    public void reducePower(int reduceAmount) {
        DevCombatLog.chantConsumed(this.owner, Math.min(this.amount, reduceAmount), this.amount);
        this.triggerChantConsumed(Math.min(this.amount, reduceAmount));
        super.reducePower(reduceAmount);
    }

    private void triggerCrystalStaff(int chantGained) {
        if (chantGained > 0 && this.owner.hasPower(CrystalStaffPower.POWER_ID)) {
            AbstractPower power = this.owner.getPower(CrystalStaffPower.POWER_ID);
            this.addToBot(new GainBlockAction(this.owner, this.owner, chantGained * power.amount));
        }
        if (chantGained > 0 && this.owner.hasPower(CrystalDomainPower.POWER_ID)) {
            ((CrystalDomainPower) this.owner.getPower(CrystalDomainPower.POWER_ID)).onChantGained(chantGained);
        }
    }

    private void triggerChantConsumed(int chantConsumed) {
        if (chantConsumed <= 0) {
            return;
        }

        if (this.owner.hasPower(ManaWeavePower.POWER_ID)) {
            ((ManaWeavePower) this.owner.getPower(ManaWeavePower.POWER_ID)).onChantConsumed(chantConsumed);
        }
        if (this.owner.hasPower(WaterSaintPower.POWER_ID)) {
            ((WaterSaintPower) this.owner.getPower(WaterSaintPower.POWER_ID)).onChantConsumed(chantConsumed);
        }
        if (this.owner.hasPower(CrystalDomainPower.POWER_ID)) {
            ((CrystalDomainPower) this.owner.getPower(CrystalDomainPower.POWER_ID)).onChantConsumed(chantConsumed);
        }
        if (this.owner == AbstractDungeon.player) {
            for (AbstractRelic relic : AbstractDungeon.player.relics) {
                if (relic instanceof RoxyChantConsumedRelic) {
                    ((RoxyChantConsumedRelic) relic).onChantConsumed(chantConsumed);
                }
            }
        }
    }
}

