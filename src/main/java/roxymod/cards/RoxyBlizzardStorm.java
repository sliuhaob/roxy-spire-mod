package roxymod.cards;

import basemod.abstracts.CustomCard;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAllEnemiesAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import roxymod.RoxyEnums;
import roxymod.RoxyMod;
import roxymod.actions.ApplyFreezeAction;

public class RoxyBlizzardStorm extends CustomCard {
    public static final String ID = RoxyMod.makeID("RoxyBlizzardStorm");

    private static final CardStrings CARD_STRINGS = CardCrawlGame.languagePack.getCardStrings(ID);
    private static final int COST = 2;
    private static final int DAMAGE = 7;
    private static final int FREEZE = 1;
    private static final int UPGRADE_DAMAGE = 3;

    public RoxyBlizzardStorm() {
        super(
            ID,
            CARD_STRINGS.NAME,
            RoxyMod.assetPath("images/cards/roxy_blizzard_storm.png"),
            COST,
            CARD_STRINGS.DESCRIPTION,
            CardType.ATTACK,
            RoxyEnums.ROXY_COLOR,
            CardRarity.UNCOMMON,
            CardTarget.ALL_ENEMY
        );
        this.baseDamage = DAMAGE;
        this.baseMagicNumber = FREEZE;
        this.magicNumber = this.baseMagicNumber;
        this.isMultiDamage = true;
    }

    @Override
    public void use(AbstractPlayer player, AbstractMonster monster) {
        this.addToBot(new DamageAllEnemiesAction(
            player,
            this.multiDamage,
            this.damageTypeForTurn,
            AbstractGameAction.AttackEffect.SLASH_VERTICAL
        ));
        for (AbstractMonster target : AbstractDungeon.getMonsters().monsters) {
            if (!target.isDeadOrEscaped()) {
                this.addToBot(new ApplyFreezeAction(target, player, this.magicNumber));
            }
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
    public RoxyBlizzardStorm makeCopy() {
        return new RoxyBlizzardStorm();
    }
}
