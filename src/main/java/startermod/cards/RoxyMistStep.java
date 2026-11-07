package startermod.cards;

import basemod.abstracts.CustomCard;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import startermod.RoxyEnums;
import startermod.StarterMod;
import startermod.util.ChantHelper;

public class RoxyMistStep extends CustomCard {
    public static final String ID = StarterMod.makeID("RoxyMistStep");

    private static final CardStrings CARD_STRINGS = CardCrawlGame.languagePack.getCardStrings(ID);
    private static final int COST = 0;
    private static final int BLOCK = 4;
    private static final int EXTRA_BLOCK = 4;
    private static final int UPGRADE_BLOCK = 2;

    public RoxyMistStep() {
        super(
            ID,
            CARD_STRINGS.NAME,
            StarterMod.assetPath("images/cards/roxy_mist_step.png"),
            COST,
            CARD_STRINGS.DESCRIPTION,
            CardType.SKILL,
            RoxyEnums.ROXY_COLOR,
            CardRarity.COMMON,
            CardTarget.SELF
        );

        this.baseBlock = BLOCK;
        this.baseMagicNumber = EXTRA_BLOCK;
        this.magicNumber = this.baseMagicNumber;
    }

    @Override
    public void use(AbstractPlayer player, AbstractMonster monster) {
        this.addToBot(new GainBlockAction(player, player, this.block));
        if (ChantHelper.amount(player) > 0) {
            this.addToBot(new GainBlockAction(player, player, this.magicNumber));
        }
    }

    @Override
    public void upgrade() {
        if (!this.upgraded) {
            this.upgradeName();
            this.upgradeBlock(UPGRADE_BLOCK);
            this.upgradeMagicNumber(UPGRADE_BLOCK);
        }
    }

    @Override
    public RoxyMistStep makeCopy() {
        return new RoxyMistStep();
    }
}
