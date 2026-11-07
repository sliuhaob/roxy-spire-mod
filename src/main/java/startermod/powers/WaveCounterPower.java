package startermod.powers;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import startermod.StarterMod;
import startermod.util.PowerIconHelper;

public class WaveCounterPower extends AbstractPower {
    public static final String POWER_ID = StarterMod.makeID("WaveCounter");

    private static final String NAME = "浪返";

    public WaveCounterPower(AbstractCreature owner, int amount) {
        this.name = NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.amount = amount;
        this.type = PowerType.BUFF;

        this.region128 = PowerIconHelper.loadLarge("wave_counter");
        this.region48 = PowerIconHelper.loadSmall("wave_counter");
        this.updateDescription();
    }

    @Override
    public int onAttacked(DamageInfo info, int damageAmount) {
        if (damageAmount > 0 && this.amount > 0 && info.type == DamageInfo.DamageType.NORMAL
                && info.owner != null && info.owner != this.owner) {
            int counterDamage = this.amount;
            this.amount = 0;
            this.flash();
            this.addToTop(new RemoveSpecificPowerAction(this.owner, this.owner, this));
            this.addToTop(new DamageAction(info.owner,
                new DamageInfo(this.owner, counterDamage, DamageInfo.DamageType.THORNS),
                AbstractGameAction.AttackEffect.BLUNT_LIGHT, true));
        }
        return damageAmount;
    }

    @Override
    public void updateDescription() {
        this.description = "下次受到攻击伤害时，对攻击者造成 #b" + this.amount + " 点伤害，然后移除此能力。";
    }
}
