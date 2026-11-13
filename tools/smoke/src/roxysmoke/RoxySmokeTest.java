package roxysmoke;

import basemod.BaseMod;
import basemod.devcommands.ConsoleCommand;
import basemod.interfaces.PostRenderSubscriber;
import basemod.interfaces.PostUpdateSubscriber;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardQueueItem;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.ModHelper;
import com.megacrit.cardcrawl.helpers.RelicLibrary;
import com.megacrit.cardcrawl.map.MapEdge;
import com.megacrit.cardcrawl.map.MapRoomNode;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.city.GremlinLeader;
import com.megacrit.cardcrawl.monsters.exordium.AcidSlime_L;
import com.megacrit.cardcrawl.monsters.exordium.GremlinNob;
import com.megacrit.cardcrawl.monsters.exordium.Hexaghost;
import com.megacrit.cardcrawl.monsters.exordium.JawWorm;
import com.megacrit.cardcrawl.monsters.exordium.Sentry;
import com.megacrit.cardcrawl.monsters.exordium.SlimeBoss;
import com.megacrit.cardcrawl.monsters.exordium.SpikeSlime_L;
import com.megacrit.cardcrawl.neow.NeowRoom;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.powers.AngerPower;
import com.megacrit.cardcrawl.powers.IntangiblePower;
import com.megacrit.cardcrawl.powers.VulnerablePower;
import com.megacrit.cardcrawl.powers.WeakPower;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.rooms.MonsterRoom;
import com.megacrit.cardcrawl.rooms.RestRoom;
import com.megacrit.cardcrawl.rooms.TreasureRoom;
import com.megacrit.cardcrawl.rooms.TreasureRoomBoss;
import com.megacrit.cardcrawl.saveAndContinue.SaveAndContinue;
import com.megacrit.cardcrawl.screens.charSelect.CharacterOption;
import com.megacrit.cardcrawl.screens.charSelect.CharacterSelectScreen;
import com.megacrit.cardcrawl.screens.options.ConfirmPopup;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BooleanSupplier;
import roxymod.RoxyEnums;
import roxymod.actions.ApplyFreezeAction;
import roxymod.potions.RoxyWaterBarrierPotion;
import roxymod.powers.ChantPower;
import roxymod.powers.FreezePower;
import roxymod.powers.WetPower;
import roxymod.util.ChantHelper;
import roxymod.util.ContinuationHelper;

/**
 * Dev-only in-game smoke test, loaded only by tools/smoke-test.ps1. That script runs the game
 * from a sandbox folder, so your real profile, saves and mods folder are never touched.
 *
 * It drives the real game on a fixed seed and checks, with screenshots: character select and
 * every card face; Freeze delaying scripted moves (Gremlin Nob, Hexaghost), a whole frozen group
 * (Gremlin Leader) and a split triggered while frozen (Slime Boss); Lightning's numbers under
 * Weak, Vulnerable, Wet and Intangible; full-hand card moves and Review; every relic and the boss
 * crystal picked on the boss relic screen; Water Sigil against Artifact, Wet and the Water
 * Barrier potion; the large card view, rest site and chest; Save and Continue; and the death
 * screen. Results go to -Droxy.smoke.out (smoke-report.txt), then the game exits.
 */
@SpireInitializer
public class RoxySmokeTest implements PostUpdateSubscriber, PostRenderSubscriber {
    private static final File OUT = new File(System.getProperty("roxy.smoke.out", "roxy-smoke"));
    private static final long SEED = 31415926L;
    private static final float TOTAL_TIMEOUT = 1100f;
    private static final int SHEET_COLUMNS = 7;
    private static final int SHEET_ROWS = 3;
    private static final AbstractMonster.Intent STUN = AbstractMonster.Intent.STUN;
    private static final String[] RELICS = {
        "roxymod:RoxyChantPrimer", "roxymod:RoxyTeachingPointer", "roxymod:RoxyHydraulicSeal",
        "roxymod:RoxyTravelFlask", "roxymod:RoxyWaterSigilRelic",
        "roxymod:RoxyLessonNotebook", "roxymod:RoxyClearWaterPendant", "roxymod:RoxyMilisCharm"
    };

    private interface Step {
        boolean tick() throws Exception;
    }

    private static final class Task {
        final String name;
        final float timeout;
        final Step step;

        Task(String name, float timeout, Step step) {
            this.name = name;
            this.timeout = timeout;
            this.step = step;
        }
    }

    private final ArrayDeque<Task> tasks = new ArrayDeque<>();
    private final List<String> report = new ArrayList<>();
    private Task current;
    private float taskTime;
    private float totalTime;
    private boolean finished;
    private int passes;
    private int failures;

    private String pendingShot;
    private List<AbstractCard> pendingSheet;
    private boolean shotTaken;
    private final List<List<AbstractCard>> sheets = new ArrayList<>();

    private GremlinNob nob;
    private Hexaghost hexaghost;
    private SlimeBoss slimeBoss;
    private int turnMark;
    private AbstractCard lightning;
    private AbstractCard drawAttack;
    private AbstractCard discardAttack;
    private List<AbstractCard> handBefore = new ArrayList<>();
    private List<AbstractMonster> group = new ArrayList<>();
    private final List<Byte> movesBefore = new ArrayList<>();
    private final List<AbstractMonster.Intent> intentsBefore = new ArrayList<>();
    private final List<Integer> historyBefore = new ArrayList<>();
    private final List<Integer> freezesBefore = new ArrayList<>();
    private AbstractRelic bossCrystal;
    private AbstractPotion potion;
    private int expected;
    private int hpBefore;
    private int freezeBefore;
    private int chantBefore;
    private int blockBefore;
    private String runBefore;

    public static void initialize() {
        BaseMod.subscribe(new RoxySmokeTest());
    }

    public RoxySmokeTest() {
        OUT.mkdirs();
        this.script();
    }

    private void script() {
        bootAndEmbark();
        nobFreeze();
        hexaghostFreeze();
        cardPopup();
        lightningAndFullHand();
        gremlinLeaderFreeze();
        slimeBossSplit();
        // Relics come after the Freeze fights so their battle-start effects cannot change them.
        relicsAndBossChoice();
        sentriesArtifactAndPotion();
        restSite();
        treasureChest();
        saveAndContinue();
        // Last: dying ends the run.
        death();
    }

