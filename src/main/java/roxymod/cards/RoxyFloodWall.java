package roxymod.cards;

import basemod.abstracts.CustomCard;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import roxymod.RoxyEnums;
import roxymod.RoxyMod;
import roxymod.cards.interfaces.FinisherCard;
import roxymod.util.ChantHelper;
import roxymod.util.ContinuationHelper;

public class RoxyFloodWall extends CustomCard implements FinisherCard {
    public static final String ID = RoxyMod.makeID("RoxyFloodWall");

    private static final CardStrings CARD_STRINGS = CardCrawlGame.languagePack.getCardStrings(ID);
    private static final int COST = 2;
    private static final int BLOCK = 16;
    private static final int BLOCK_PER_CHANT = 2;
    private static final int UPGRADE_BLOCK = 4;

    public RoxyFloodWall() {
        super(ID, CARD_STRINGS.NAME, RoxyMod.assetPath("images/cards/roxy_flood_wall.png"), COST, CARD_STRINGS.DESCRIPTION,
            CardType.SKILL, RoxyEnums.ROXY_COLOR, CardRarity.UNCOMMON, CardTarget.SELF);
        this.baseBlock = BLOCK;
        this.baseMagicNumber = BLOCK_PER_CHANT;
        this.magicNumber = this.baseMagicNumber;
    }

    @Override
    public void use(AbstractPlayer player, AbstractMonster monster) {
        int totalBlock = this.block;
        if (ContinuationHelper.consume(player)) {
            totalBlock += ChantHelper.amount(player) * this.magicNumber;
        }
        this.addToBot(new GainBlockAction(player, player, totalBlock));
    }

    @Override
    public void upgrade() {
        if (!this.upgraded) {
            this.upgradeName();
            this.upgradeBlock(UPGRADE_BLOCK);
        }
    }

    @Override
    public RoxyFloodWall makeCopy() {
        return new RoxyFloodWall();
    }
}
