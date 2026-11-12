package roxymod.cards;

import basemod.abstracts.CustomCard;
import com.megacrit.cardcrawl.actions.common.GainEnergyAction;
import roxymod.actions.ConsumeChantAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import roxymod.RoxyEnums;
import roxymod.RoxyMod;
import roxymod.util.ChantHelper;

public class RoxyManaCondense extends CustomCard {
    public static final String ID = RoxyMod.makeID("RoxyManaCondense");

    private static final CardStrings CARD_STRINGS = CardCrawlGame.languagePack.getCardStrings(ID);
    private static final int COST = 0;
    private static final int CHANT_COST = 2;
    private static final int ENERGY = 2;

    public RoxyManaCondense() {
        super(ID, CARD_STRINGS.NAME, RoxyMod.assetPath("images/cards/roxy_mana_condense.png"), COST, CARD_STRINGS.DESCRIPTION,
            CardType.SKILL, RoxyEnums.ROXY_COLOR, CardRarity.UNCOMMON, CardTarget.SELF);
        this.baseMagicNumber = CHANT_COST;
        this.magicNumber = this.baseMagicNumber;
        this.exhaust = true;
    }

    @Override
    public void use(AbstractPlayer player, AbstractMonster monster) {
        if (ChantHelper.amount(player) >= this.magicNumber) {
            this.addToBot(new ConsumeChantAction(player, this.magicNumber));
            this.addToBot(new GainEnergyAction(ENERGY));
        }
    }

    @Override
    public void upgrade() {
        if (!this.upgraded) {
            this.upgradeName();
            this.upgradeMagicNumber(-1);
        }
    }

    @Override
    public RoxyManaCondense makeCopy() {
        return new RoxyManaCondense();
    }
}
