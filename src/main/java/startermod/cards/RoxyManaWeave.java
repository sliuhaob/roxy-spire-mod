package startermod.cards;

import basemod.abstracts.CustomCard;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import startermod.RoxyEnums;
import startermod.StarterMod;
import startermod.powers.ManaWeavePower;

public class RoxyManaWeave extends CustomCard {
    public static final String ID = StarterMod.makeID("RoxyManaWeave");

    private static final CardStrings CARD_STRINGS = CardCrawlGame.languagePack.getCardStrings(ID);
    private static final int COST = 2;
    private static final int DRAW = 1;

    public RoxyManaWeave() {
        super(ID, CARD_STRINGS.NAME, StarterMod.assetPath("images/cards/roxy_mana_weave.png"), COST, CARD_STRINGS.DESCRIPTION,
            CardType.POWER, RoxyEnums.ROXY_COLOR, CardRarity.UNCOMMON, CardTarget.SELF);
        this.baseMagicNumber = DRAW;
        this.magicNumber = this.baseMagicNumber;
    }

    @Override
    public void use(AbstractPlayer player, AbstractMonster monster) {
        this.addToBot(new ApplyPowerAction(player, player, new ManaWeavePower(player, this.magicNumber), this.magicNumber));
    }

    @Override
    public void upgrade() {
        if (!this.upgraded) {
            this.upgradeName();
            this.upgradeBaseCost(1);
        }
    }

    @Override
    public RoxyManaWeave makeCopy() {
        return new RoxyManaWeave();
    }
}
