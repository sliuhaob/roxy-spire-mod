package roxymod.cards;

import basemod.abstracts.CustomCard;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.EnergizedPower;
import roxymod.RoxyEnums;
import roxymod.RoxyMod;
import roxymod.powers.ChantPower;

public class RoxyManaReserve extends CustomCard {
    public static final String ID = RoxyMod.makeID("RoxyManaReserve");

    private static final CardStrings CARD_STRINGS = CardCrawlGame.languagePack.getCardStrings(ID);
    private static final int COST = 1;
    private static final int CHANT = 2;
    private static final int NEXT_TURN_ENERGY = 2;

    public RoxyManaReserve() {
        super(ID, CARD_STRINGS.NAME, RoxyMod.assetPath("images/cards/roxy_mana_reserve.png"), COST, CARD_STRINGS.DESCRIPTION,
            CardType.SKILL, RoxyEnums.ROXY_COLOR, CardRarity.COMMON, CardTarget.SELF);
        this.baseMagicNumber = CHANT;
        this.magicNumber = this.baseMagicNumber;
    }

    @Override
    public void use(AbstractPlayer player, AbstractMonster monster) {
        this.addToBot(new ApplyPowerAction(player, player, new ChantPower(player, this.magicNumber), this.magicNumber));
        this.addToBot(new ApplyPowerAction(player, player, new EnergizedPower(player, NEXT_TURN_ENERGY), NEXT_TURN_ENERGY));
    }

    @Override
    public void upgrade() {
        if (!this.upgraded) {
            this.upgradeName();
            this.upgradeBaseCost(0);
        }
    }

    @Override
    public RoxyManaReserve makeCopy() {
        return new RoxyManaReserve();
    }
}
