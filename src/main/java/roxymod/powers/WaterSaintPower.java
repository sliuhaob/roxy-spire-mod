package roxymod.powers;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageRandomEnemyAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import roxymod.RoxyMod;
import roxymod.util.PowerIconHelper;

public class WaterSaintPower extends AbstractPower {
    public static final String POWER_ID = RoxyMod.makeID("WaterSaint");

    private static final String NAME = "水圣称号";

    public WaterSaintPower(AbstractCreature owner, int amount) {
        this.name = NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.amount = amount;
        this.type = PowerType.BUFF;

        this.region128 = PowerIconHelper.loadLarge("water_saint");
        this.region48 = PowerIconHelper.loadSmall("water_saint");
        this.updateDescription();
    }

    public void onChantConsumed(int chantConsumed) {
        this.flash();
        this.addToBot(new DamageRandomEnemyAction(new DamageInfo(this.owner, this.amount, DamageInfo.DamageType.THORNS),
            AbstractGameAction.AttackEffect.BLUNT_LIGHT));
    }

    @Override
    public void updateDescription() {
        this.description = "每当你消耗咏唱，随机敌人受到 #b" + this.amount + " 点伤害。";
    }
}
