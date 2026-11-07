package startermod.cards;

import basemod.abstracts.CustomCard;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import startermod.RoxyEnums;
import startermod.StarterMod;
import startermod.actions.UpgradeRandomHandCardAction;
import startermod.util.ReviewHelper;

public class RoxySpellCorrection extends CustomCard {
    public static final String ID = StarterMod.makeID("RoxySpellCorrection");

    private static final CardStrings CARD_STRINGS = CardCrawlGame.languagePack.getCardStrings(ID);
    private static final int COST = 1;
    private static final int DRAW = 1;

    public RoxySpellCorrection() {
        super(ID, CARD_STRINGS.NAME, StarterMod.assetPath("images/cards/roxy_spell_correction.png"), COST, CARD_STRINGS.DESCRIPTION,
            CardType.SKILL, RoxyEnums.ROXY_COLOR, CardRarity.UNCOMMON, CardTarget.SELF);
        this.baseMagicNumber = DRAW;
        this.magicNumber = this.baseMagicNumber;
    }

    @Override
    public void use(AbstractPlayer player, AbstractMonster monster) {
        int cardsToUpgrade = ReviewHelper.isReview(this) ? 2 : 1;
        this.addToBot(new UpgradeRandomHandCardAction(cardsToUpgrade));
        this.addToBot(new DrawCardAction(player, this.magicNumber));
    }

    @Override
    public void upgrade() {
        if (!this.upgraded) {
            this.upgradeName();
            this.upgradeBaseCost(0);
        }
    }

    @Override
    public RoxySpellCorrection makeCopy() {
        return new RoxySpellCorrection();
    }
}
