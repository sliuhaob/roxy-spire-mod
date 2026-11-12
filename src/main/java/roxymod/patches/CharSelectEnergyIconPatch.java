package roxymod.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpireInstrumentPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.screens.charSelect.CharacterOption;
import com.megacrit.cardcrawl.screens.mainMenu.MainMenuScreen;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import roxymod.characters.Roxy;

/**
 * Text draws [E] with the current player's energy icon. On character select there is no run
 * yet, so the game falls back to the red Ironclad icon (or the last run's character), and
 * Roxy's starter relic showed a red icon there. While Roxy is the selected character, use her
 * icon instead. In identifyOrb, AbstractDungeon.player is read only for [E].
 */
@SpirePatch(clz = FontHelper.class, method = "identifyOrb")
public class CharSelectEnergyIconPatch {
    @SpireInstrumentPatch
    public static ExprEditor instrument() {
        return new ExprEditor() {
            @Override
            public void edit(FieldAccess access) throws CannotCompileException {
                if (access.isReader()
                    && access.getClassName().equals(AbstractDungeon.class.getName())
                    && access.getFieldName().equals("player")) {
                    access.replace("{ $_ = roxymod.patches.CharSelectEnergyIconPatch.energyIconOwner($proceed()); }");
                }
            }
        };
    }

    public static AbstractPlayer energyIconOwner(AbstractPlayer player) {
        if (CardCrawlGame.mode != CardCrawlGame.GameMode.CHAR_SELECT
            || CardCrawlGame.mainMenuScreen == null
            || CardCrawlGame.mainMenuScreen.screen != MainMenuScreen.CurScreen.CHAR_SELECT
            || CardCrawlGame.mainMenuScreen.charSelectScreen == null
            || CardCrawlGame.mainMenuScreen.charSelectScreen.options == null) {
            return player;
        }
        for (CharacterOption option : CardCrawlGame.mainMenuScreen.charSelectScreen.options) {
            if (option.selected && option.c instanceof Roxy) {
                return option.c;
            }
        }
        return player;
    }
}
