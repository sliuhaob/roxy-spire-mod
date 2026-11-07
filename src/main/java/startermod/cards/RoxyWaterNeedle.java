package startermod.cards;

import basemod.abstracts.CustomCard;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import startermod.RoxyEnums;
import startermod.StarterMod;
import startermod.util.ChantHelper;
import startermod.actions.ConsumeChantAction;

public class RoxyWaterNeedle extends CustomCard {
    public static final String ID = StarterMod.makeID("RoxyWaterNeedle");

    private static final CardStrings CARD_STRINGS = CardCrawlGame.languagePack.getCardStrings(ID);
    private static final int COST = 0;
    private static final int DAMAGE = 4;
    private static final int DRAW = 1;
    private static final int UPGRADE_DAMAGE = 2;

    public RoxyWaterNeedle() {
        super(
            ID,
            CARD_STRINGS.NAME,
            StarterMod.assetPath("images/cards/roxy_water_needle.png"),
            COST,
            CARD_STRINGS.DESCRIPTION,
            CardType.ATTACK,
            RoxyEnums.ROXY_COLOR,
            CardRarity.COMMON,
            CardTarget.ENEMY
        );

        this.baseDamage = DAMAGE;
        this.baseMagicNumber = DRAW;
        this.magicNumber = this.baseMagicNumber;
    }

    @Override
    public void use(AbstractPlayer player, AbstractMonster monster) {
        this.addToBot(new DamageAction(
            monster,
            new DamageInfo(player, this.damage, this.damageTypeForTurn),
            AbstractGameAction.AttackEffect.SLASH_VERTICAL
        ));
        if (ChantHelper.amount(player) > 0) {
            this.addToBot(new ConsumeChantAction(player, 1));
            this.addToBot(new DrawCardAction(player, this.magicNumber));
        }
    }

    @Override
    public void upgrade() {
        if (!this.upgraded) {
            this.upgradeName();
            this.upgradeDamage(UPGRADE_DAMAGE);
        }
    }

    @Override
    public RoxyWaterNeedle makeCopy() {
        return new RoxyWaterNeedle();
    }
}
