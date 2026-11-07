package startermod.cards;

import basemod.abstracts.CustomCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import startermod.RoxyEnums;
import startermod.StarterMod;
import startermod.actions.ApplyFreezeAction;

public class RoxyIcicleField extends CustomCard {
    public static final String ID = StarterMod.makeID("RoxyIcicleField");

    private static final CardStrings CARD_STRINGS = CardCrawlGame.languagePack.getCardStrings(ID);
    private static final int COST = 1;
    private static final int FREEZE = 1;
    private static final int UPGRADE_FREEZE = 1;

    public RoxyIcicleField() {
        super(
            ID,
            CARD_STRINGS.NAME,
            StarterMod.assetPath("images/cards/roxy_icicle_field.png"),
            COST,
            CARD_STRINGS.DESCRIPTION,
            CardType.SKILL,
            RoxyEnums.ROXY_COLOR,
            CardRarity.UNCOMMON,
            CardTarget.ALL_ENEMY
        );
        this.baseMagicNumber = FREEZE;
        this.magicNumber = this.baseMagicNumber;
    }

    @Override
    public void use(AbstractPlayer player, AbstractMonster monster) {
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
            this.upgradeMagicNumber(UPGRADE_FREEZE);
        }
    }

    @Override
    public RoxyIcicleField makeCopy() {
        return new RoxyIcicleField();
    }
}
