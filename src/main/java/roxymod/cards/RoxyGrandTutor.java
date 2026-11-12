package roxymod.cards;

import basemod.abstracts.CustomCard;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import roxymod.RoxyEnums;
import roxymod.RoxyMod;

public class RoxyGrandTutor extends CustomCard {
    public static final String ID = RoxyMod.makeID("RoxyGrandTutor");

    private static final CardStrings CARD_STRINGS = CardCrawlGame.languagePack.getCardStrings(ID);
    private static final int COST = 2;
    private static final int DRAW = 2;

    public RoxyGrandTutor() {
        super(ID, CARD_STRINGS.NAME, RoxyMod.assetPath("images/cards/roxy_grand_tutor.png"), COST, CARD_STRINGS.DESCRIPTION,
            CardType.SKILL, RoxyEnums.ROXY_COLOR, CardRarity.RARE, CardTarget.SELF);
        this.baseMagicNumber = DRAW;
        this.magicNumber = this.baseMagicNumber;
        this.exhaust = true;
    }

    @Override
    public void use(AbstractPlayer player, AbstractMonster monster) {
        for (AbstractCard card : player.hand.group) {
            if (card.canUpgrade()) {
                card.upgrade();
                card.applyPowers();
            }
        }
        player.hand.refreshHandLayout();
        this.addToBot(new DrawCardAction(player, this.magicNumber));
    }

    @Override
    public void upgrade() {
        if (!this.upgraded) {
            this.upgradeName();
            this.upgradeBaseCost(1);
        }
    }

    @Override
    public RoxyGrandTutor makeCopy() {
        return new RoxyGrandTutor();
    }
}
