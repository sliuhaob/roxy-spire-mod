package roxymod.powers;

import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import roxymod.RoxyMod;
import roxymod.util.PowerIconHelper;

public class AbsoluteWaterPower extends AbstractPower {
    public static final String POWER_ID = RoxyMod.makeID("AbsoluteWater");

    private static final String NAME = "绝对水域";
    private static final int CHANT = 1;

    public AbsoluteWaterPower(AbstractCreature owner, int amount) {
        this.name = NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.amount = amount;
        this.type = PowerType.BUFF;

        this.region128 = PowerIconHelper.loadLarge("absolute_water");
        this.region48 = PowerIconHelper.loadSmall("absolute_water");
        this.updateDescription();
    }

    @Override
    public float atDamageGive(float damage, DamageInfo.DamageType type, AbstractCard card) {
        if (type == DamageInfo.DamageType.NORMAL && card != null && card.type == AbstractCard.CardType.ATTACK) {
            return damage + this.amount;
        }
        return damage;
    }

    @Override
    public void atStartOfTurn() {
        this.flash();
        this.addToBot(new ApplyPowerAction(this.owner, this.owner, new ChantPower(this.owner, CHANT), CHANT));
    }

    @Override
    public void updateDescription() {
        this.description = "攻击牌造成的伤害增加 #b" + this.amount + " 点。每回合开始时，获得 #b1 层咏唱。";
    }
}
