package startermod.relics;

import basemod.abstracts.CustomRelic;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.RelicAboveCreatureAction;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import startermod.StarterMod;
import startermod.powers.ChantPower;

public class RoxyMilisCharm extends CustomRelic {
    public static final String ID = StarterMod.makeID("RoxyMilisCharm");

    private static final String IMG = StarterMod.assetPath("images/relics/milis_charm.png");
    private static final String OUTLINE = StarterMod.assetPath("images/relics/milis_charm_outline.png");
    private static final String LARGE_IMG = StarterMod.assetPath("images/relics/milis_charm_large.png");
    private static final int CHANT = 2;

    public RoxyMilisCharm() {
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
    public void onUsePotion() {
        if (AbstractDungeon.player == null
            || AbstractDungeon.getCurrRoom() == null
            || AbstractDungeon.getCurrRoom().phase != AbstractRoom.RoomPhase.COMBAT) {
            return;
        }

        this.flash();
        this.addToBot(new RelicAboveCreatureAction(AbstractDungeon.player, this));
        this.addToBot(new ApplyPowerAction(
            AbstractDungeon.player,
            AbstractDungeon.player,
            new ChantPower(AbstractDungeon.player, CHANT),
            CHANT
        ));
    }

    @Override
    public String getUpdatedDescription() {
        return this.DESCRIPTIONS[0];
    }

    @Override
    public AbstractRelic makeCopy() {
        return new RoxyMilisCharm();
    }
}
