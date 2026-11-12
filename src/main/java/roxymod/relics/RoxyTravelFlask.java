package roxymod.relics;

import basemod.abstracts.CustomRelic;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.potions.PotionSlot;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import roxymod.RoxyMod;

public class RoxyTravelFlask extends CustomRelic {
    public static final String ID = RoxyMod.makeID("RoxyTravelFlask");

    private static final String IMG = RoxyMod.assetPath("images/relics/travel_flask.png");
    private static final String OUTLINE = RoxyMod.assetPath("images/relics/travel_flask_outline.png");
    private static final String LARGE_IMG = RoxyMod.assetPath("images/relics/travel_flask_large.png");

    public RoxyTravelFlask() {
        super(
            ID,
            ImageMaster.loadImage(IMG),
            ImageMaster.loadImage(OUTLINE),
            RelicTier.SHOP,
            LandingSound.CLINK
        );
        this.largeImg = ImageMaster.loadImage(LARGE_IMG);
    }

    @Override
    public void onEquip() {
        AbstractDungeon.player.potionSlots += 1;
        AbstractDungeon.player.potions.add(new PotionSlot(AbstractDungeon.player.potionSlots - 1));
        AbstractDungeon.player.adjustPotionPositions();
    }

    @Override
    public void onUnequip() {
        if (AbstractDungeon.player.potionSlots > 0 && !AbstractDungeon.player.potions.isEmpty()) {
            AbstractDungeon.player.potionSlots -= 1;
            AbstractDungeon.player.potions.remove(AbstractDungeon.player.potions.size() - 1);
            AbstractDungeon.player.adjustPotionPositions();
        }
    }

    @Override
    public String getUpdatedDescription() {
        return this.DESCRIPTIONS[0];
    }

    @Override
    public AbstractRelic makeCopy() {
        return new RoxyTravelFlask();
    }
}
