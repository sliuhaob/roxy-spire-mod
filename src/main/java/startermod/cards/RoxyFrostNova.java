package startermod.cards;

import basemod.abstracts.CustomCard;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import startermod.actions.ConsumeChantAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import startermod.StarterMod;
import startermod.RoxyEnums;
import startermod.actions.ApplyFreezeAction;
import startermod.cards.interfaces.FinisherCard;
import startermod.util.ChantHelper;
import startermod.util.ContinuationHelper;

public class RoxyFrostNova extends CustomCard implements FinisherCard {
    public static final String ID = StarterMod.makeID("RoxyFrostNova");

    private static final CardStrings CARD_STRINGS = CardCrawlGame.languagePack.getCardStrings(ID);
    private static final int COST = 1;
    private static final int DAMAGE = 3;
    private static final int FREEZE = 2;
    private static final int CHANT_COST = 2;
    private static final int UPGRADE_CHANT_COST = -1;

    public RoxyFrostNova() {
        super(
            ID,
            CARD_STRINGS.NAME,
            StarterMod.assetPath("images/cards/roxy_frost_nova.png"),
            COST,
            CARD_STRINGS.DESCRIPTION,
            CardType.ATTACK,
            RoxyEnums.ROXY_COLOR,
            CardRarity.COMMON,
            CardTarget.ENEMY
        );

        this.baseDamage = DAMAGE;
        this.baseMagicNumber = CHANT_COST;
        this.magicNumber = this.baseMagicNumber;
    }

    @Override
    public void use(AbstractPlayer player, AbstractMonster monster) {
        boolean continued = ContinuationHelper.consume(player);
        int chantCost = Math.max(0, this.magicNumber - (continued ? 1 : 0));
        if (chantCost > 0) {
            this.addToBot(new ConsumeChantAction(player, chantCost));
        }
        this.addToBot(new DamageAction(
            monster,
            new DamageInfo(player, this.damage, this.damageTypeForTurn),
            AbstractGameAction.AttackEffect.BLUNT_LIGHT
        ));
        this.addToBot(new ApplyFreezeAction(monster, player, FREEZE));
    }

    @Override
    public boolean canUse(AbstractPlayer player, AbstractMonster monster) {
        if (!super.canUse(player, monster)) {
            return false;
        }
        int chantCost = Math.max(0, this.magicNumber - (ContinuationHelper.has(player) ? 1 : 0));
        if (ChantHelper.amount(player) < chantCost) {
            this.cantUseMessage = "需要至少 " + chantCost + " 层咏唱。";
            return false;
        }
        return true;
    }

    @Override
    public void upgrade() {
        if (!this.upgraded) {
            this.upgradeName();
            this.upgradeMagicNumber(UPGRADE_CHANT_COST);
        }
    }

    @Override
    public RoxyFrostNova makeCopy() {
        return new RoxyFrostNova();
    }
}
