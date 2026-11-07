package startermod.cards;

import basemod.abstracts.CustomCard;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.ReduceCostForTurnAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import startermod.RoxyEnums;
import startermod.StarterMod;
import startermod.powers.ChantPower;

public class RoxyRapidChant extends CustomCard {
    public static final String ID = StarterMod.makeID("RoxyRapidChant");

    private static final CardStrings CARD_STRINGS = CardCrawlGame.languagePack.getCardStrings(ID);
    private static final int COST = 0;
    private static final int CHANT = 3;

    public RoxyRapidChant() {
        super(ID, CARD_STRINGS.NAME, StarterMod.assetPath("images/cards/roxy_rapid_chant.png"), COST, CARD_STRINGS.DESCRIPTION,
            CardType.SKILL, RoxyEnums.ROXY_COLOR, CardRarity.UNCOMMON, CardTarget.SELF);
        this.baseMagicNumber = CHANT;
        this.magicNumber = this.baseMagicNumber;
        this.exhaust = true;
    }

    @Override
    public void use(AbstractPlayer player, AbstractMonster monster) {
        this.addToBot(new ApplyPowerAction(player, player, new ChantPower(player, this.magicNumber), this.magicNumber));
        for (AbstractCard card : player.hand.group) {
            if (card.type == CardType.ATTACK && card.costForTurn > 0) {
                this.addToBot(new ReduceCostForTurnAction(card, 1));
            }
        }
    }

    @Override
    public void upgrade() {
        if (!this.upgraded) {
            this.upgradeName();
            this.upgradeMagicNumber(1);
        }
    }

    @Override
    public RoxyRapidChant makeCopy() {
        return new RoxyRapidChant();
    }
}
