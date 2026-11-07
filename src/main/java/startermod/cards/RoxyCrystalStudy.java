package startermod.cards;

import basemod.abstracts.CustomCard;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import startermod.StarterMod;
import startermod.RoxyEnums;
import startermod.powers.ChantPower;

public class RoxyCrystalStudy extends CustomCard {
    public static final String ID = StarterMod.makeID("RoxyCrystalStudy");

    private static final CardStrings CARD_STRINGS = CardCrawlGame.languagePack.getCardStrings(ID);
    private static final int COST = 1;
    private static final int DRAW_AND_CHANT = 2;
    private static final int UPGRADE_AMOUNT = 1;

    public RoxyCrystalStudy() {
        super(
            ID,
            CARD_STRINGS.NAME,
            StarterMod.assetPath("images/cards/roxy_crystal_study.png"),
            COST,
            CARD_STRINGS.DESCRIPTION,
            CardType.SKILL,
            RoxyEnums.ROXY_COLOR,
            CardRarity.UNCOMMON,
            CardTarget.SELF
        );

        this.baseMagicNumber = DRAW_AND_CHANT;
        this.magicNumber = this.baseMagicNumber;
    }

    @Override
    public void use(AbstractPlayer player, AbstractMonster monster) {
        this.addToBot(new DrawCardAction(player, this.magicNumber));
        this.addToBot(new ApplyPowerAction(player, player, new ChantPower(player, this.magicNumber), this.magicNumber));
    }

    @Override
    public void upgrade() {
        if (!this.upgraded) {
            this.upgradeName();
            this.upgradeMagicNumber(UPGRADE_AMOUNT);
        }
    }

    @Override
    public RoxyCrystalStudy makeCopy() {
        return new RoxyCrystalStudy();
    }
}

