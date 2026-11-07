package startermod.cards;

import basemod.abstracts.CustomCard;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import startermod.actions.ConsumeChantAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import startermod.RoxyEnums;
import startermod.StarterMod;
import startermod.cards.interfaces.FinisherCard;
import startermod.util.ChantHelper;
import startermod.util.ContinuationHelper;

public class RoxySaintWaterLance extends CustomCard implements FinisherCard {
    public static final String ID = StarterMod.makeID("RoxySaintWaterLance");

    private static final CardStrings CARD_STRINGS = CardCrawlGame.languagePack.getCardStrings(ID);
    private static final int COST = 2;
    private static final int DAMAGE = 12;
    private static final int UPGRADE_DAMAGE = 4;

    public RoxySaintWaterLance() {
        super(ID, CARD_STRINGS.NAME, StarterMod.assetPath("images/cards/roxy_saint_water_lance.png"), COST, CARD_STRINGS.DESCRIPTION,
            CardType.ATTACK, RoxyEnums.ROXY_COLOR, CardRarity.UNCOMMON, CardTarget.ENEMY);
        this.baseDamage = DAMAGE;
    }

    @Override
    public void use(AbstractPlayer player, AbstractMonster monster) {
        boolean continued = ContinuationHelper.consume(player);
        this.addToBot(new DamageAction(monster, new DamageInfo(player, this.damage, this.damageTypeForTurn),
            AbstractGameAction.AttackEffect.BLUNT_HEAVY));
        if (continued || ChantHelper.amount(player) > 0) {
            if (!continued) {
                this.addToBot(new ConsumeChantAction(player, 1));
            }
            this.addToBot(new DamageAction(monster, new DamageInfo(player, this.damage, this.damageTypeForTurn),
                AbstractGameAction.AttackEffect.LIGHTNING));
        }
    }

    @Override
    public void upgrade() {
        if (!this.upgraded) {
            this.upgradeName();
            this.upgradeDamage(UPGRADE_DAMAGE);
        }
    }

    @Override
    public RoxySaintWaterLance makeCopy() {
        return new RoxySaintWaterLance();
    }
}
