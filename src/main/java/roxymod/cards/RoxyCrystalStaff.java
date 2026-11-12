package roxymod.cards;

import basemod.abstracts.CustomCard;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import roxymod.RoxyEnums;
import roxymod.RoxyMod;
import roxymod.powers.CrystalStaffPower;

public class RoxyCrystalStaff extends CustomCard {
    public static final String ID = RoxyMod.makeID("RoxyCrystalStaff");

    private static final CardStrings CARD_STRINGS = CardCrawlGame.languagePack.getCardStrings(ID);
    private static final int COST = 1;
    private static final int BLOCK_PER_CHANT_GAIN = 1;
    private static final int UPGRADE_AMOUNT = 1;

    public RoxyCrystalStaff() {
        super(ID, CARD_STRINGS.NAME, RoxyMod.assetPath("images/cards/roxy_crystal_staff.png"), COST, CARD_STRINGS.DESCRIPTION,
            CardType.POWER, RoxyEnums.ROXY_COLOR, CardRarity.UNCOMMON, CardTarget.SELF);
        this.baseMagicNumber = BLOCK_PER_CHANT_GAIN;
        this.magicNumber = this.baseMagicNumber;
    }

    @Override
    public void use(AbstractPlayer player, AbstractMonster monster) {
        this.addToBot(new ApplyPowerAction(player, player, new CrystalStaffPower(player, this.magicNumber), this.magicNumber));
    }

    @Override
    public void upgrade() {
        if (!this.upgraded) {
            this.upgradeName();
            this.upgradeMagicNumber(UPGRADE_AMOUNT);
        }
    }

    @Override
    public RoxyCrystalStaff makeCopy() {
        return new RoxyCrystalStaff();
    }
}
