package startermod.cards;

import basemod.abstracts.CustomCard;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import startermod.RoxyEnums;
import startermod.StarterMod;
import startermod.actions.SetRandomHandCardCostForTurnAction;

public class RoxyCalmTutor extends CustomCard {
    public static final String ID = StarterMod.makeID("RoxyCalmTutor");

    private static final CardStrings CARD_STRINGS = CardCrawlGame.languagePack.getCardStrings(ID);
    private static final int COST = 1;
    private static final int DRAW = 1;
    private static final int UPGRADE_DRAW = 1;

    public RoxyCalmTutor() {
        super(ID, CARD_STRINGS.NAME, StarterMod.assetPath("images/cards/roxy_calm_tutor.png"), COST, CARD_STRINGS.DESCRIPTION,
            CardType.SKILL, RoxyEnums.ROXY_COLOR, CardRarity.UNCOMMON, CardTarget.SELF);
        this.baseMagicNumber = DRAW;
        this.magicNumber = this.baseMagicNumber;
    }

    @Override
    public void use(AbstractPlayer player, AbstractMonster monster) {
        this.addToBot(new DrawCardAction(player, this.magicNumber));
        this.addToBot(new SetRandomHandCardCostForTurnAction());
    }

    @Override
    public void upgrade() {
        if (!this.upgraded) {
            this.upgradeName();
            this.upgradeMagicNumber(UPGRADE_DRAW);
        }
    }

    @Override
    public RoxyCalmTutor makeCopy() {
        return new RoxyCalmTutor();
    }
}
