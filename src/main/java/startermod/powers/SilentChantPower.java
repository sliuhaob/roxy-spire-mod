package startermod.powers;

import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import startermod.StarterMod;
import startermod.util.PowerIconHelper;

public class SilentChantPower extends AbstractPower {
    public static final String POWER_ID = StarterMod.makeID("SilentChant");

    private static final String NAME = "无声咏唱";

    public SilentChantPower(AbstractCreature owner, int amount) {
        this.name = NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.amount = amount;
        this.type = PowerType.BUFF;

        this.region128 = PowerIconHelper.loadLarge("silent_chant");
        this.region48 = PowerIconHelper.loadSmall("silent_chant");
        this.updateDescription();
    }

    @Override
    public void onUseCard(AbstractCard card, UseCardAction action) {
        if (card.type == AbstractCard.CardType.SKILL) {
            this.flash();
            this.addToBot(new ApplyPowerAction(this.owner, this.owner, new ChantPower(this.owner, this.amount), this.amount));
        }
    }

    @Override
    public void atEndOfTurn(boolean isPlayer) {
        this.addToBot(new RemoveSpecificPowerAction(this.owner, this.owner, this));
    }

    @Override
    public void updateDescription() {
        this.description = "本回合每打出 1 张技能牌，获得 #b" + this.amount + " 层咏唱。";
    }
}
