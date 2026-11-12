package roxymod.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpireField;
import com.evacipated.cardcrawl.modthespire.lib.SpireInstrumentPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.EnemyMoveInfo;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import roxymod.powers.FreezePower;

/**
 * Shows a stun intent while a monster's freeze has reached its counter.
 *
 * Only the intent display reads the substitute move. The monster's real move is never
 * replaced, so anything that changes it while frozen (a re-roll, a split, a phase change)
 * is still what it does once the freeze ends. This avoids saving and restoring the move.
 */
@SpirePatch(clz = AbstractMonster.class, method = "createIntent")
public class FreezeIntentPatch {
    @SpireInstrumentPatch
    public static ExprEditor instrument() {
        return new ExprEditor() {
            @Override
            public void edit(FieldAccess access) throws CannotCompileException {
                if (access.isReader()
                    && access.getClassName().equals(AbstractMonster.class.getName())
                    && access.getFieldName().equals("move")) {
                    access.replace("{ $_ = roxymod.patches.FreezeIntentPatch.displayedMove($0, $proceed()); }");
                }
            }
        };
    }

    public static EnemyMoveInfo displayedMove(AbstractMonster monster, EnemyMoveInfo realMove) {
        boolean frozen = FreezePower.isFrozen(monster);
        FrozenIntentField.shown.set(monster, frozen);
        if (!frozen) {
            return realMove;
        }
        byte pendingMove = realMove != null ? realMove.nextMove : monster.nextMove;
        return new EnemyMoveInfo(pendingMove, AbstractMonster.Intent.STUN, -1, 0, false);
    }

    /** Redraws the intent when freeze crosses the counter in either direction. */
    public static void refresh(AbstractMonster monster) {
        if (monster == null || monster.isDeadOrEscaped()) {
            return;
        }
        boolean shown = Boolean.TRUE.equals(FrozenIntentField.shown.get(monster));
        if (FreezePower.isFrozen(monster) != shown) {
            monster.createIntent();
        }
    }

    @SpirePatch(clz = AbstractMonster.class, method = SpirePatch.CLASS)
    public static class FrozenIntentField {
        public static SpireField<Boolean> shown = new SpireField<>(() -> false);
    }
}
