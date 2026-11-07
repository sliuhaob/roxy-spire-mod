package startermod.cards;

import basemod.abstracts.CustomCard;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import startermod.RoxyEnums;
import startermod.StarterMod;
import startermod.powers.CrystalDomainPower;

public class RoxyCrystalDomain extends CustomCard {
    public static final String ID = StarterMod.makeID("RoxyCrystalDomain");

    private static final CardStrings CARD_STRINGS = CardCrawlGame.languagePack.getCardStrings(ID);
    private static final int COST = 2;
    private static final int DRAW = 1;

    public RoxyCrystalDomain() {
        super(ID, CARD_STRINGS.NAME, StarterMod.assetPath("images/cards/roxy_crystal_domain.png"), COST, CARD_STRINGS.DESCRIPTION,
            CardType.POWER, RoxyEnums.ROXY_COLOR, CardRarity.RARE, CardTarget.SELF);
        this.baseMagicNumber = DRAW;
        this.magicNumber = this.baseMagicNumber;
    }

    @Override
    public void use(AbstractPlayer player, AbstractMonster monster) {
        this.addToBot(new ApplyPowerAction(player, player, new CrystalDomainPower(player, this.magicNumber), this.magicNumber));
    }

    @Override
    public void upgrade() {
        if (!this.upgraded) {
            this.upgradeName();
            this.upgradeBaseCost(1);
        }
    }

    @Override
    public RoxyCrystalDomain makeCopy() {
        return new RoxyCrystalDomain();
    }
}
