package startermod.cards;

import basemod.abstracts.CustomCard;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import startermod.StarterMod;
import startermod.RoxyEnums;
import startermod.powers.StartTurnChantPower;

public class RoxyManaCycle extends CustomCard {
    public static final String ID = StarterMod.makeID("RoxyManaCycle");

    private static final CardStrings CARD_STRINGS = CardCrawlGame.languagePack.getCardStrings(ID);
    private static final int COST = 1;
    private static final int CHANT_PER_TURN = 1;

    public RoxyManaCycle() {
        super(
            ID,
            CARD_STRINGS.NAME,
            StarterMod.assetPath("images/cards/roxy_mana_cycle.png"),
            COST,
            CARD_STRINGS.DESCRIPTION,
            CardType.POWER,
            RoxyEnums.ROXY_COLOR,
            CardRarity.UNCOMMON,
            CardTarget.SELF
        );

        this.baseMagicNumber = CHANT_PER_TURN;
        this.magicNumber = this.baseMagicNumber;
    }

    @Override
    public void use(AbstractPlayer player, AbstractMonster monster) {
        this.addToBot(new ApplyPowerAction(
            player,
            player,
            new StartTurnChantPower(player, this.magicNumber),
            this.magicNumber
        ));
    }

    @Override
    public void upgrade() {
        if (!this.upgraded) {
            this.upgradeName();
            this.upgradeBaseCost(0);
        }
    }

    @Override
    public RoxyManaCycle makeCopy() {
        return new RoxyManaCycle();
    }
}

