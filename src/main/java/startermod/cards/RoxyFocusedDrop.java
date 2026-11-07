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
import startermod.powers.WetPower;

public class RoxyFocusedDrop extends CustomCard {
    public static final String ID = StarterMod.makeID("RoxyFocusedDrop");

    private static final CardStrings CARD_STRINGS = CardCrawlGame.languagePack.getCardStrings(ID);
    private static final int COST = 2;
    private static final int DAMAGE = 15;
    private static final int WET = 2;
    private static final int UPGRADE_DAMAGE = 5;

    public RoxyFocusedDrop() {
        super(
            ID,
            CARD_STRINGS.NAME,
            StarterMod.assetPath("images/cards/roxy_focused_drop.png"),
            COST,
            CARD_STRINGS.DESCRIPTION,
            CardType.ATTACK,
            RoxyEnums.ROXY_COLOR,
            CardRarity.COMMON,
            CardTarget.ENEMY
        );

        this.baseDamage = DAMAGE;
        this.baseMagicNumber = WET;
        this.magicNumber = this.baseMagicNumber;
    }

    @Override
    public void use(AbstractPlayer player, AbstractMonster monster) {
        this.addToBot(new DamageAction(
            monster,
            new DamageInfo(player, this.damage, this.damageTypeForTurn),
            AbstractGameAction.AttackEffect.BLUNT_HEAVY
        ));
        this.addToBot(new ApplyPowerAction(
            monster,
            player,
            new WetPower(monster, this.magicNumber),
            this.magicNumber
        ));
    }

    @Override
    public void upgrade() {
        if (!this.upgraded) {
            this.upgradeName();
            this.upgradeDamage(UPGRADE_DAMAGE);
        }
    }

    @Override
    public RoxyFocusedDrop makeCopy() {
        return new RoxyFocusedDrop();
    }
}