    private void bootAndEmbark() {
        waitUntil("main menu is up", 180, () -> CardCrawlGame.mode == CardCrawlGame.GameMode.CHAR_SELECT
            && CardCrawlGame.mainMenuScreen != null && !CardCrawlGame.mainMenuScreen.isFadingOut);
        delay(2);
        once("content is registered", () -> {
            check("75 Roxy cards in the card library", roxyCards().size() == 75, "found " + roxyCards().size());
            check("Roxy is on character select", roxyOption() != null, "");
        });
        once("open character select", () -> CardCrawlGame.mainMenuScreen.charSelectScreen.open(false));
        delay(1);
        once("click Roxy", () -> roxyOption().hb.clicked = true);
        delay(2.5f);
        shot("01-character-select");
        once("embark on a fixed seed", () -> {
            CharacterSelectScreen screen = CardCrawlGame.mainMenuScreen.charSelectScreen;
            check("Roxy selected", CardCrawlGame.chosenCharacter == RoxyEnums.ROXY, String.valueOf(CardCrawlGame.chosenCharacter));
            screen.isAscensionMode = false;
            Settings.seed = SEED;
            screen.confirmButton.hb.clicked = true;
        });
        waitUntil("run starts at Neow", 120, () -> CardCrawlGame.mode == CardCrawlGame.GameMode.GAMEPLAY
            && CardCrawlGame.dungeonTransitionScreen == null && AbstractDungeon.player != null
            && AbstractDungeon.currMapNode != null && currentRoom() instanceof NeowRoom);
        delay(3);
        once("starting loadout", () -> {
            check("playing as Roxy", AbstractDungeon.player.chosenClass == RoxyEnums.ROXY, String.valueOf(AbstractDungeon.player.chosenClass));
            check("starter deck has 10 cards", AbstractDungeon.player.masterDeck.size() == 10, "size " + AbstractDungeon.player.masterDeck.size());
            check("starter relic equipped", !AbstractDungeon.player.relics.isEmpty(), "no relics");
            check("Roxy's own energy orb is in use (blue energy flash)",
                AbstractDungeon.player.getEnergyImage() == ImageMaster.BLUE_ORB_FLASH_VFX, "flash texture differs");
            this.buildSheets();
        });
        shot("02-neow");
        for (int page = 0; page < 8; page++) {
            sheet(page);
        }
    }

    private void nobFreeze() {
        once("fight Gremlin Nob", () -> console("fight Gremlin_Nob"));
        waitUntil("Nob combat ready", 90, () -> (this.nob = find(GremlinNob.class)) != null && playerTurnReady());
        delay(1.5f);
        once("Nob opens with Bellow", () -> {
            this.turnMark = GameActionManager.turn;
            check("Nob intends Bellow (move 3, buff)", this.nob.nextMove == 3 && this.nob.intent == AbstractMonster.Intent.BUFF, describe(this.nob));
            play("roxymod:RoxyBeginnerChant", null);
        });
        waitUntil("Beginner Chant resolves", 30, this::playerTurnReady);
        once("play Frost Nova on Nob", () -> {
            check("Beginner Chant gives chant and Continuation",
                ChantHelper.amount(AbstractDungeon.player) > 0 && ContinuationHelper.has(AbstractDungeon.player), powers(AbstractDungeon.player));
            play("roxymod:RoxyFrostNova", this.nob);
        });
        waitUntil("Frost Nova resolves", 30, this::playerTurnReady);
        once("top Nob up to the counter", () -> {
            int freeze = amount(this.nob, FreezePower.POWER_ID);
            check("Frost Nova freezes Nob", freeze > 0, powers(this.nob));
            check("Frost Nova used Continuation", !ContinuationHelper.has(AbstractDungeon.player), powers(AbstractDungeon.player));
            if (freeze < 3) {
                AbstractDungeon.actionManager.addToBottom(new ApplyFreezeAction(this.nob, AbstractDungeon.player, 3 - freeze));
            }
        });
        waitUntil("freeze lands", 30, this::playerTurnReady);
        delay(1.5f);
        once("frozen Nob shows a stun intent", () -> check("frozen Nob shows stun, Bellow still pending",
            FreezePower.isFrozen(this.nob) && this.nob.intent == STUN && this.nob.nextMove == 3, describe(this.nob)));
        shot("11-nob-frozen");
        endTurn();
        waitUntil("Nob turn 2", 90, () -> GameActionManager.turn == this.turnMark + 1 && playerTurnReady());
        delay(1.5f);
        once("Bellow was delayed", () -> {
            check("Nob skipped its turn (no Anger yet)", !this.nob.hasPower(AngerPower.POWER_ID), powers(this.nob));
            check("Bellow still pending after the skip", this.nob.nextMove == 3 && this.nob.intent == AbstractMonster.Intent.BUFF, describe(this.nob));
            check("freeze spent and Nob's counter is 4", !this.nob.hasPower(FreezePower.POWER_ID) && FreezePower.getCounter(this.nob) == 4,
                powers(this.nob) + " counter=" + FreezePower.getCounter(this.nob));
        });
        shot("12-nob-bellow-pending");
        endTurn();
        waitUntil("Nob turn 3", 90, () -> GameActionManager.turn == this.turnMark + 2 && playerTurnReady());
        delay(1.5f);
        once("Bellow happens a turn late", () -> check("Nob Bellowed on its next turn (has Anger)", this.nob.hasPower(AngerPower.POWER_ID), powers(this.nob)));
        shot("13-nob-after-bellow");
        killAndWaitForRewards("Nob");
        delay(2);
        shot("14-combat-reward");
    }

