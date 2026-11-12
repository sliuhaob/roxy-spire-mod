package roxymod.cards;

import basemod.abstracts.CustomCard;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import roxymod.RoxyMod;
import roxymod.RoxyEnums;
import roxymod.powers.ChantPower;

public class RoxyLabyrinthInsight extends CustomCard {
    public static final String ID = RoxyMod.makeID("RoxyLabyrinthInsight");

    private static final CardStrings CARD_STRINGS = CardCrawlGame.languagePack.getCardStrings(ID);
    private static final int COST = 1;
    private static final int DRAW = 2;
    private static final int CHANT = 1;
    private static final int UPGRADE_DRAW = 1;

    public RoxyLabyrinthInsight() {
        super(
            ID,
            CARD_STRINGS.NAME,
            RoxyMod.assetPath("images/cards/roxy_labyrinth_insight.png"),
            COST,
            CARD_STRINGS.DESCRIPTION,
            CardType.SKILL,
            RoxyEnums.ROXY_COLOR,
            CardRarity.UNCOMMON,
            CardTarget.SELF
        );

        this.baseMagicNumber = DRAW;
        this.magicNumber = this.baseMagicNumber;
    }

    @Override
    public void use(AbstractPlayer player, AbstractMonster monster) {
        this.addToBot(new DrawCardAction(player, this.magicNumber));
        this.addToBot(new ApplyPowerAction(player, player, new ChantPower(player, CHANT), CHANT));
    }

    @Override
    public void upgrade() {
        if (!this.upgraded) {
            this.upgradeName();
            this.upgradeMagicNumber(UPGRADE_DRAW);
        }
    }

    @Override
    public RoxyLabyrinthInsight makeCopy() {
        return new RoxyLabyrinthInsight();
    }
}

