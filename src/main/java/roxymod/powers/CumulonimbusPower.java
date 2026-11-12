package roxymod.powers;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.AbstractPower;
import roxymod.RoxyMod;
import roxymod.util.PowerIconHelper;

public class CumulonimbusPower extends AbstractPower {
    public static final String POWER_ID = RoxyMod.makeID("Cumulonimbus");

    private static final String NAME = "豪雷积层云";
    private static final int WET = 1;

    public CumulonimbusPower(AbstractCreature owner, int amount) {
        this.name = NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.amount = amount;
        this.type = PowerType.BUFF;

        this.region128 = PowerIconHelper.loadLarge("cumulonimbus");
        this.region48 = PowerIconHelper.loadSmall("cumulonimbus");
        this.updateDescription();
    }

    @Override
    public void atStartOfTurn() {
        AbstractMonster target = AbstractDungeon.getMonsters().getRandomMonster(
            null,
            true,
            AbstractDungeon.cardRandomRng
        );
        if (target == null) {
            return;
        }

        this.flash();
        this.addToBot(new DamageAction(
            target,
            new DamageInfo(this.owner, this.amount, DamageInfo.DamageType.THORNS),
            AbstractGameAction.AttackEffect.LIGHTNING
        ));
        this.addToBot(new ApplyPowerAction(
            target,
            this.owner,
            new WetPower(target, WET),
            WET
        ));
    }

    @Override
    public void updateDescription() {
        this.description = "每回合开始时，对随机敌人造成 #b" + this.amount
            + " 点伤害并给予 #b1 层湿润。";
    }
}
