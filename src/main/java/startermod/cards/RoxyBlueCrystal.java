package startermod.cards;

import basemod.abstracts.CustomCard;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import startermod.RoxyEnums;
import startermod.StarterMod;
import startermod.util.ChantHelper;

public class RoxyBlueCrystal extends CustomCard {
    public static final String ID = StarterMod.makeID("RoxyBlueCrystal");

    private static final CardStrings CARD_STRINGS = CardCrawlGame.languagePack.getCardStrings(ID);
    private static final int COST = 0;
    private static final int DRAW = 2;
    private static final int REQUIRED_CHANT = 3;

    public RoxyBlueCrystal() {
        super(ID, CARD_STRINGS.NAME, StarterMod.assetPath("images/cards/roxy_blue_crystal.png"), COST, CARD_STRINGS.DESCRIPTION,
            CardType.SKILL, RoxyEnums.ROXY_COLOR, CardRarity.UNCOMMON, CardTarget.SELF);
        this.baseMagicNumber = DRAW;
        this.magicNumber = this.baseMagicNumber;
        this.exhaust = true;
    }

    @Override
    public void use(AbstractPlayer player, AbstractMonster monster) {
        if (ChantHelper.amount(player) >= REQUIRED_CHANT) {
            this.addToBot(new DrawCardAction(player, this.magicNumber));
        }
    }

    @Override
    public void upgrade() {
        if (!this.upgraded) {
            this.upgradeName();
            this.upgradeMagicNumber(1);
        }
    }

    @Override
    public RoxyBlueCrystal makeCopy() {
        return new RoxyBlueCrystal();
    }
}
