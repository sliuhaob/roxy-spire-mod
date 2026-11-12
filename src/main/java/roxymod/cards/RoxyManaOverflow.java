package roxymod.cards;

import basemod.abstracts.CustomCard;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.common.GainEnergyAction;
import roxymod.actions.ConsumeChantAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import roxymod.RoxyEnums;
import roxymod.RoxyMod;
import roxymod.util.ChantHelper;

public class RoxyManaOverflow extends CustomCard {
    public static final String ID = RoxyMod.makeID("RoxyManaOverflow");

    private static final CardStrings CARD_STRINGS = CardCrawlGame.languagePack.getCardStrings(ID);
    private static final int COST = 1;
    private static final int MAX_CHANT = 4;
    private static final int UPGRADE_MAX_CHANT = 2;

    public RoxyManaOverflow() {
        super(ID, CARD_STRINGS.NAME, RoxyMod.assetPath("images/cards/roxy_mana_overflow.png"), COST, CARD_STRINGS.DESCRIPTION,
            CardType.SKILL, RoxyEnums.ROXY_COLOR, CardRarity.RARE, CardTarget.SELF);
        this.baseMagicNumber = MAX_CHANT;
        this.magicNumber = this.baseMagicNumber;
        this.exhaust = true;
    }

    @Override
    public void use(AbstractPlayer player, AbstractMonster monster) {
        int chant = Math.min(ChantHelper.amount(player), this.magicNumber);
        if (chant > 0) {
            this.addToBot(new ConsumeChantAction(player, chant));
            this.addToBot(new GainEnergyAction(chant));
            this.addToBot(new DrawCardAction(player, chant));
        }
    }

    @Override
    public void upgrade() {
        if (!this.upgraded) {
            this.upgradeName();
            this.upgradeMagicNumber(UPGRADE_MAX_CHANT);
        }
    }

    @Override
    public RoxyManaOverflow makeCopy() {
        return new RoxyManaOverflow();
    }
}
