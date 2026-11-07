package startermod.cards;

import basemod.abstracts.CustomCard;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import startermod.RoxyEnums;
import startermod.StarterMod;
import startermod.powers.ChantPower;
import startermod.powers.FreezePower;

public class RoxyMeasuredCast extends CustomCard {
    public static final String ID = StarterMod.makeID("RoxyMeasuredCast");

    private static final CardStrings CARD_STRINGS = CardCrawlGame.languagePack.getCardStrings(ID);
    private static final int COST = 2;
    private static final int DAMAGE = 18;
    private static final int CHANT = 2;
    private static final int UPGRADE_CHANT = 1;

    public RoxyMeasuredCast() {
        super(ID, CARD_STRINGS.NAME, StarterMod.assetPath("images/cards/roxy_measured_cast.png"), COST, CARD_STRINGS.DESCRIPTION,
            CardType.ATTACK, RoxyEnums.ROXY_COLOR, CardRarity.UNCOMMON, CardTarget.ENEMY);
        this.baseDamage = DAMAGE;
        this.baseMagicNumber = CHANT;
        this.magicNumber = this.baseMagicNumber;
    }

    @Override
    public void use(AbstractPlayer player, AbstractMonster monster) {
        this.addToBot(new DamageAction(monster, new DamageInfo(player, this.damage, this.damageTypeForTurn),
            AbstractGameAction.AttackEffect.BLUNT_HEAVY));
        if (monster != null && monster.hasPower(FreezePower.POWER_ID)) {
            this.addToBot(new ApplyPowerAction(player, player, new ChantPower(player, this.magicNumber), this.magicNumber));
        }
    }

    @Override
    public void upgrade() {
        if (!this.upgraded) {
            this.upgradeName();
            this.upgradeMagicNumber(UPGRADE_CHANT);
        }
    }

    @Override
    public RoxyMeasuredCast makeCopy() {
        return new RoxyMeasuredCast();
    }
}
