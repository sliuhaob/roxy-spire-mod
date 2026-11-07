package startermod.cards;

import basemod.abstracts.CustomCard;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import startermod.RoxyEnums;
import startermod.StarterMod;
import startermod.cards.interfaces.FinisherCard;
import startermod.util.ChantHelper;
import startermod.util.ContinuationHelper;

public class RoxyWaterCurtain extends CustomCard implements FinisherCard {
    public static final String ID = StarterMod.makeID("RoxyWaterCurtain");

    private static final CardStrings CARD_STRINGS = CardCrawlGame.languagePack.getCardStrings(ID);
    private static final int COST = 1;
    private static final int BLOCK = 3;
    private static final int DRAW = 1;
    private static final int UPGRADE_CHANT_BLOCK_MULTIPLIER = 2;

    public RoxyWaterCurtain() {
        super(
            ID,
            CARD_STRINGS.NAME,
            StarterMod.assetPath("images/cards/roxy_water_curtain.png"),
            COST,
            CARD_STRINGS.DESCRIPTION,
            CardType.SKILL,
            RoxyEnums.ROXY_COLOR,
            CardRarity.BASIC,
            CardTarget.SELF
        );

        this.baseBlock = BLOCK;
        this.baseMagicNumber = DRAW;
        this.magicNumber = this.baseMagicNumber;
    }

    @Override
    public void use(AbstractPlayer player, AbstractMonster monster) {
        this.addToBot(new GainBlockAction(player, player, this.block));
        int chantBlock = ChantHelper.amount(player);
        if (chantBlock > 0) {
            if (this.upgraded) {
                chantBlock *= UPGRADE_CHANT_BLOCK_MULTIPLIER;
            }
            this.addToBot(new GainBlockAction(player, player, chantBlock));
        }
        if (ContinuationHelper.consume(player)) {
            this.addToBot(new DrawCardAction(player, this.magicNumber));
        }
    }

    @Override
    public void upgrade() {
        if (!this.upgraded) {
            this.upgradeName();
            this.rawDescription = CARD_STRINGS.UPGRADE_DESCRIPTION;
            this.initializeDescription();
        }
    }

    @Override
    public RoxyWaterCurtain makeCopy() {
        return new RoxyWaterCurtain();
    }
}