    private void hexaghostFreeze() {
        once("fight Hexaghost", () -> console("fight Hexaghost"));
        waitUntil("Hexaghost combat ready", 90, () -> (this.hexaghost = find(Hexaghost.class)) != null && playerTurnReady());
        delay(1.5f);
        once("freeze Hexaghost before Activate", () -> {
            this.turnMark = GameActionManager.turn;
            check("Hexaghost intends Activate (move 5, unknown)", this.hexaghost.nextMove == 5
                && this.hexaghost.intent == AbstractMonster.Intent.UNKNOWN, describe(this.hexaghost));
            AbstractDungeon.actionManager.addToBottom(new ApplyFreezeAction(this.hexaghost, AbstractDungeon.player, 3));
        });
        waitUntil("freeze lands on Hexaghost", 30, this::playerTurnReady);
        delay(1.5f);
        once("frozen Hexaghost shows a stun intent", () -> check("frozen Hexaghost shows stun, Activate still pending",
            this.hexaghost.intent == STUN && this.hexaghost.nextMove == 5, describe(this.hexaghost)));
        shot("21-hexaghost-frozen");
        endTurn();
        waitUntil("Hexaghost turn 2", 90, () -> GameActionManager.turn == this.turnMark + 1 && playerTurnReady());
        delay(1.5f);
        once("Activate was delayed", () -> check("Activate still pending after the skip", this.hexaghost.nextMove == 5
            && this.hexaghost.intent == AbstractMonster.Intent.UNKNOWN, describe(this.hexaghost)));
        endTurn();
        waitUntil("Hexaghost turn 3", 90, () -> GameActionManager.turn == this.turnMark + 2 && playerTurnReady());
        delay(1.5f);
        once("Divider is set up", () -> check("Hexaghost now intends Divider with real damage", this.hexaghost.nextMove == 1
            && this.hexaghost.intent == AbstractMonster.Intent.ATTACK && this.hexaghost.getIntentBaseDmg() > 0, describe(this.hexaghost)));
        shot("22-hexaghost-divider");
        killAndWaitForRewards("Hexaghost");
    }

    /** The enlarged card view, with its keyword tooltips. */
    private void cardPopup() {
        once("open the large card view", () -> {
            AbstractCard card = CardLibrary.getCard("roxymod:RoxyIciclePrison").makeCopy();
            CardCrawlGame.cardPopup.open(card);
        });
        delay(1.5f);
        once("large card view is open", () -> check("large card view opens for a Roxy card", CardCrawlGame.cardPopup.isOpen, "popup closed"));
        shot("30-card-popup");
        once("close the large card view", () -> CardCrawlGame.cardPopup.close());
        delay(0.5f);
    }

    /** Lightning's numbers under Weak, Vulnerable, Wet and Intangible; full-hand card moves; Review. */
    private void lightningAndFullHand() {
        once("fight Gremlin Nob again", () -> console("fight Gremlin_Nob"));
        waitUntil("second Nob combat ready", 90, () -> (this.nob = find(GremlinNob.class)) != null && playerTurnReady());
        delay(1);
        once("Weak, Vulnerable and 2 Wet", () -> {
            AbstractPlayer p = AbstractDungeon.player;
            applyPower(p, new ChantPower(p, 6), 6);
            applyPower(p, new WeakPower(p, 2, false), 2);
            applyPower(this.nob, new VulnerablePower(this.nob, 2, false), 2);
            applyPower(this.nob, new WetPower(this.nob, 2), 2);
        });
        waitUntil("debuffs land", 30, this::playerTurnReady);
        once("Lightning under Weak, Vulnerable and Wet", () -> {
            this.lightning = handCard("roxymod:RoxyLightning");
            this.lightning.calculateCardDamage(this.nob);
            this.expected = (int) Math.floor((24 + 5 * 2) * 0.75f * 1.5f);
            check("Lightning preview: (24 + 5 x 2 Wet) x 0.75 Weak x 1.5 Vulnerable = 38",
                this.lightning.damage == this.expected, "preview " + this.lightning.damage);
            this.hpBefore = this.nob.currentHealth;
            queue(this.lightning, this.nob);
        });
        delay(0.5f);
        shot("31-lightning");
        waitUntil("Lightning resolves", 30, this::playerTurnReady);
        once("Lightning damage dealt", () -> {
            check("Lightning deals the previewed 38", this.hpBefore - this.nob.currentHealth == this.expected,
                "dealt " + (this.hpBefore - this.nob.currentHealth));
            check("Lightning consumed all Wet", !this.nob.hasPower(WetPower.POWER_ID), powers(this.nob));
            applyPower(this.nob, new IntangiblePower(this.nob, 1), 1);
            applyPower(this.nob, new WetPower(this.nob, 1), 1);
        });
        waitUntil("Intangible lands", 30, this::playerTurnReady);
        once("Lightning into Intangible", () -> {
            this.lightning = handCard("roxymod:RoxyLightning");
            this.lightning.calculateCardDamage(this.nob);
            check("Lightning preview against Intangible is 1", this.lightning.damage == 1, "preview " + this.lightning.damage);
            this.hpBefore = this.nob.currentHealth;
            queue(this.lightning, this.nob);
        });
        waitUntil("Lightning into Intangible resolves", 30, this::playerTurnReady);
        once("Intangible caps Lightning", () -> check("Lightning deals 1 through Intangible",
            this.hpBefore - this.nob.currentHealth == 1, "dealt " + (this.hpBefore - this.nob.currentHealth)));

        once("fill the hand, seed the piles", () -> {
            AbstractPlayer p = AbstractDungeon.player;
            while (p.hand.size() < BaseMod.MAX_HAND_SIZE) {
                p.hand.addToHand(CardLibrary.getCard("roxymod:RoxyDefend").makeCopy());
            }
            p.hand.refreshHandLayout();
            this.drawAttack = CardLibrary.getCard("roxymod:RoxyIciclePrison").makeCopy();
            p.drawPile.addToTop(this.drawAttack);
            this.discardAttack = CardLibrary.getCard("roxymod:RoxyIciclePrison").makeCopy();
            p.discardPile.addToTop(this.discardAttack);
            queueFromLimbo("roxymod:RoxyLabyrinthMap", null);
        });
        waitUntil("Labyrinth Map resolves on a full hand", 30, this::playerTurnReady);
        delay(0.5f);
        shot("32-full-hand");
        once("full hand: Labyrinth Map moves nothing", () -> {
            AbstractPlayer p = AbstractDungeon.player;
            check("full hand: Labyrinth Map leaves the attack in the draw pile",
                p.hand.size() == BaseMod.MAX_HAND_SIZE && p.drawPile.contains(this.drawAttack),
                "hand " + p.hand.size() + ", in draw pile " + p.drawPile.contains(this.drawAttack));
            queueFromLimbo("roxymod:RoxyRecast", null);
        });
        waitUntil("Recast resolves on a full hand", 30, this::playerTurnReady);
        once("full hand: Recast moves nothing", () -> {
            AbstractPlayer p = AbstractDungeon.player;
            check("full hand: Recast leaves the discard attack and its cost alone",
                p.hand.size() == BaseMod.MAX_HAND_SIZE && p.discardPile.contains(this.discardAttack)
                    && this.discardAttack.costForTurn == this.discardAttack.cost,
                "hand " + p.hand.size() + ", cost " + this.discardAttack.costForTurn + "/" + this.discardAttack.cost);
            for (int i = 0; i < 3; i++) {
                p.hand.moveToDiscardPile(p.hand.getTopCard());
            }
            this.handBefore = new ArrayList<>(p.hand.group);
            queueFromLimbo("roxymod:RoxyRecast", null);
        });
        waitUntil("second Recast resolves", 30, this::playerTurnReady);
        once("Review lowers the recovered attack's cost", () -> {
            AbstractCard moved = null;
            for (AbstractCard c : AbstractDungeon.player.hand.group) {
                if (!this.handBefore.contains(c) && c.type == AbstractCard.CardType.ATTACK) {
                    moved = c;
                }
            }
            check("Recast's Review: the recovered attack costs 1 less this turn",
                moved != null && moved.costForTurn == Math.max(0, moved.cost - 1),
                moved == null ? "no attack recovered" : moved.cardID + " cost " + moved.costForTurn + "/" + moved.cost);
        });
        killAndWaitForRewards("second Nob");
    }

