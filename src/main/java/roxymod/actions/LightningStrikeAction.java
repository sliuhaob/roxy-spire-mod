package roxymod.actions;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import roxymod.powers.WetPower;

public class LightningStrikeAction extends AbstractGameAction {
    private final AbstractMonster monster;
    private final AbstractCreature source;
    private final AbstractCard damageCard;

    public LightningStrikeAction(
        AbstractMonster monster,
        AbstractCreature source,
        AbstractCard damageCard
    ) {
        this.monster = monster;
        this.source = source;
        this.damageCard = damageCard;
        this.actionType = ActionType.DAMAGE;
    }

    @Override
    public void update() {
        if (this.monster == null || this.monster.isDeadOrEscaped()) {
            this.isDone = true;
            return;
        }

        int wet = this.monster.hasPower(WetPower.POWER_ID)
            ? Math.max(0, this.monster.getPower(WetPower.POWER_ID).amount)
            : 0;
        // Recalculate from base damage while Wet is present. Strength, Weak,
        // Vulnerable and Intangible must also apply to the Wet contribution.
        this.damageCard.calculateCardDamage(this.monster);

        this.addToTop(new DamageAction(
            this.monster,
            new DamageInfo(this.source, this.damageCard.damage, this.damageCard.damageTypeForTurn),
            AttackEffect.LIGHTNING
        ));
        if (wet > 0) {
            this.monster.getPower(WetPower.POWER_ID).flash();
            this.addToTop(new RemoveSpecificPowerAction(
                this.monster,
                this.source,
                WetPower.POWER_ID
            ));
        }
        this.isDone = true;
    }
}
