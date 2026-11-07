package startermod.cards;

import basemod.abstracts.CustomCard;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import startermod.RoxyEnums;
import startermod.StarterMod;
import startermod.powers.WetPower;

public class RoxyWaterSplash extends CustomCard {
    public static final String ID = StarterMod.makeID("RoxyWaterSplash");

    private static final CardStrings CARD_STRINGS = CardCrawlGame.languagePack.getCardStrings(ID);
    private static final int COST = 1;
    private static final int WET = 1;
    private static final int DRAW = 1;

    public RoxyWaterSplash() {
        super(
            ID,
            CARD_STRINGS.NAME,
            StarterMod.assetPath("images/cards/roxy_water_splash.png"),
            COST,
            CARD_STRINGS.DESCRIPTION,
            CardType.SKILL,
            RoxyEnums.ROXY_COLOR,
            CardRarity.COMMON,
            CardTarget.ALL_ENEMY
        );

        this.baseMagicNumber = WET;
        this.magicNumber = this.baseMagicNumber;
    }

    @Override
    public void use(AbstractPlayer player, AbstractMonster monster) {
        for (AbstractMonster target : AbstractDungeon.getMonsters().monsters) {
            if (!target.isDeadOrEscaped()) {
                this.addToBot(new ApplyPowerAction(
                    target,
                    player,
                    new WetPower(target, this.magicNumber),
                    this.magicNumber
                ));
            }
        }
        if (this.upgraded) {
            this.addToBot(new DrawCardAction(player, DRAW));
        }
    }

    @Override
    public void upgrade() {
        if (!this.upgraded) {
            this.upgradeName();
            this.rawDescription = CARD_STRINGS.UPGRADE_DESCRIPTION;
            this.initializeDescription();
        }
    }

    @Override
    public RoxyWaterSplash makeCopy() {
        return new RoxyWaterSplash();
    }
}
