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
import roxymod.powers.ChantPower;

public class RoxyChantPotion extends CustomPotion {
    public static final String POTION_ID = RoxyMod.makeID("RoxyChantPotion");

    private static final PotionStrings POTION_STRINGS = CardCrawlGame.languagePack.getPotionString(POTION_ID);

    public RoxyChantPotion() {
        super(
            POTION_STRINGS.NAME,
            POTION_ID,
            PotionRarity.COMMON,
            PotionSize.S,
            PotionColor.BLUE
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
            new ChantPower(AbstractDungeon.player, this.potency),
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
        return 4;
    }

    @Override
    public AbstractPotion makeCopy() {
        return new RoxyChantPotion();
    }
}
