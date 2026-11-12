package roxymod.powers;

import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import roxymod.RoxyMod;
import roxymod.util.PowerIconHelper;

public class CrystalDomainPower extends AbstractPower {
    public static final String POWER_ID = RoxyMod.makeID("CrystalDomain");

    private static final String NAME = "结晶领域";
    private boolean triggeredThisTurn;

    @Override
    public void atStartOfTurn() {
        this.triggeredThisTurn = false;
    }

    public CrystalDomainPower(AbstractCreature owner, int amount) {
        this.name = NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.amount = amount;
        this.type = PowerType.BUFF;

        this.region128 = PowerIconHelper.loadLarge("crystal_domain");
        this.region48 = PowerIconHelper.loadSmall("crystal_domain");
        this.updateDescription();
    }

    public void onChantGained(int chantGained) {
        if (chantGained > 0) {
            this.flash();
            this.addToBot(new GainBlockAction(this.owner, this.owner, chantGained * this.amount));
        }
    }

    public void onChantConsumed(int chantConsumed) {
        if (chantConsumed > 0 && !this.triggeredThisTurn) {
            this.triggeredThisTurn = true;
            this.flash();
            this.addToBot(new DrawCardAction(this.owner, this.amount));
        }
    }

    @Override
    public void updateDescription() {
        this.description = "每获得 1 层咏唱，获得 #b" + this.amount
            + " 点格挡。每回合第一次消耗咏唱时，抽 #b" + this.amount + " 张牌。";
    }
}
