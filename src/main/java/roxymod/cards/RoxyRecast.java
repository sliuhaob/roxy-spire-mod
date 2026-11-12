package roxymod.cards;

import basemod.abstracts.CustomCard;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import roxymod.RoxyEnums;
import roxymod.RoxyMod;
import roxymod.actions.MoveRandomAttackFromDiscardPileToHandAction;
import roxymod.powers.ChantPower;
import roxymod.util.ReviewHelper;

public class RoxyRecast extends CustomCard {
    public static final String ID = RoxyMod.makeID("RoxyRecast");

    private static final CardStrings CARD_STRINGS = CardCrawlGame.languagePack.getCardStrings(ID);
    private static final int COST = 1;
    private static final int CHANT = 1;

    public RoxyRecast() {
        super(ID, CARD_STRINGS.NAME, RoxyMod.assetPath("images/cards/roxy_recast.png"), COST, CARD_STRINGS.DESCRIPTION,
            CardType.SKILL, RoxyEnums.ROXY_COLOR, CardRarity.UNCOMMON, CardTarget.SELF);
        this.baseMagicNumber = CHANT;
        this.magicNumber = this.baseMagicNumber;
    }

    @Override
    public void use(AbstractPlayer player, AbstractMonster monster) {
        this.addToBot(new MoveRandomAttackFromDiscardPileToHandAction(ReviewHelper.isReview(this)));
        this.addToBot(new ApplyPowerAction(player, player, new ChantPower(player, this.magicNumber), this.magicNumber));
    }

    @Override
    public void upgrade() {
        if (!this.upgraded) {
            this.upgradeName();
            this.upgradeMagicNumber(1);
        }
    }

    @Override
    public RoxyRecast makeCopy() {
        return new RoxyRecast();
    }
}
