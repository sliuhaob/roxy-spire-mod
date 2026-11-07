package startermod.potions;

import basemod.abstracts.CustomPotion;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.PowerTip;
import com.megacrit.cardcrawl.localization.PotionStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import startermod.StarterMod;
import startermod.actions.ApplyFreezeAction;

public class RoxyWaterBarrierPotion extends CustomPotion {
    public static final String POTION_ID = StarterMod.makeID("RoxyWaterBarrierPotion");

    private static final PotionStrings POTION_STRINGS = CardCrawlGame.languagePack.getPotionString(POTION_ID);
    private static final int FREEZE = 2;

    public RoxyWaterBarrierPotion() {
        super(
            POTION_STRINGS.NAME,
            POTION_ID,
            PotionRarity.UNCOMMON,
            PotionSize.H,
            PotionColor.SKILL
        );
        this.isThrown = false;
        this.targetRequired = false;
        this.initializeData();
    }

    @Override
    public void use(AbstractCreature target) {
        this.addToBot(new GainBlockAction(AbstractDungeon.player, AbstractDungeon.player, this.potency));
        for (AbstractMonster monster : AbstractDungeon.getMonsters().monsters) {
            if (!monster.isDeadOrEscaped()) {
                this.addToBot(new ApplyFreezeAction(monster, AbstractDungeon.player, FREEZE));
            }
        }
    }

    @Override
    public void initializeData() {
        this.potency = this.getPotency();
        this.description = POTION_STRINGS.DESCRIPTIONS[0] + this.potency + POTION_STRINGS.DESCRIPTIONS[1] + FREEZE + POTION_STRINGS.DESCRIPTIONS[2];
        this.tips.clear();
        this.tips.add(new PowerTip(this.name, this.description));
    }

    @Override
    public int getPotency(int ascensionLevel) {
        return 8;
    }

    @Override
    public AbstractPotion makeCopy() {
        return new RoxyWaterBarrierPotion();
    }
}