    /** Several monsters frozen at once all skip, keep their moves, then act. */
    private void gremlinLeaderFreeze() {
        once("fight Gremlin Leader", () -> console("fight Gremlin_Leader"));
        waitUntil("Gremlin Leader combat ready", 90, () -> find(GremlinLeader.class) != null && playerTurnReady());
        delay(1);
        once("freeze every monster", () -> {
            this.turnMark = GameActionManager.turn;
            this.group = aliveMonsters();
            this.movesBefore.clear();
            this.intentsBefore.clear();
            this.historyBefore.clear();
            for (AbstractMonster m : this.group) {
                this.movesBefore.add(m.nextMove);
                this.intentsBefore.add(m.intent);
                this.historyBefore.add(m.moveHistory.size());
                AbstractDungeon.actionManager.addToBottom(new ApplyFreezeAction(m, AbstractDungeon.player, 3));
            }
        });
        waitUntil("freeze lands on the group", 30, this::playerTurnReady);
        delay(1.5f);
        once("the whole group shows stun", () -> {
            boolean allStunned = true;
            for (AbstractMonster m : this.group) {
                allStunned &= m.intent == STUN;
            }
            check("every frozen monster shows a stun intent (" + this.group.size() + " monsters)", allStunned, describeAll(this.group));
            this.hpBefore = AbstractDungeon.player.currentHealth;
        });
        shot("33-gremlin-leader-frozen");
        endTurn();
        waitUntil("Gremlin Leader turn 2", 90, () -> GameActionManager.turn == this.turnMark + 1 && playerTurnReady());
        delay(1);
        once("the group skipped together", () -> {
            boolean kept = true;
            for (int i = 0; i < this.group.size(); i++) {
                AbstractMonster m = this.group.get(i);
                kept &= m.nextMove == this.movesBefore.get(i) && m.intent == this.intentsBefore.get(i)
                    && m.moveHistory.size() == this.historyBefore.get(i);
            }
            check("every frozen monster skipped and kept its pending move", kept, describeAll(this.group));
            check("no frozen monster hurt Roxy or summoned", AbstractDungeon.player.currentHealth == this.hpBefore
                && aliveMonsters().size() == this.group.size(), "hp " + this.hpBefore + " -> " + AbstractDungeon.player.currentHealth
                + ", monsters " + aliveMonsters().size());
        });
        endTurn();
        waitUntil("Gremlin Leader turn 3", 90, () -> GameActionManager.turn == this.turnMark + 2 && playerTurnReady());
        once("the group acted a turn later", () -> {
            boolean acted = true;
            for (int i = 0; i < this.group.size(); i++) {
                AbstractMonster m = this.group.get(i);
                acted &= m.isDeadOrEscaped() || m.moveHistory.size() > this.historyBefore.get(i);
            }
            check("every monster took its delayed turn", acted, describeAll(this.group));
        });
        killAndWaitForRewards("Gremlin Leader");
    }

    /** A split triggered while frozen is hidden behind the stun, then still happens. */
    private void slimeBossSplit() {
        once("fight Slime Boss", () -> console("fight Slime_Boss"));
        waitUntil("Slime Boss combat ready", 90, () -> (this.slimeBoss = find(SlimeBoss.class)) != null && playerTurnReady());
        delay(1);
        once("freeze Slime Boss", () -> {
            this.turnMark = GameActionManager.turn;
            AbstractDungeon.actionManager.addToBottom(new ApplyFreezeAction(this.slimeBoss, AbstractDungeon.player, 3));
        });
        waitUntil("freeze lands on Slime Boss", 30, this::playerTurnReady);
        once("knock Slime Boss under half health", () -> {
            int damage = this.slimeBoss.currentHealth - this.slimeBoss.maxHealth / 2 + 1;
            AbstractDungeon.actionManager.addToBottom(new DamageAction(this.slimeBoss,
                new DamageInfo(AbstractDungeon.player, damage, DamageInfo.DamageType.HP_LOSS)));
        });
        waitUntil("damage resolves", 30, this::playerTurnReady);
        delay(1.5f);
        once("split is pending behind the stun", () -> check("frozen Slime Boss hides its pending split behind stun",
            this.slimeBoss.nextMove == 3 && this.slimeBoss.intent == STUN, describe(this.slimeBoss)));
        shot("34-slime-boss-split-pending");
        endTurn();
        waitUntil("Slime Boss turn 2", 90, () -> GameActionManager.turn == this.turnMark + 1 && playerTurnReady());
        once("split delayed, not lost", () -> check("Slime Boss skipped and now shows its split",
            !this.slimeBoss.isDying && this.slimeBoss.nextMove == 3 && this.slimeBoss.intent == AbstractMonster.Intent.UNKNOWN
                && aliveMonsters().size() == 1, describe(this.slimeBoss)));
        endTurn();
        waitUntil("Slime Boss turn 3", 90, () -> GameActionManager.turn == this.turnMark + 2 && playerTurnReady());
        delay(1);
        once("Slime Boss split a turn late", () -> check("Slime Boss split into two large slimes on its next turn",
            find(SpikeSlime_L.class) != null && find(AcidSlime_L.class) != null, describeAll(aliveMonsters())));
        shot("35-slime-boss-split");
        killAndWaitForRewards("slimes");
    }

