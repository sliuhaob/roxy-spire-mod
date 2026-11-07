package startermod.cards;

import basemod.abstracts.CustomCard;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.GainEnergyAction;
import startermod.actions.ConsumeChantAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import startermod.RoxyEnums;
import startermod.StarterMod;
import startermod.util.ChantHelper;

public class RoxyTidalCut extends CustomCard {
    public static final String ID = StarterMod.makeID("RoxyTidalCut");

    private static final CardStrings CARD_STRINGS = CardCrawlGame.languagePack.getCardStrings(ID);
    private static final int COST = 1;
    private static final int DAMAGE = 9;
    private static final int ENERGY = 1;
    private static final int UPGRADE_DAMAGE = 3;

    public RoxyTidalCut() {
        super(ID, CARD_STRINGS.NAME, StarterMod.assetPath("images/cards/roxy_tidal_cut.png"), COST, CARD_STRINGS.DESCRIPTION,
            CardType.ATTACK, RoxyEnums.ROXY_COLOR, CardRarity.UNCOMMON, CardTarget.ENEMY);
        this.baseDamage = DAMAGE;
    }

    @Override
    public void use(AbstractPlayer player, AbstractMonster monster) {
        this.addToBot(new DamageAction(monster, new DamageInfo(player, this.damage, this.damageTypeForTurn),
            AbstractGameAction.AttackEffect.SLASH_HORIZONTAL));
        if (ChantHelper.amount(player) > 0) {
            this.addToBot(new ConsumeChantAction(player, 1));
            this.addToBot(new GainEnergyAction(ENERGY));
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
    public RoxyTidalCut makeCopy() {
        return new RoxyTidalCut();
    }
}
