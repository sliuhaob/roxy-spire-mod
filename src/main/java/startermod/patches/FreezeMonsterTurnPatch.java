package startermod.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpireField;
import com.evacipated.cardcrawl.modthespire.lib.SpireInstrumentPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import startermod.powers.FreezePower;

@SpirePatch(clz = GameActionManager.class, method = "getNextAction")
public class FreezeMonsterTurnPatch {
    @SpireInstrumentPatch
    public static ExprEditor instrument() {
        return new ExprEditor() {
            @Override
            public void edit(MethodCall call) throws CannotCompileException {
                if (call.getClassName().equals(AbstractMonster.class.getName())
                    && call.getMethodName().equals("takeTurn")) {
                    call.replace("{ if (!startermod.patches.FreezeMonsterTurnPatch.shouldSkipTurn($0)) { $proceed($$); } }");
                }
            }
        };
    }

    public static boolean shouldSkipTurn(AbstractMonster monster) {
        if (monster == null
            || monster.isDeadOrEscaped()
            || !monster.hasPower(FreezePower.POWER_ID)
            || !(monster.getPower(FreezePower.POWER_ID) instanceof FreezePower)) {
            return false;
        }

        FreezePower freezePower = (FreezePower) monster.getPower(FreezePower.POWER_ID);
        if (!freezePower.shouldSkipTurn()) {
            return false;
        }

        freezePower.onSkipTurn();
        return true;
    }

    @SpirePatch(clz = AbstractMonster.class, method = SpirePatch.CLASS)
    public static class FreezeCounterField {
        public static SpireField<Integer> counter = new SpireField<>(() -> 3);
    }
}