    /** Every Roxy relic, then the boss crystal chosen on the real boss relic screen. */
    private void relicsAndBossChoice() {
        once("obtain the other Roxy relics", () -> {
            for (String id : RELICS) {
                console("relic add " + id);
            }
        });
        delay(2);
        once("go to a boss treasure room", () -> goToRoom(new TreasureRoomBoss()));
        waitUntil("in the boss treasure room", 60, () -> currentRoom() instanceof TreasureRoomBoss
            && !AbstractDungeon.isFadingOut && AbstractDungeon.screen == AbstractDungeon.CurrentScreen.NONE);
        delay(1);
        once("open the boss relic choice", () -> {
            ArrayList<AbstractRelic> choice = new ArrayList<>();
            this.bossCrystal = RelicLibrary.getRelic("roxymod:RoxyManaCrystalCore").makeCopy();
            choice.add(this.bossCrystal);
            choice.add(RelicLibrary.getRelic("Black Star").makeCopy());
            choice.add(RelicLibrary.getRelic("Busted Crown").makeCopy());
            AbstractDungeon.bossRelicScreen.open(choice);
        });
        delay(2);
        shot("40-boss-relic-choice");
        once("pick the boss crystal", () -> this.bossCrystal.bossObtainLogic());
        waitUntil("boss relic taken", 30, () -> AbstractDungeon.screen != AbstractDungeon.CurrentScreen.BOSS_REWARD
            && AbstractDungeon.player.hasRelic("roxymod:RoxyManaCrystalCore"));
        delay(2);
        once("relic bar", () -> {
            boolean allKept = true;
            for (String id : RELICS) {
                allKept &= AbstractDungeon.player.hasRelic(id);
            }
            check("boss crystal took the starter's slot and every other relic was kept",
                allKept && !AbstractDungeon.player.hasRelic("roxymod:RoxyManaCrystal") && roxyRelicCount() == 9
                    && AbstractDungeon.player.relics.get(0).relicId.equals("roxymod:RoxyManaCrystalCore"),
                roxyRelicCount() + " Roxy relics: " + relicIds());
        });
        shot("41-relic-bar");
    }

    /** Water Sigil against Artifact, Wet with Icicle Prison, and the Water Barrier potion. */
    private void sentriesArtifactAndPotion() {
        once("fight 3 Sentries", () -> console("fight 3_Sentries"));
        waitUntil("Sentries combat ready", 90, () -> find(Sentry.class) != null && playerTurnReady());
        delay(1);
        once("Water Sigil met Artifact", () -> {
            this.group = aliveMonsters();
            boolean blocked = true;
            for (AbstractMonster m : this.group) {
                blocked &= !m.hasPower(FreezePower.POWER_ID) && !m.hasPower("Artifact");
            }
            check("Water Sigil's battle-start freeze was spent on each Sentry's Artifact", blocked, describeAll(this.group));
            AbstractMonster target = this.group.get(0);
            applyPower(target, new WetPower(target, 1), 1);
        });
        waitUntil("Wet lands", 30, this::playerTurnReady);
        once("Icicle Prison on a Wet Sentry", () -> {
            AbstractMonster target = this.group.get(0);
            this.expected = 1 + (ChantHelper.amount(AbstractDungeon.player) > 0 ? 1 : 0) + 1;
            this.freezeBefore = amount(target, FreezePower.POWER_ID);
            queue(handCard("roxymod:RoxyIciclePrison"), target);
        });
        waitUntil("Icicle Prison resolves", 30, this::playerTurnReady);
        once("Wet added one freeze", () -> {
            AbstractMonster target = this.group.get(0);
            check("Icicle Prison + Wet: " + this.expected + " freeze and the Wet is spent",
                amount(target, FreezePower.POWER_ID) - this.freezeBefore == this.expected && !target.hasPower(WetPower.POWER_ID),
                describe(target));
            AbstractPlayer p = AbstractDungeon.player;
            this.potion = new RoxyWaterBarrierPotion();
            p.obtainPotion(this.potion);
            this.chantBefore = ChantHelper.amount(p);
            this.blockBefore = p.currentBlock;
            this.freezesBefore.clear();
            for (AbstractMonster m : this.group) {
                this.freezesBefore.add(amount(m, FreezePower.POWER_ID));
            }
            // What drinking from the potion bar does.
            this.potion.use(null);
            for (AbstractRelic r : p.relics) {
                r.onUsePotion();
            }
            AbstractDungeon.topPanel.destroyPotion(this.potion.slot);
        });
        waitUntil("potion resolves", 30, this::playerTurnReady);
        delay(1);
        once("Water Barrier potion", () -> {
            AbstractPlayer p = AbstractDungeon.player;
            boolean froze = true;
            for (int i = 0; i < this.group.size(); i++) {
                AbstractMonster m = this.group.get(i);
                froze &= m.isDeadOrEscaped() || amount(m, FreezePower.POWER_ID) - this.freezesBefore.get(i) == 2;
            }
            check("Water Barrier: 8 block and 2 freeze on every enemy", p.currentBlock - this.blockBefore == 8 && froze,
                "block +" + (p.currentBlock - this.blockBefore) + " " + describeAll(this.group));
            check("Milis Charm: drinking a potion gives 2 chant", ChantHelper.amount(p) - this.chantBefore == 2,
                "chant " + this.chantBefore + " -> " + ChantHelper.amount(p));
        });
        shot("42-sentries");
        killAndWaitForRewards("Sentries");
    }

    private void restSite() {
        once("go to a rest site", () -> goToRoom(new RestRoom()));
        waitUntil("at the rest site", 60, () -> currentRoom() instanceof RestRoom
            && ((RestRoom) currentRoom()).campfireUI != null && !AbstractDungeon.isFadingOut);
        delay(3);
        shot("43-rest-site");
    }

