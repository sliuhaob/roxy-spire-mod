package roxymod.cards;

import basemod.abstracts.CustomCard;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import roxymod.RoxyEnums;
import roxymod.RoxyMod;
import roxymod.powers.ChantPower;
import roxymod.util.ReviewHelper;

public class RoxyManaLecture extends CustomCard {
    public static final String ID = RoxyMod.makeID("RoxyManaLecture");

    private static final CardStrings CARD_STRINGS = CardCrawlGame.languagePack.getCardStrings(ID);
    private static final int COST = 0;
    private static final int DRAW = 1;
    private static final int CHANT = 1;
    private static final int UPGRADE_DRAW = 1;

    public RoxyManaLecture() {
        super(
            ID,
            CARD_STRINGS.NAME,
            RoxyMod.assetPath("images/cards/roxy_mana_lecture.png"),
            COST,
            CARD_STRINGS.DESCRIPTION,
            CardType.SKILL,
            RoxyEnums.ROXY_COLOR,
            CardRarity.COMMON,
            CardTarget.SELF
        );

        this.baseMagicNumber = DRAW;
        this.magicNumber = this.baseMagicNumber;
        this.exhaust = true;
    }

    @Override
    public void use(AbstractPlayer player, AbstractMonster monster) {
        this.addToBot(new DrawCardAction(player, this.magicNumber));
        if (ReviewHelper.isReview(this)) {
            this.addToBot(new ApplyPowerAction(player, player, new ChantPower(player, CHANT), CHANT));
        }
    }

    @Override
    public void upgrade() {
        if (!this.upgraded) {
            this.upgradeName();
            this.upgradeMagicNumber(UPGRADE_DRAW);
        }
    }

    @Override
    public RoxyManaLecture makeCopy() {
        return new RoxyManaLecture();
    }
}
