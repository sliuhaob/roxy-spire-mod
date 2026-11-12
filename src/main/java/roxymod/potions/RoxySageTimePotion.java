package roxymod.potions;

import basemod.abstracts.CustomPotion;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.PowerTip;
import com.megacrit.cardcrawl.localization.PotionStrings;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import roxymod.RoxyMod;
import roxymod.powers.SageTimePower;

public class RoxySageTimePotion extends CustomPotion {
    public static final String POTION_ID = RoxyMod.makeID("RoxySageTimePotion");

    private static final PotionStrings POTION_STRINGS = CardCrawlGame.languagePack.getPotionString(POTION_ID);

    public RoxySageTimePotion() {
        super(
            POTION_STRINGS.NAME,
            POTION_ID,
            PotionRarity.RARE,
            PotionSize.MOON,
            PotionColor.POWER
        );
        this.isThrown = false;
        this.targetRequired = false;
        this.initializeData();
    }

    @Override
    public void use(AbstractCreature target) {
        this.addToBot(new ApplyPowerAction(
            AbstractDungeon.player,
            AbstractDungeon.player,
            new SageTimePower(AbstractDungeon.player, this.potency),
            this.potency
        ));
    }

    @Override
    public void initializeData() {
        this.potency = this.getPotency();
        this.description = POTION_STRINGS.DESCRIPTIONS[0] + this.potency + POTION_STRINGS.DESCRIPTIONS[1];
        this.tips.clear();
        this.tips.add(new PowerTip(this.name, this.description));
    }

    @Override
    public int getPotency(int ascensionLevel) {
        return 2;
    }

    @Override
    public AbstractPotion makeCopy() {
        return new RoxySageTimePotion();
    }
}
