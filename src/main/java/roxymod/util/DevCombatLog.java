package roxymod.util;

import basemod.BaseMod;
import basemod.interfaces.OnCardUseSubscriber;
import basemod.interfaces.OnPlayerTurnStartSubscriber;
import basemod.interfaces.OnStartBattleSubscriber;
import basemod.interfaces.PostBattleSubscriber;
import basemod.interfaces.PostDeathSubscriber;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.SeedHelper;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.StringJoiner;

/**
 * Dev-only playtest log, off unless the game starts with -Droxy.dev.combatLog=true.
 *
 * Appends one tab-separated row per event to roxymod-combat-log.tsv in the game folder
 * (override with -Droxy.dev.combatLogFile=path), plus a combat_end summary row per fight
 * that can be filtered in a spreadsheet. Every hook returns at once when the log is off,
 * and a write failure never interrupts the game.
 */
public final class DevCombatLog implements OnStartBattleSubscriber, PostBattleSubscriber,
        OnPlayerTurnStartSubscriber, OnCardUseSubscriber, PostDeathSubscriber {
    public static final boolean ENABLED = Boolean.getBoolean("roxy.dev.combatLog");
    private static final String HEADER =
        "seed\tfloor\tencounter\tturn\tevent\tamount\ttotal\tlast_card\tdetail";

    private static boolean inCombat;
    private static boolean writeFailed;
    private static String encounter = "";
    private static String lastCard = "";
    private static int turn;
    private static int hpAtStart;
    private static int cardsPlayed;
    private static int chantGained;
    private static int chantConsumed;
    private static int chantLost;
    private static int peakChant;
    private static int continuationGranted;
    private static int continuationConsumed;
    private static int continuationExpired;
    private static int exhaustion;
    private static int freezeApplied;
    private static int freezeSkips;

    private DevCombatLog() {
    }

    public static void register() {
        if (ENABLED) {
            BaseMod.subscribe(new DevCombatLog());
            System.out.println("[RoxyMod] Dev combat log on: " + file().toAbsolutePath());
        }
    }

    @Override
    public void receiveOnBattleStart(AbstractRoom room) {
        inCombat = true;
        turn = 0;
        cardsPlayed = chantGained = chantConsumed = chantLost = peakChant = 0;
        continuationGranted = continuationConsumed = continuationExpired = 0;
        exhaustion = freezeApplied = freezeSkips = 0;
        lastCard = "";
        StringJoiner names = new StringJoiner("+");
        if (room != null && room.monsters != null) {
            for (AbstractMonster m : room.monsters.monsters) {
                names.add(m.id);
            }
        }
        encounter = names.toString();
        hpAtStart = AbstractDungeon.player == null ? 0 : AbstractDungeon.player.currentHealth;
        row("combat_start", hpAtStart, maxHp(), "room=" + (room == null ? "" : room.getClass().getSimpleName()));
    }

    @Override
    public void receiveOnPlayerTurnStart() {
        if (!inCombat) {
            return;
        }
        turn++;
        lastCard = "";
        row("turn_start", chant(), AbstractDungeon.player.currentHealth, "block=" + AbstractDungeon.player.currentBlock);
    }

    @Override
    public void receiveCardUsed(AbstractCard card) {
        if (!inCombat || card == null) {
            return;
        }
        cardsPlayed++;
        lastCard = card.cardID + (card.upgraded ? "+" : "");
        row("card", card.costForTurn, chant(), "type=" + card.type);
    }

    @Override
    public void receivePostBattle(AbstractRoom room) {
        endCombat("won");
    }

    @Override
    public void receivePostDeath() {
        endCombat("died");
    }

    private static void endCombat(String result) {
        if (!inCombat) {
            return;
        }
        int hpLost = hpAtStart - (AbstractDungeon.player == null ? 0 : AbstractDungeon.player.currentHealth);
        row("combat_end", turn, hpLost, "result=" + result
            + " cards=" + cardsPlayed
            + " chant_gained=" + chantGained
            + " chant_consumed=" + chantConsumed
            + " chant_lost=" + chantLost
            + " chant_peak=" + peakChant
            + " continuation_granted=" + continuationGranted
            + " continuation_consumed=" + continuationConsumed
            + " continuation_expired=" + continuationExpired
            + " exhaustion=" + exhaustion
            + " freeze_applied=" + freezeApplied
            + " freeze_skips=" + freezeSkips);
        inCombat = false;
    }

    // Hooks called from the mod's own powers and helpers.

    public static void chantGained(AbstractCreature owner, int amount, int total) {
        if (!ENABLED || !isPlayer(owner) || amount <= 0) {
            return;
        }
        chantGained += amount;
        peakChant = Math.max(peakChant, total);
        row("chant_gain", amount, total, "");
    }

    public static void chantConsumed(AbstractCreature owner, int amount, int before) {
        if (!ENABLED || !isPlayer(owner) || amount <= 0) {
            return;
        }
        chantConsumed += amount;
        row("chant_spend", amount, before - amount, "");
    }

    public static void chantLost(AbstractCreature owner, int amount, String reason) {
        if (!ENABLED || !isPlayer(owner) || amount <= 0) {
            return;
        }
        chantLost += amount;
        row("chant_lost", amount, 0, reason);
    }

    public static void highRankSpell(int required, int paid, int exhaustionGained) {
        if (!ENABLED) {
            return;
        }
        exhaustion += exhaustionGained;
        row("high_rank_spell", required, paid, "exhaustion=" + exhaustionGained);
    }

    public static void continuation(String event) {
        if (!ENABLED) {
            return;
        }
        if (event.equals("grant")) {
            continuationGranted++;
        } else if (event.equals("consume")) {
            continuationConsumed++;
        } else if (event.equals("expire")) {
            continuationExpired++;
        }
        row("continuation_" + event, 1, 0, "");
    }

    public static void freezeApplied(AbstractMonster monster, int amount, int total, int counter) {
        if (!ENABLED || monster == null || amount <= 0) {
            return;
        }
        freezeApplied += amount;
        row("freeze_apply", amount, total, monster.id + " counter=" + counter);
    }

    public static void freezeSkip(AbstractMonster monster, int counter) {
        if (!ENABLED || monster == null) {
            return;
        }
        freezeSkips++;
        row("freeze_skip", counter, counter + 1, monster.id + " pending_move=" + monster.nextMove);
    }

    private static boolean isPlayer(AbstractCreature owner) {
        return owner != null && owner == AbstractDungeon.player;
    }

    private static int chant() {
        return ChantHelper.amount(AbstractDungeon.player);
    }

    private static int maxHp() {
        return AbstractDungeon.player == null ? 0 : AbstractDungeon.player.maxHealth;
    }

    private static Path file() {
        return Paths.get(System.getProperty("roxy.dev.combatLogFile", "roxymod-combat-log.tsv"));
    }

    private static void row(String event, int amount, int total, String detail) {
        if (!ENABLED || writeFailed) {
            return;
        }
        String seed = Settings.seed == null ? "" : SeedHelper.getString(Settings.seed);
        String line = seed + "\t" + AbstractDungeon.floorNum + "\t" + encounter + "\t" + turn + "\t" + event
            + "\t" + amount + "\t" + total + "\t" + lastCard + "\t" + detail;
        try {
            Path path = file();
            if (!Files.exists(path)) {
                Files.write(path, Collections.singletonList(HEADER), StandardCharsets.UTF_8, StandardOpenOption.CREATE);
            }
            Files.write(path, Collections.singletonList(line), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException | RuntimeException e) {
            writeFailed = true;
            System.err.println("[RoxyMod] Dev combat log disabled after a write error: " + e);
        }
    }
}
