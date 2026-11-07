package startermod.powers;

import com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardQueueItem;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.AbstractPower;
import startermod.StarterMod;
import startermod.cards.interfaces.FinisherCard;

public class EchoLessonPower extends AbstractPower {
    public static final String POWER_ID = StarterMod.makeID("EchoLesson");
    private static final String NAME = "回声授课";

    public EchoLessonPower(AbstractCreature owner, int amount) {
        this.name = NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.amount = amount;
        this.type = PowerType.BUFF;
        this.loadRegion("doubleTap");
        this.updateDescription();
    }

    @Override
    public void onUseCard(AbstractCard card, UseCardAction action) {
        if (card.purgeOnUse || this.amount <= 0 || !isEligible(card)) {
            return;
        }

        this.flash();
        AbstractMonster target = action.target instanceof AbstractMonster
                ? (AbstractMonster) action.target
                : null;
        AbstractCard copy = card.makeSameInstanceOf();
        AbstractPlayer player = AbstractDungeon.player;
        player.limbo.addToBottom(copy);
        copy.current_x = card.current_x;
        copy.current_y = card.current_y;
        copy.target_x = (float) Settings.WIDTH / 2.0F - 300.0F * Settings.scale;
        copy.target_y = (float) Settings.HEIGHT / 2.0F;
        if (target != null) {
            copy.calculateCardDamage(target);
        }
        copy.purgeOnUse = true;
        AbstractDungeon.actionManager.addCardQueueItem(
                new CardQueueItem(copy, target, card.energyOnUse, true, true), true);

        this.amount--;
        if (this.amount == 0) {
            this.addToBot(new RemoveSpecificPowerAction(this.owner, this.owner, POWER_ID));
        } else {
            this.updateDescription();
        }
    }

    @Override
    public void atEndOfTurn(boolean isPlayer) {
        if (isPlayer) {
            this.addToBot(new RemoveSpecificPowerAction(this.owner, this.owner, POWER_ID));
        }
    }

    @Override
    public void updateDescription() {
        if (this.amount == 1) {
            this.description = "本回合下一张攻击牌或结式牌打出两次。";
        } else {
            this.description = "本回合接下来 " + this.amount + " 张攻击牌或结式牌打出两次。";
        }
    }

    private static boolean isEligible(AbstractCard card) {
        return card.type == AbstractCard.CardType.ATTACK || card instanceof FinisherCard;
    }
}
