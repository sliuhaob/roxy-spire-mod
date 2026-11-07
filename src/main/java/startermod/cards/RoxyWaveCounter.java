package startermod.cards;

import basemod.abstracts.CustomCard;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import startermod.RoxyEnums;
import startermod.StarterMod;
import startermod.powers.WaveCounterPower;

public class RoxyWaveCounter extends CustomCard {
    public static final String ID = StarterMod.makeID("RoxyWaveCounter");

    private static final CardStrings CARD_STRINGS = CardCrawlGame.languagePack.getCardStrings(ID);
    private static final int COST = 1;
    private static final int BLOCK = 11;
    private static final int COUNTER_DAMAGE = 6;
    private static final int UPGRADE_BLOCK = 4;
    private static final int UPGRADE_COUNTER_DAMAGE = 2;

    public RoxyWaveCounter() {
        super(ID, CARD_STRINGS.NAME, StarterMod.assetPath("images/cards/roxy_wave_counter.png"), COST, CARD_STRINGS.DESCRIPTION,
            CardType.SKILL, RoxyEnums.ROXY_COLOR, CardRarity.UNCOMMON, CardTarget.SELF);
        this.baseBlock = BLOCK;
        this.baseMagicNumber = COUNTER_DAMAGE;
        this.magicNumber = this.baseMagicNumber;
    }

    @Override
    public void use(AbstractPlayer player, AbstractMonster monster) {
        this.addToBot(new GainBlockAction(player, player, this.block));
        this.addToBot(new ApplyPowerAction(player, player, new WaveCounterPower(player, this.magicNumber), this.magicNumber));
    }

    @Override
    public void upgrade() {
        if (!this.upgraded) {
            this.upgradeName();
            this.upgradeBlock(UPGRADE_BLOCK);
            this.upgradeMagicNumber(UPGRADE_COUNTER_DAMAGE);
        }
    }

    @Override
    public RoxyWaveCounter makeCopy() {
        return new RoxyWaveCounter();
    }
}