    private void treasureChest() {
        once("go to a treasure room", () -> goToRoom(new TreasureRoom()));
        waitUntil("at the treasure room", 60, () -> currentRoom() instanceof TreasureRoom
            && ((TreasureRoom) currentRoom()).chest != null && !AbstractDungeon.isFadingOut);
        delay(1);
        once("open the chest", () -> ((TreasureRoom) currentRoom()).chest.open(false));
        // open() hands out the rewards; the chest's own isOpen flag is only set by a click.
        waitUntil("chest rewards shown", 30, () -> AbstractDungeon.screen == AbstractDungeon.CurrentScreen.COMBAT_REWARD);
        delay(2);
        shot("44-chest");
    }

    /** Save and Quit mid-run, then Continue: deck, relics, potions, gold and health survive. */
    private void saveAndContinue() {
        // Console fights started from Neow sit on map row -1, and the game cannot reload a save
        // made there. Enter this fight at a real first-row map position, as a player would.
        once("enter a fight on the map's first row (entering autosaves)", () -> {
            MapRoomNode real = null;
            for (MapRoomNode n : AbstractDungeon.map.get(0)) {
                if (real == null && n.hasEdges()) {
                    real = n;
                }
            }
            AbstractDungeon.monsterList.add(0, "Jaw Worm");
            MapRoomNode node = new MapRoomNode(real.x, real.y);
            node.room = new MonsterRoom();
            for (MapEdge edge : real.getEdges()) {
                node.addEdge(edge);
            }
            AbstractDungeon.nextRoom = node;
            AbstractDungeon.nextRoomTransitionStart();
        });
        waitUntil("Jaw Worm combat ready", 90, () -> find(JawWorm.class) != null && playerTurnReady());
        delay(1);
        once("save and quit", () -> {
            check("entering the room wrote a save", SaveAndContinue.saveExistsAndNotCorrupted(AbstractDungeon.player), "no save");
            this.runBefore = runSnapshot();
            ConfirmPopup exit = AbstractDungeon.settingsScreen.exitPopup;
            exit.type = ConfirmPopup.ConfirmType.EXIT;
            try {
                Method yes = ConfirmPopup.class.getDeclaredMethod("yesButtonEffect");
                yes.setAccessible(true);
                yes.invoke(exit);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }
        });
        waitUntil("back at the main menu", 60, () -> CardCrawlGame.mode == CardCrawlGame.GameMode.CHAR_SELECT
            && CardCrawlGame.mainMenuScreen != null && !CardCrawlGame.mainMenuScreen.isFadingOut);
        delay(2);
        once("continue the run", () -> {
            // What the main menu's Continue button does.
            CardCrawlGame.loadingSave = true;
            CardCrawlGame.chosenCharacter = CardCrawlGame.characterManager.loadChosenCharacter().chosenClass;
            CardCrawlGame.mainMenuScreen.isFadingOut = true;
            CardCrawlGame.mainMenuScreen.fadeOutMusic();
            Settings.isDailyRun = false;
            Settings.isTrial = false;
            ModHelper.setModsFalse();
        });
        waitUntil("run resumed in a fight", 120, () -> CardCrawlGame.mode == CardCrawlGame.GameMode.GAMEPLAY
            && AbstractDungeon.player != null && currentRoom() instanceof MonsterRoom && playerTurnReady());
        delay(2);
        once("run state survived", () -> {
            String after = runSnapshot();
            check("the reloaded fight is the saved Jaw Worm fight", find(JawWorm.class) != null, describeAll(aliveMonsters()));
            check("deck, relics, potions, gold and health survive Save and Continue", after.equals(this.runBefore),
                "before: " + this.runBefore + " | after: " + after);
            check("still Roxy with her energy orb after loading", AbstractDungeon.player.chosenClass == RoxyEnums.ROXY
                && AbstractDungeon.player.getEnergyImage() == ImageMaster.BLUE_ORB_FLASH_VFX, String.valueOf(AbstractDungeon.player.chosenClass));
        });
        shot("45-after-continue");
    }

    private void death() {
        once("die", () -> console("kill self"));
        waitUntil("death screen", 60, () -> AbstractDungeon.screen == AbstractDungeon.CurrentScreen.DEATH);
        delay(3);
        shot("46-death");
    }

    // Task helpers.

    private void add(String name, float timeout, Step step) {
        this.tasks.add(new Task(name, timeout, step));
    }

    private void once(String name, Runnable action) {
        add(name, 5, () -> {
            action.run();
            return true;
        });
    }

    private void delay(float seconds) {
        add("wait " + seconds + "s", seconds + 5, () -> this.taskTime >= seconds);
    }

    private void waitUntil(String name, float timeout, BooleanSupplier condition) {
        add(name, timeout, () -> {
            if (condition.getAsBoolean()) {
                log("ok    " + name + " (" + String.format("%.1f", this.taskTime) + "s)");
                return true;
            }
            return false;
        });
    }

    private void shot(String name) {
        shot(name, null);
    }

    private void shot(String name, List<AbstractCard> sheet) {
        add("screenshot " + name, 10, () -> {
            if (this.shotTaken) {
                this.shotTaken = false;
                return true;
            }
            if (this.pendingShot == null) {
                this.pendingShot = name;
                this.pendingSheet = sheet;
            }
            return false;
        });
    }

    private void sheet(int page) {
        add("card sheet " + page, 10, () -> {
            if (page >= this.sheets.size()) {
                return true;
            }
            if (this.shotTaken) {
                this.shotTaken = false;
                return true;
            }
            if (this.pendingShot == null) {
                this.pendingShot = String.format("%02d-cards-%s-%d", 3 + page, page < 4 ? "base" : "upgraded", page % 4 + 1);
                this.pendingSheet = this.sheets.get(page);
            }
            return false;
        });
    }

    private void endTurn() {
        // What a click on End Turn does once the button is usable.
        once("end turn", () -> AbstractDungeon.overlayMenu.endTurnButton.disable(true));
    }

    private void check(String name, boolean ok, String detail) {
        if (ok) {
            this.passes++;
            log("PASS  " + name);
        } else {
            this.failures++;
            log("FAIL  " + name + " -- " + detail);
        }
    }

