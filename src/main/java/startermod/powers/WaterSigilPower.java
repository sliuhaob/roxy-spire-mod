package startermod.powers;

import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import startermod.StarterMod;
import startermod.util.PowerIconHelper;

public class WaterSigilPower extends AbstractPower {
    public static final String POWER_ID = StarterMod.makeID("WaterSigil");

    private static final String NAME = "水之术印";

    public WaterSigilPower(AbstractCreature owner, int amount) {
        this.name = NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.amount = amount;
        this.type = PowerType.BUFF;

        this.region128 = PowerIconHelper.loadLarge("water_sigil");
        this.region48 = PowerIconHelper.loadSmall("water_sigil");
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
    public void updateDescription() {
        this.description = "攻击牌造成的伤害增加 #b" + this.amount + " 点。";
    }
}
