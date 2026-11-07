package startermod.cards;

import basemod.abstracts.CustomCard;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import startermod.StarterMod;
import startermod.RoxyEnums;
import startermod.powers.ChantPower;

public class RoxyLabyrinthRescue extends CustomCard {
    public static final String ID = StarterMod.makeID("RoxyLabyrinthRescue");

    private static final CardStrings CARD_STRINGS = CardCrawlGame.languagePack.getCardStrings(ID);
    private static final int COST = 2;
    private static final int BLOCK = 18;
    private static final int DRAW_AND_CHANT = 2;
    private static final int UPGRADE_BLOCK = 6;

    public RoxyLabyrinthRescue() {
        super(
            ID,
            CARD_STRINGS.NAME,
            StarterMod.assetPath("images/cards/roxy_labyrinth_rescue.png"),
            COST,
            CARD_STRINGS.DESCRIPTION,
            CardType.SKILL,
            RoxyEnums.ROXY_COLOR,
            CardRarity.RARE,
            CardTarget.SELF
        );

        this.baseBlock = BLOCK;
        this.baseMagicNumber = DRAW_AND_CHANT;
        this.magicNumber = this.baseMagicNumber;
    }

    @Override
    public void use(AbstractPlayer player, AbstractMonster monster) {
        this.addToBot(new GainBlockAction(player, player, this.block));
        this.addToBot(new DrawCardAction(player, this.magicNumber));
        this.addToBot(new ApplyPowerAction(player, player, new ChantPower(player, this.magicNumber), this.magicNumber));
    }

    @Override
    public void upgrade() {
        if (!this.upgraded) {
            this.upgradeName();
            this.upgradeBlock(UPGRADE_BLOCK);
        }
    }

    @Override
    public RoxyLabyrinthRescue makeCopy() {
        return new RoxyLabyrinthRescue();
    }
}

