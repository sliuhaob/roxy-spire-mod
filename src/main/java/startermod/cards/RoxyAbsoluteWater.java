package startermod.cards;

import basemod.abstracts.CustomCard;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import startermod.RoxyEnums;
import startermod.StarterMod;
import startermod.powers.AbsoluteWaterPower;

public class RoxyAbsoluteWater extends CustomCard {
    public static final String ID = StarterMod.makeID("RoxyAbsoluteWater");

    private static final CardStrings CARD_STRINGS = CardCrawlGame.languagePack.getCardStrings(ID);
    private static final int COST = 3;
    private static final int DAMAGE_BONUS = 3;

    public RoxyAbsoluteWater() {
        super(ID, CARD_STRINGS.NAME, StarterMod.assetPath("images/cards/roxy_absolute_water.png"), COST, CARD_STRINGS.DESCRIPTION,
            CardType.POWER, RoxyEnums.ROXY_COLOR, CardRarity.RARE, CardTarget.SELF);
        this.baseMagicNumber = DAMAGE_BONUS;
        this.magicNumber = this.baseMagicNumber;
    }

    @Override
    public void use(AbstractPlayer player, AbstractMonster monster) {
        this.addToBot(new ApplyPowerAction(player, player, new AbsoluteWaterPower(player, this.magicNumber), this.magicNumber));
    }

    @Override
    public void upgrade() {
        if (!this.upgraded) {
            this.upgradeName();
            this.upgradeBaseCost(2);
        }
    }

    @Override
    public RoxyAbsoluteWater makeCopy() {
        return new RoxyAbsoluteWater();
    }
}
