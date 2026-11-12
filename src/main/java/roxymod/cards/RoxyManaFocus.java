package roxymod.cards;

import basemod.abstracts.CustomCard;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.StrengthPower;
import roxymod.RoxyMod;
import roxymod.RoxyEnums;
import roxymod.powers.ChantPower;

public class RoxyManaFocus extends CustomCard {
    public static final String ID = RoxyMod.makeID("RoxyManaFocus");

    private static final CardStrings CARD_STRINGS = CardCrawlGame.languagePack.getCardStrings(ID);
    private static final int COST = 1;
    private static final int STRENGTH_AND_CHANT = 2;
    private static final int UPGRADE_AMOUNT = 1;

    public RoxyManaFocus() {
        super(
            ID,
            CARD_STRINGS.NAME,
            RoxyMod.assetPath("images/cards/roxy_mana_focus.png"),
            COST,
            CARD_STRINGS.DESCRIPTION,
            CardType.POWER,
            RoxyEnums.ROXY_COLOR,
            CardRarity.RARE,
            CardTarget.SELF
        );

        this.baseMagicNumber = STRENGTH_AND_CHANT;
        this.magicNumber = this.baseMagicNumber;
    }

    @Override
    public void use(AbstractPlayer player, AbstractMonster monster) {
        this.addToBot(new ApplyPowerAction(player, player, new StrengthPower(player, this.magicNumber), this.magicNumber));
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
    public RoxyManaFocus makeCopy() {
        return new RoxyManaFocus();
    }
}

