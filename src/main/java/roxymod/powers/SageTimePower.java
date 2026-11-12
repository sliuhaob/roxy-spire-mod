package roxymod.powers;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.ReducePowerAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import roxymod.RoxyMod;
import roxymod.util.PowerIconHelper;

public class SageTimePower extends AbstractPower {
    public static final String POWER_ID = RoxyMod.makeID("SageTime");

    private static final String NAME = "贤者时间";

    private boolean addingBonusBlock;

    public SageTimePower(AbstractCreature owner, int amount) {
        this.name = NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.amount = amount;
        this.type = PowerType.BUFF;

        this.region128 = PowerIconHelper.loadLarge("sage_time");
        this.region48 = PowerIconHelper.loadSmall("sage_time");
        this.updateDescription();
    }

    @Override
    public void atStartOfTurn() {
        if (this.amount > 0) {
            this.flash();
            this.addToBot(new ReducePowerAction(this.owner, this.owner, POWER_ID, 1));
        }
    }

    @Override
    public void onGainedBlock(float blockAmount) {
        if (blockAmount > 0.0F && this.amount > 0 && !this.addingBonusBlock) {
            this.flash();
            this.addingBonusBlock = true;
            this.owner.addBlock(this.amount);
            this.addingBonusBlock = false;
        }
    }

    @Override
    public void onAttack(DamageInfo info, int damageAmount, AbstractCreature target) {
        if (damageAmount > 0
            && this.amount > 0
            && info.owner == this.owner
            && info.type == DamageInfo.DamageType.NORMAL
            && target != null
            && target != this.owner
            && !target.isDying
            && !target.isDead) {
            this.flash();
            this.addToBot(new DamageAction(
                target,
                new DamageInfo(this.owner, this.amount, DamageInfo.DamageType.THORNS),
                AbstractGameAction.AttackEffect.BLUNT_LIGHT,
                true
            ));
        }
    }

    public static int getBonusChant(AbstractCreature owner, int chantGained) {
        if (owner != null && chantGained > 0 && owner.hasPower(POWER_ID)) {
            AbstractPower power = owner.getPower(POWER_ID);
            power.flash();
            return Math.max(0, power.amount);
        }
        return 0;
    }

    @Override
    public void updateDescription() {
        this.description = "每当你获得格挡，额外获得 #b" + this.amount
            + " 点格挡。每当你造成攻击伤害，额外造成 #b" + this.amount
            + " 点伤害。每当你获得咏唱，额外获得 #b" + this.amount
            + " 层咏唱。每回合开始时减少 1 层。";
    }
}