    private void log(String line) {
        String stamped = String.format("[%6.1fs] %s", this.totalTime, line);
        this.report.add(stamped);
        System.out.println("[RoxySmoke] " + stamped);
    }

    @Override
    public void receivePostUpdate() {
        if (this.finished) {
            return;
        }
        float dt = Gdx.graphics.getDeltaTime();
        this.totalTime += dt;
        try {
            if (this.totalTime > TOTAL_TIMEOUT) {
                this.failures++;
                log("FAIL  whole run timed out at: " + (this.current == null ? "?" : this.current.name));
                finish();
                return;
            }
            if (this.current == null) {
                this.current = this.tasks.poll();
                this.taskTime = 0;
                if (this.current == null) {
                    finish();
                    return;
                }
            }
            this.taskTime += dt;
            if (this.current.step.tick()) {
                this.current = null;
            } else if (this.taskTime > this.current.timeout) {
                // A wait that never finished leaves the game in an unknown state, so stop here.
                this.failures++;
                log("FAIL  " + this.current.name + " timed out after " + this.current.timeout + "s -- " + state());
                finish();
            }
        } catch (Throwable t) {
            this.failures++;
            StringWriter trace = new StringWriter();
            t.printStackTrace(new PrintWriter(trace));
            log("FAIL  " + (this.current == null ? "smoke test" : this.current.name) + " threw " + trace);
            finish();
        }
    }

    @Override
    public void receivePostRender(SpriteBatch sb) {
        if (this.pendingShot == null || this.finished) {
            return;
        }
        String name = this.pendingShot;
        try {
            if (this.pendingSheet != null) {
                drawSheet(sb, this.pendingSheet);
            }
            File file = new File(OUT, name + ".png");
            capture(sb, file);
            log("shot  " + file.getName());
        } catch (Throwable t) {
            this.failures++;
            log("FAIL  screenshot " + name + " threw " + t);
        }
        this.pendingShot = null;
        this.pendingSheet = null;
        this.shotTaken = true;
    }

    private void finish() {
        if (this.finished) {
            return;
        }
        this.finished = true;
        List<String> lines = new ArrayList<>();
        lines.add((this.failures == 0 ? "RESULT: PASS" : "RESULT: FAIL") + " (" + this.passes + " passed, " + this.failures + " failed)");
        lines.addAll(this.report);
        try {
            Files.write(new File(OUT, "smoke-report.txt").toPath(), lines, StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.out.println("[RoxySmoke] could not write report: " + e);
        }
        System.out.println("[RoxySmoke] " + lines.get(0));
        Gdx.app.exit();
    }

    // Game helpers.

    /** The current room, or null while a run is loading and there is no map node yet. */
    private static AbstractRoom currentRoom() {
        return AbstractDungeon.currMapNode == null ? null : AbstractDungeon.getCurrRoom();
    }

    private boolean playerTurnReady() {
        AbstractRoom room = currentRoom();
        return room != null && room.phase == AbstractRoom.RoomPhase.COMBAT
            && !AbstractDungeon.actionManager.turnHasEnded
            && AbstractDungeon.actionManager.phase == GameActionManager.Phase.WAITING_ON_USER
            && AbstractDungeon.actionManager.actions.isEmpty()
            && AbstractDungeon.actionManager.cardQueue.isEmpty()
            && AbstractDungeon.overlayMenu.endTurnButton.enabled
            && !AbstractDungeon.isScreenUp;
    }

    private static <T extends AbstractMonster> T find(Class<T> type) {
        AbstractRoom room = currentRoom();
        if (!(room instanceof MonsterRoom) || room.monsters == null) {
            return null;
        }
        for (AbstractMonster m : room.monsters.monsters) {
            if (type.isInstance(m) && !m.isDeadOrEscaped()) {
                return type.cast(m);
            }
        }
        return null;
    }

    private static void play(String cardId, AbstractMonster target) {
        queue(handCard(cardId), target);
    }

    /** A fresh copy of the card, put into the hand. */
    private static AbstractCard handCard(String cardId) {
        AbstractCard card = CardLibrary.getCard(cardId).makeCopy();
        AbstractDungeon.player.hand.addToHand(card);
        AbstractDungeon.player.hand.refreshHandLayout();
        card.applyPowers();
        return card;
    }

    /** Plays the card through the game's card queue, the way Havoc-style autoplay does. */
    private static void queue(AbstractCard card, AbstractMonster target) {
        AbstractDungeon.actionManager.cardQueue.add(new CardQueueItem(card, target, card.energyOnUse, true, true));
    }

    /** Plays a card that is not in the hand, so a full hand stays full while it resolves. */
    private static void queueFromLimbo(String cardId, AbstractMonster target) {
        AbstractCard card = CardLibrary.getCard(cardId).makeCopy();
        card.current_x = card.target_x = Settings.WIDTH / 2f;
        card.current_y = card.target_y = Settings.HEIGHT / 2f;
        AbstractDungeon.player.limbo.addToTop(card);
        queue(card, target);
    }

    private static void applyPower(AbstractCreature target, AbstractPower power, int amount) {
        AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(target, AbstractDungeon.player, power, amount));
    }

    private void killAndWaitForRewards(String label) {
        once("kill the " + label, () -> console("kill all"));
        waitUntil(label + " reward screen", 60, () -> AbstractDungeon.screen == AbstractDungeon.CurrentScreen.COMBAT_REWARD);
    }

    /** What the console's fight command does, for any room type. */
    private static void goToRoom(AbstractRoom room) {
        MapRoomNode current = AbstractDungeon.currMapNode;
        MapRoomNode node = new MapRoomNode(current.x, current.y);
        node.room = room;
        for (MapEdge edge : current.getEdges()) {
            node.addEdge(edge);
        }
        AbstractDungeon.nextRoom = node;
        AbstractDungeon.nextRoomTransitionStart();
    }

    private static List<AbstractMonster> aliveMonsters() {
        List<AbstractMonster> alive = new ArrayList<>();
        AbstractRoom room = currentRoom();
        if (room != null && room.monsters != null) {
            for (AbstractMonster m : room.monsters.monsters) {
                if (!m.isDeadOrEscaped()) {
                    alive.add(m);
                }
            }
        }
        return alive;
    }

