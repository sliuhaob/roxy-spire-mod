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
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardQueueItem;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.GremlinNob;
import com.megacrit.cardcrawl.monsters.exordium.Hexaghost;
import com.megacrit.cardcrawl.neow.NeowRoom;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.AngerPower;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.rooms.MonsterRoom;
import com.megacrit.cardcrawl.screens.charSelect.CharacterOption;
import com.megacrit.cardcrawl.screens.charSelect.CharacterSelectScreen;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BooleanSupplier;
import roxymod.RoxyEnums;
import roxymod.actions.ApplyFreezeAction;
import roxymod.powers.FreezePower;
import roxymod.util.ChantHelper;
import roxymod.util.ContinuationHelper;

/**
 * Dev-only in-game smoke test, loaded only by tools/smoke-test.ps1. That script runs the game
 * from a sandbox folder, so your real profile, saves and mods folder are never touched.
 *
 * It drives the real game: picks Roxy on the character select screen, embarks on a fixed seed,
 * draws every card face (base and upgraded), then fights Gremlin Nob and Hexaghost with Freeze
 * and checks that each frozen scripted move is delayed rather than lost. Screenshots and
 * smoke-report.txt go to -Droxy.smoke.out, then the game exits.
 */
@SpireInitializer
public class RoxySmokeTest implements PostUpdateSubscriber, PostRenderSubscriber {
    private static final File OUT = new File(System.getProperty("roxy.smoke.out", "roxy-smoke"));
    private static final long SEED = 31415926L;
    private static final float TOTAL_TIMEOUT = 480f;
    private static final int SHEET_COLUMNS = 7;
    private static final int SHEET_ROWS = 3;
    private static final AbstractMonster.Intent STUN = AbstractMonster.Intent.STUN;
    private static final String[] RELICS = {
        "roxymod:RoxyChantPrimer", "roxymod:RoxyTeachingPointer", "roxymod:RoxyHydraulicSeal",
        "roxymod:RoxyTravelFlask", "roxymod:RoxyManaCrystalCore", "roxymod:RoxyWaterSigilRelic",
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
    private int turnMark;

    public static void initialize() {
        BaseMod.subscribe(new RoxySmokeTest());
    }

    public RoxySmokeTest() {
        OUT.mkdirs();
        this.script();
    }

    private void script() {
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
            && AbstractDungeon.currMapNode != null && AbstractDungeon.getCurrRoom() instanceof NeowRoom);
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
        once("kill Nob", () -> console("kill all"));
        waitUntil("combat reward screen", 60, () -> AbstractDungeon.screen == AbstractDungeon.CurrentScreen.COMBAT_REWARD);
        delay(2);
        shot("14-combat-reward");

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
        once("kill Hexaghost", () -> console("kill all"));
        waitUntil("Hexaghost reward screen", 60, () -> AbstractDungeon.screen == AbstractDungeon.CurrentScreen.COMBAT_REWARD);

        // Last, so battle-start relic effects cannot change the Freeze checks above.
        once("obtain every Roxy relic", () -> {
            for (String id : RELICS) {
                console("relic add " + id);
            }
        });
        delay(2);
        once("relic bar", () -> {
            // The boss relic replaces the starter in its slot; nothing else may be lost.
            boolean allKept = true;
            for (String id : RELICS) {
                allKept &= AbstractDungeon.player.hasRelic(id);
            }
            check("boss crystal replaced the starter and every other relic was kept",
                allKept && !AbstractDungeon.player.hasRelic("roxymod:RoxyManaCrystal") && roxyRelicCount() == 9,
                roxyRelicCount() + " Roxy relics: " + relicIds());
        });
        shot("23-relics");
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

    private boolean playerTurnReady() {
        AbstractRoom room = AbstractDungeon.getCurrRoom();
        return room != null && room.phase == AbstractRoom.RoomPhase.COMBAT
            && !AbstractDungeon.actionManager.turnHasEnded
            && AbstractDungeon.actionManager.phase == GameActionManager.Phase.WAITING_ON_USER
            && AbstractDungeon.actionManager.actions.isEmpty()
            && AbstractDungeon.actionManager.cardQueue.isEmpty()
            && AbstractDungeon.overlayMenu.endTurnButton.enabled
            && !AbstractDungeon.isScreenUp;
    }

    private static <T extends AbstractMonster> T find(Class<T> type) {
        AbstractRoom room = AbstractDungeon.getCurrRoom();
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
        AbstractCard card = CardLibrary.getCard(cardId).makeCopy();
        AbstractDungeon.player.hand.addToHand(card);
        AbstractDungeon.player.hand.refreshHandLayout();
        card.applyPowers();
        AbstractDungeon.actionManager.cardQueue.add(new CardQueueItem(card, target, card.energyOnUse, true, true));
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
        AbstractRoom room = CardCrawlGame.mode == CardCrawlGame.GameMode.GAMEPLAY ? AbstractDungeon.getCurrRoom() : null;
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
