package startermod.cards;

import basemod.abstracts.CustomCard;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import startermod.actions.ConsumeChantAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import startermod.StarterMod;
import startermod.RoxyEnums;
import startermod.util.ChantHelper;

public class RoxyRainShelter extends CustomCard {
    public static final String ID = StarterMod.makeID("RoxyRainShelter");

    private static final CardStrings CARD_STRINGS = CardCrawlGame.languagePack.getCardStrings(ID);
    private static final int COST = 2;
    private static final int BLOCK = 16;
    private static final int EXTRA_BLOCK = 8;
    private static final int UPGRADE_BLOCK = 5;

    public RoxyRainShelter() {
        super(
            ID,
            CARD_STRINGS.NAME,
            StarterMod.assetPath("images/cards/roxy_rain_shelter.png"),
            COST,
            CARD_STRINGS.DESCRIPTION,
            CardType.SKILL,
            RoxyEnums.ROXY_COLOR,
            CardRarity.RARE,
            CardTarget.SELF
        );

        this.baseBlock = BLOCK;
        this.baseMagicNumber = EXTRA_BLOCK;
        this.magicNumber = this.baseMagicNumber;
    }

    @Override
    public void use(AbstractPlayer player, AbstractMonster monster) {
        this.addToBot(new GainBlockAction(player, player, this.block));
        this.addToBot(new DrawCardAction(player, 1));
        if (ChantHelper.amount(player) > 0) {
            this.addToBot(new ConsumeChantAction(player, 1));
            this.addToBot(new GainBlockAction(player, player, this.magicNumber));
        }
    }

    @Override
    public void upgrade() {
        if (!this.upgraded) {
            this.upgradeName();
            this.upgradeBlock(UPGRADE_BLOCK);
        }
    }

    @Override
    public RoxyRainShelter makeCopy() {
        return new RoxyRainShelter();
    }
}