    private static String describeAll(List<AbstractMonster> monsters) {
        StringBuilder text = new StringBuilder();
        for (AbstractMonster m : monsters) {
            text.append('{').append(describe(m)).append("} ");
        }
        return text.toString().trim();
    }

    /** Deck, relics with counters, potions, gold and health: what a save must carry. */
    private static String runSnapshot() {
        AbstractPlayer p = AbstractDungeon.player;
        StringBuilder text = new StringBuilder("deck[");
        for (AbstractCard c : p.masterDeck.group) {
            text.append(c.cardID).append(c.upgraded ? "+" : "").append(' ');
        }
        text.append("] relics[");
        for (AbstractRelic r : p.relics) {
            text.append(r.relicId).append('=').append(r.counter).append(' ');
        }
        text.append("] potions[");
        for (AbstractPotion potion : p.potions) {
            text.append(potion.ID).append(' ');
        }
        return text.append("] gold=").append(p.gold).append(" hp=").append(p.currentHealth).append('/').append(p.maxHealth)
            .append(" floor=").append(AbstractDungeon.floorNum).toString();
    }

    private static void console(String command) {
        ConsoleCommand.execute(command.split(" "));
    }

    private static int roxyRelicCount() {
        int count = 0;
        for (com.megacrit.cardcrawl.relics.AbstractRelic relic : AbstractDungeon.player.relics) {
            if (relic.relicId.startsWith("roxymod:")) {
                count++;
            }
        }
        return count;
    }

    private static String relicIds() {
        StringBuilder ids = new StringBuilder();
        for (com.megacrit.cardcrawl.relics.AbstractRelic relic : AbstractDungeon.player.relics) {
            ids.append(relic.relicId).append(' ');
        }
        return ids.toString().trim();
    }

    private static int amount(AbstractCreature creature, String powerId) {
        AbstractPower power = creature.getPower(powerId);
        return power == null ? 0 : power.amount;
    }

    private static String describe(AbstractMonster m) {
        return m.id + " nextMove=" + m.nextMove + " intent=" + m.intent + " baseDmg=" + m.getIntentBaseDmg() + " " + powers(m);
    }

    private static String powers(AbstractCreature creature) {
        StringBuilder text = new StringBuilder("powers[");
        for (AbstractPower p : creature.powers) {
            text.append(p.ID).append('=').append(p.amount).append(' ');
        }
        return text.toString().trim() + "]";
    }

    private static String state() {
        AbstractRoom room = CardCrawlGame.mode == CardCrawlGame.GameMode.GAMEPLAY ? currentRoom() : null;
        return "mode=" + CardCrawlGame.mode
            + " room=" + (room == null ? "none" : room.getClass().getSimpleName() + "/" + room.phase)
            + " screen=" + AbstractDungeon.screen
            + " screenUp=" + AbstractDungeon.isScreenUp
            + (AbstractDungeon.actionManager == null ? "" : " phase=" + AbstractDungeon.actionManager.phase
                + " actions=" + AbstractDungeon.actionManager.actions.size()
                + " turnEnded=" + AbstractDungeon.actionManager.turnHasEnded)
            + " turn=" + GameActionManager.turn;
    }

    private static List<AbstractCard> roxyCards() {
        List<AbstractCard> cards = new ArrayList<>();
        for (AbstractCard c : CardLibrary.cards.values()) {
            if (c.color == RoxyEnums.ROXY_COLOR) {
                cards.add(c);
            }
        }
        cards.sort(Comparator.comparing((AbstractCard c) -> c.rarity.ordinal())
            .thenComparing(c -> c.type.ordinal())
            .thenComparing(c -> c.cardID));
        return cards;
    }

    private static CharacterOption roxyOption() {
        for (CharacterOption option : CardCrawlGame.mainMenuScreen.charSelectScreen.options) {
            if (option.c != null && option.c.chosenClass == RoxyEnums.ROXY) {
                return option;
            }
        }
        return null;
    }

    private void buildSheets() {
        List<AbstractCard> cards = roxyCards();
        int perPage = SHEET_COLUMNS * SHEET_ROWS;
        for (boolean upgraded : new boolean[] {false, true}) {
            for (int start = 0; start < cards.size(); start += perPage) {
                List<AbstractCard> page = new ArrayList<>();
                for (AbstractCard original : cards.subList(start, Math.min(cards.size(), start + perPage))) {
                    AbstractCard copy = original.makeCopy();
                    if (upgraded && copy.canUpgrade()) {
                        copy.upgrade();
                    }
                    page.add(copy);
                }
                this.sheets.add(page);
            }
        }
    }

    private static void drawSheet(SpriteBatch sb, List<AbstractCard> cards) {
        sb.setColor(Color.BLACK);
        sb.draw(ImageMaster.WHITE_SQUARE_IMG, 0, 0, Settings.WIDTH, Settings.HEIGHT);
        sb.setColor(Color.WHITE);
        float cellW = Settings.WIDTH / (float) SHEET_COLUMNS;
        float cellH = Settings.HEIGHT / (float) SHEET_ROWS;
        float scale = Math.min(cellW / AbstractCard.IMG_WIDTH, cellH / AbstractCard.IMG_HEIGHT) * 0.94f;
        for (int i = 0; i < cards.size(); i++) {
            AbstractCard c = cards.get(i);
            c.current_x = c.target_x = cellW * (i % SHEET_COLUMNS + 0.5f);
            c.current_y = c.target_y = Settings.HEIGHT - cellH * (i / SHEET_COLUMNS + 0.5f);
            c.drawScale = c.targetDrawScale = scale;
            c.angle = c.targetAngle = 0;
            c.transparency = c.targetTransparency = 1f;
            c.render(sb);
        }
    }

    /** BaseMod draws the frame into a bound framebuffer, so read it before the frame ends. */
    private static void capture(SpriteBatch sb, File file) {
        sb.flush();
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();
        byte[] pixels = ScreenUtils.getFrameBufferPixels(0, 0, width, height, true);
        for (int i = 3; i < pixels.length; i += 4) {
            pixels[i] = (byte) 255;
        }
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        BufferUtils.copy(pixels, 0, pixmap.getPixels(), pixels.length);
        PixmapIO.writePNG(new FileHandle(file), pixmap);
        pixmap.dispose();
    }
}
