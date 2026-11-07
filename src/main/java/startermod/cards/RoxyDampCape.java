package startermod.cards;

import basemod.abstracts.CustomCard;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.WeakPower;
import startermod.RoxyEnums;
import startermod.StarterMod;

public class RoxyDampCape extends CustomCard {
    public static final String ID = StarterMod.makeID("RoxyDampCape");

    private static final CardStrings CARD_STRINGS = CardCrawlGame.languagePack.getCardStrings(ID);
    private static final int COST = 1;
    private static final int BLOCK = 6;
    private static final int WEAK = 1;
    private static final int UPGRADE_BLOCK = 3;

    public RoxyDampCape() {
        super(
            ID,
            CARD_STRINGS.NAME,
            StarterMod.assetPath("images/cards/roxy_damp_cape.png"),
            COST,
            CARD_STRINGS.DESCRIPTION,
            CardType.SKILL,
            RoxyEnums.ROXY_COLOR,
            CardRarity.COMMON,
            CardTarget.ALL_ENEMY
        );

        this.baseBlock = BLOCK;
        this.baseMagicNumber = WEAK;
        this.magicNumber = this.baseMagicNumber;
    }

    @Override
    public void use(AbstractPlayer player, AbstractMonster monster) {
        this.addToBot(new GainBlockAction(player, player, this.block));
        for (AbstractMonster target : AbstractDungeon.getMonsters().monsters) {
            if (!target.isDeadOrEscaped()) {
                this.addToBot(new ApplyPowerAction(target, player, new WeakPower(target, this.magicNumber, false), this.magicNumber));
            }
        }
    }

    @Override
    public void upgrade() {
        if (!this.upgraded) {
            this.upgradeName();
            this.upgradeBlock(UPGRADE_BLOCK);
        }
    }

    @Override
    public RoxyDampCape makeCopy() {
        return new RoxyDampCape();
    }
}
