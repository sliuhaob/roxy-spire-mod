import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.google.gson.Gson;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.actions.IntentFlashAction;
import com.megacrit.cardcrawl.actions.utility.WaitAction;
import com.megacrit.cardcrawl.actions.common.*;
import com.megacrit.cardcrawl.cards.*;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.characters.Ironclad;
import com.megacrit.cardcrawl.core.*;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.*;
import com.megacrit.cardcrawl.map.MapRoomNode;
import com.megacrit.cardcrawl.monsters.*;
import com.megacrit.cardcrawl.monsters.beyond.TimeEater;
import com.megacrit.cardcrawl.monsters.exordium.Cultist;
import com.megacrit.cardcrawl.monsters.exordium.GremlinNob;
import com.megacrit.cardcrawl.monsters.exordium.Hexaghost;
import com.megacrit.cardcrawl.powers.*;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rooms.EmptyRoom;
import com.megacrit.cardcrawl.screens.charSelect.CharacterOption;
import com.megacrit.cardcrawl.screens.charSelect.CharacterSelectScreen;
import com.megacrit.cardcrawl.screens.mainMenu.MainMenuScreen;
import com.megacrit.cardcrawl.stances.NeutralStance;
import java.io.*;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import sun.misc.Unsafe;
import roxymod.actions.*;
import roxymod.cards.RoxyLightning;
import roxymod.powers.*;
import roxymod.relics.RoxyChantConsumedRelic;
import roxymod.util.ReviewHelper;

/** Uses the installed game engine. Skips graphical constructors with Unsafe. */
public class MechanicsTest {
    private static Unsafe unsafe;
    private static Ironclad player;
    private static EmptyRoom room;
    private static int checks;

    static class HeadlessTexture extends com.badlogic.gdx.graphics.Texture {
        HeadlessTexture() { super(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888); }
        public int getWidth() { return 1; }
        public int getHeight() { return 1; }
    }

    public static class Monster extends AbstractMonster {
        int turns;
        Monster() { super("test", "test", 100, 0, 0, 1, 1, null); }
        public void takeTurn() { turns++; }
        protected void getMove(int roll) { }
    }
    static class CounterRelic extends AbstractRelic implements RoxyChantConsumedRelic {
        int calls, consumed;
        CounterRelic() { super("test", "test.png", RelicTier.COMMON, LandingSound.FLAT); }
        public void onChantConsumed(int n) { calls++; consumed += n; }
        public String getUpdatedDescription() { return "test"; }
        public AbstractRelic makeCopy() { return this; }
    }
    static class QuietWet extends WetPower {
        QuietWet(AbstractCreature c, int n) { super(c, n); }
        public void flash() { }
    }
    static class QuietDomain extends CrystalDomainPower {
        QuietDomain(AbstractCreature c, int n) { super(c, n); }
        public void flash() { }
    }
    static class QuietZero extends ZeroChantDelayPower {
        QuietZero(AbstractCreature c) { super(c); }
        public void flash() { }
    }
    static class QuietCounter extends WaveCounterPower {
        QuietCounter(AbstractCreature c, int n) { super(c, n); }
        public void flash() { }
    }
    static class QuietFreeze extends FreezePower {
        QuietFreeze(AbstractCreature c, int n) { super(c, n); }
        public void flash() { }
    }
    static TextureAtlas.AtlasRegion roxyOrb, lastRunOrb;
    public static class OrbRoxy extends roxymod.characters.Roxy {
        OrbRoxy() { super("test"); }
        public TextureAtlas.AtlasRegion getOrb() { return roxyOrb; }
    }
    static <T> T bare(Class<T> type) throws Exception { return type.cast(unsafe.allocateInstance(type)); }
    static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
        checks++;
    }
    static void reset() {
        AbstractDungeon.actionManager = new GameActionManager();
        AbstractDungeon.effectList.clear();
        room.monsters.monsters.clear();
        player.powers.clear();
        player.relics.clear();
    }
    static long queued(Class<?> type) {
        return AbstractDungeon.actionManager.actions.stream().filter(type::isInstance).count();
    }
    static Object field(Object obj, String name) throws Exception {
        Class<?> type = obj.getClass();
        while (type != null) {
            try { Field f = type.getDeclaredField(name); f.setAccessible(true); return f.get(obj); }
            catch (NoSuchFieldException ignored) { type = type.getSuperclass(); }
        }
        throw new NoSuchFieldException(name);
    }
    static void setField(Object obj, String name, Object value) throws Exception {
        Class<?> type = obj.getClass();
        while (type != null) {
            try { Field f = type.getDeclaredField(name); f.setAccessible(true); f.set(obj, value); return; }
            catch (NoSuchFieldException ignored) { type = type.getSuperclass(); }
        }
        throw new NoSuchFieldException(name);
    }
    /** A vanilla monster without its animation constructor; the caller sets what the constructor would. */
    static <T extends AbstractMonster> T vanilla(Class<T> type, int hp, int maxHp, int... damage) throws Exception {
        T m = bare(type);
        m.powers = new ArrayList<>(); m.moveHistory = new ArrayList<>(); m.damage = new ArrayList<>();
        for (int base : damage) m.damage.add(new DamageInfo(m, base));
        m.currentHealth = hp; m.maxHealth = maxHp;
        m.hb = new com.megacrit.cardcrawl.helpers.Hitbox(1, 1);
        setField(m, "intentTip", new com.megacrit.cardcrawl.helpers.PowerTip());
        return m;
    }
    static Monster monster() throws Exception {
        Monster m = vanilla(Monster.class, 100, 100);
        m.setMove("Test move", (byte)1, AbstractMonster.Intent.ATTACK, 6);
        m.createIntent();
        return m;
    }
    static AbstractGameAction first(Class<?> type) {
        return AbstractDungeon.actionManager.actions.stream().filter(type::isInstance).findFirst()
            .orElseThrow(() -> new AssertionError("no queued " + type.getSimpleName()));
    }
    static boolean onlyQueued(Class<?>... types) {
        return AbstractDungeon.actionManager.actions.stream()
            .allMatch(a -> Arrays.stream(types).anyMatch(t -> t.isInstance(a)));
    }
    /** Adds freeze the way ApplyPowerAction does: add the power, then onInitialApplication. */
    static QuietFreeze freeze(AbstractMonster m, int amount) {
        QuietFreeze freeze = new QuietFreeze(m, amount);
        m.powers.add(freeze); freeze.onInitialApplication();
        return freeze;
    }
    /** One monster's slot in the enemy turn, through the patched GameActionManager.getNextAction. */
    static void monsterTurn(AbstractMonster m) throws Exception {
        GameActionManager manager = AbstractDungeon.actionManager;
        manager.actions.clear();
        manager.monsterAttacksQueued = true;
        manager.monsterQueue.add(new MonsterQueueItem(m));
        Method next = GameActionManager.class.getDeclaredMethod("getNextAction");
        next.setAccessible(true);
        next.invoke(manager);
    }
    /** Runs the queued freeze reduction (and the removal it may queue) to completion. */
    static void resolveFreezeActions() {
        ArrayList<AbstractGameAction> queue = AbstractDungeon.actionManager.actions;
        for (int guard = 0; guard < 20; guard++) {
            AbstractGameAction next = queue.stream()
                .filter(a -> a instanceof ReducePowerAction || a instanceof RemoveSpecificPowerAction)
                .findFirst().orElse(null);
            if (next == null) return;
            queue.remove(next);
            for (int tick = 0; tick < 200 && !next.isDone; tick++) next.update();
        }
        throw new AssertionError("freeze actions did not settle");
    }
    static void initialize() throws Exception {
        com.badlogic.gdx.Graphics.DisplayMode display = new com.badlogic.gdx.Graphics.DisplayMode(1920, 1080, 60, 32) { };
        com.badlogic.gdx.Gdx.graphics = (com.badlogic.gdx.Graphics)Proxy.newProxyInstance(
            MechanicsTest.class.getClassLoader(), new Class<?>[]{com.badlogic.gdx.Graphics.class},
            (proxy, method, args) -> {
                if (method.getReturnType() == com.badlogic.gdx.Graphics.DisplayMode.class) return display;
                if (method.getReturnType() == com.badlogic.gdx.Graphics.DisplayMode[].class) return new com.badlogic.gdx.Graphics.DisplayMode[]{display};
                if (method.getReturnType() == boolean.class) return false;
                if (method.getReturnType() == int.class) return 1080;
                if (method.getReturnType() == float.class) return 1f / 60;
                if (method.getReturnType() == long.class) return 0L;
                return null;
            });
        com.badlogic.gdx.Gdx.app = (com.badlogic.gdx.Application)Proxy.newProxyInstance(
            MechanicsTest.class.getClassLoader(), new Class<?>[]{com.badlogic.gdx.Application.class},
            (proxy, method, args) -> {
                if (method.getReturnType() == com.badlogic.gdx.Application.ApplicationType.class) return com.badlogic.gdx.Application.ApplicationType.Desktop;
                if (method.getReturnType() == int.class) return 0;
                if (method.getReturnType() == long.class) return 0L;
                return null;
            });
        // Route file probes away from real game preferences and saves.
        com.badlogic.gdx.Gdx.files = (com.badlogic.gdx.Files)Proxy.newProxyInstance(
            MechanicsTest.class.getClassLoader(), new Class<?>[]{com.badlogic.gdx.Files.class},
            (proxy, method, args) -> {
                if (method.getReturnType() == com.badlogic.gdx.files.FileHandle.class)
                    return new com.badlogic.gdx.files.FileHandle("build/headless-state/" + args[0]);
                if (method.getReturnType() == boolean.class) return false;
                return "build/headless-state/";
            });
        Field f = Unsafe.class.getDeclaredField("theUnsafe"); f.setAccessible(true);
        unsafe = (Unsafe)f.get(null);
        Gson gson = new Gson();
        for (Field map : LocalizedStrings.class.getDeclaredFields()) {
            if (Map.class.isAssignableFrom(map.getType())) {
                String name = map.getName().replace("scoreBonuses", "score_bonuses").replace("mod", "run_mods");
                InputStream stream = MechanicsTest.class.getResourceAsStream("/localization/eng/" + name + ".json");
                map.setAccessible(true);
                map.set(null, stream == null ? new HashMap<>() : gson.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), map.getGenericType()));
            }
        }
        Field cards = LocalizedStrings.class.getDeclaredField("cards"); cards.setAccessible(true);
        Map<String, CardStrings> custom = gson.fromJson(new InputStreamReader(new FileInputStream(
            new File(System.getProperty("roxy.projectDir", "."), "src/main/resources/roxymod/localization/zhs/CardStrings.json")), StandardCharsets.UTF_8), cards.getGenericType());
        ((Map<String, CardStrings>)cards.get(null)).putAll(custom);
        CardCrawlGame.languagePack = bare(LocalizedStrings.class);
        HeadlessTexture texture = bare(HeadlessTexture.class);
        for (Field imageField : com.megacrit.cardcrawl.helpers.ImageMaster.class.getDeclaredFields()) {
            if (Modifier.isStatic(imageField.getModifiers()) && imageField.getType() == com.badlogic.gdx.graphics.Texture.class) {
                imageField.setAccessible(true); imageField.set(null, texture);
            }
        }
        AbstractPower.atlas = new TextureAtlas();
        com.badlogic.gdx.graphics.g2d.BitmapFont font = new com.badlogic.gdx.graphics.g2d.BitmapFont(
            new com.badlogic.gdx.graphics.g2d.BitmapFont.BitmapFontData(),
            new com.badlogic.gdx.graphics.g2d.TextureRegion(), false);
        for (Field fontField : com.megacrit.cardcrawl.helpers.FontHelper.class.getDeclaredFields()) {
            if (Modifier.isStatic(fontField.getModifiers()) && fontField.getType() == font.getClass()) {
                fontField.setAccessible(true); fontField.set(null, font);
            }
        }
        Settings.scale = 1;
        Settings.gamePref = new com.megacrit.cardcrawl.helpers.Prefs();
        Settings.soundPref = new com.megacrit.cardcrawl.helpers.Prefs();
        for (Field pref : com.megacrit.cardcrawl.unlock.UnlockTracker.class.getDeclaredFields()) {
            if (Modifier.isStatic(pref.getModifiers()) && pref.getType() == com.megacrit.cardcrawl.helpers.Prefs.class) {
                pref.setAccessible(true); pref.set(null, new com.megacrit.cardcrawl.helpers.Prefs());
            }
        }
        player = bare(Ironclad.class);
        player.powers = new ArrayList<>(); player.relics = new ArrayList<>();
        player.blights = new ArrayList<>(); player.orbs = new ArrayList<>();
        player.hand = new CardGroup(CardGroup.CardGroupType.HAND);
        player.stance = bare(NeutralStance.class);
        player.currentHealth = player.maxHealth = 70;
        AbstractDungeon.player = player;
        MapRoomNode node = bare(MapRoomNode.class);
        room = bare(EmptyRoom.class);
        room.phase = com.megacrit.cardcrawl.rooms.AbstractRoom.RoomPhase.COMBAT;
        room.monsters = new MonsterGroup(new AbstractMonster[0]);
        node.room = room;
        AbstractDungeon.currMapNode = node;
        AbstractDungeon.effectList = new ArrayList<>();
        AbstractDungeon.aiRng = new com.megacrit.cardcrawl.random.Random(7L);
        com.megacrit.cardcrawl.helpers.TipTracker.tips.put("INTENT_TIP", true);
        // SpireFields live on AbstractMonster, added by PatchEmulator like ModTheSpire would.
        int spireFields = PatchEmulator.initializeFields(new File(System.getProperty("roxy.projectDir", "."), "build/classes"));
        check(spireFields == 2, "both freeze SpireFields bound to the emulated AbstractMonster fields");
        reset();
    }
    static void payments() throws Exception {
        for (int available = 0; available <= 6; available++) {
            for (int requested = 0; requested <= 8; requested++) {
                reset();
                ChantPower chant = new ChantPower(player, available);
                player.powers.add(chant);
                CounterRelic relic = bare(CounterRelic.class); player.relics.add(relic);
                new ConsumeChantAction(player, requested).update();
                int paid = Math.min(available, requested);
                check(chant.amount == available - paid, "remaining chant");
                check(relic.calls == (paid > 0 ? 1 : 0) && relic.consumed == paid, "one payment event, including exact/overpayment");
                check(queued(RemoveSpecificPowerAction.class) == (paid > 0 && paid == available ? 1 : 0), "remove exhausted chant only: available=" + available + ", requested=" + requested + ", queued=" + AbstractDungeon.actionManager.actions);
            }
        }
        reset();
        CounterRelic relic = bare(CounterRelic.class); player.relics.add(relic);
        player.powers.add(new ChantPower(player, 4));
        new QuietZero(player).atStartOfTurn();
        check(relic.calls == 0 && queued(RemoveSpecificPowerAction.class) == 2, "loss is not consumption");
        // Design decision: Artifact negates both self-penalties, as it does vanilla Flex's Strength loss.
        check(new ExhaustionPower(player, 1).type == AbstractPower.PowerType.DEBUFF
            && new QuietZero(player).type == AbstractPower.PowerType.DEBUFF, "Artifact blocks Exhaustion and the Zero Chant penalty");
    }
    static void reactions() throws Exception {
        for (int wet = 0; wet <= 3; wet++) {
            for (boolean artifact : new boolean[]{false, true}) {
                reset(); Monster m = monster();
                if (wet > 0) m.powers.add(new QuietWet(m, wet));
                if (artifact) m.powers.add(new ArtifactPower(m, 1));
                new ApplyFreezeAction(m, player, 2).update();
                ApplyPowerAction applied = (ApplyPowerAction)AbstractDungeon.actionManager.actions.stream()
                    .filter(ApplyPowerAction.class::isInstance).findFirst().get();
                AbstractPower freeze = (AbstractPower)field(applied, "powerToApply");
                boolean reacts = wet > 0 && !artifact;
                check(freeze.amount == (reacts ? 3 : 2), "Wet adds precisely one freeze");
                check(queued(ReducePowerAction.class) == (reacts ? 1 : 0), "Artifact preserves Wet");
            }
        }
        reset(); Monster dead = monster(); dead.isDead = true; dead.isDying = true;
        new ApplyFreezeAction(dead, player, 2).update();
        check(AbstractDungeon.actionManager.actions.isEmpty(), "ignore dead freeze target");
        reset(); Monster first = monster(), second = monster();
        QuietFreeze freeze = new QuietFreeze(first, 3);
        check(freeze.shouldSkipTurn(), "three freeze reaches first threshold");
        freeze.onSkipTurn();
        check(FreezePower.getCounter(first) == 4 && FreezePower.getCounter(second) == 3, "freeze thresholds are per monster");
        check(!freeze.shouldSkipTurn(), "three stacks no longer reach second threshold");
        check(queued(ReducePowerAction.class) == 1 && queued(RollMoveAction.class) == 0, "skip consumes freeze and keeps the pending move");
    }
    static void freezeTurns() throws Exception {
        AbstractMonster.Intent stun = AbstractMonster.Intent.STUN, attack = AbstractMonster.Intent.ATTACK;

        reset(); Monster m = monster(); room.monsters.monsters.add(m);
        QuietFreeze f = freeze(m, 2);
        check(m.intent == attack && f.description.contains("#b2 / #b3"), "below the counter: real intent, progress 2/3");
        monsterTurn(m);
        check(m.turns == 1 && f.amount == 2 && "Test move".equals(field(first(ShowMoveNameAction.class), "msg")), "below the counter the turn runs normally");

        reset(); m = monster(); room.monsters.monsters.add(m);
        f = freeze(m, 2); f.stackPower(1);
        check(m.intent == stun && m.nextMove == 1 && m.getIntentBaseDmg() == -1, "stacking to the counter shows a stun intent");
        m.setMove("Changed", (byte)7, AbstractMonster.Intent.ATTACK_DEFEND, 10); m.createIntent();
        check(m.intent == stun && m.nextMove == 7, "a move changed while frozen stays behind the stun intent");
        monsterTurn(m);
        check(m.turns == 0 && FreezePower.NAME.equals(field(first(ShowMoveNameAction.class), "msg")) && "Changed".equals(m.moveName), "frozen turn is skipped, banner says frozen, move keeps its name");
        check(onlyQueued(ShowMoveNameAction.class, IntentFlashAction.class, ReducePowerAction.class, WaitAction.class), "skip queues no re-roll");
        check(m.intent == stun, "stun intent holds until the freeze is spent");
        resolveFreezeActions();
        check(!m.hasPower(FreezePower.POWER_ID) && FreezePower.getCounter(m) == 4, "skip spends the freeze and raises the counter");
        check(m.intent == AbstractMonster.Intent.ATTACK_DEFEND && m.nextMove == 7 && m.getIntentBaseDmg() == 10, "the latest move resumes, not a stale copy");

        reset(); m = monster(); room.monsters.monsters.add(m);
        f = freeze(m, 7);
        monsterTurn(m); resolveFreezeActions();
        check(m.turns == 0 && f.amount == 4 && FreezePower.getCounter(m) == 4 && m.intent == stun, "7 freeze: first skip leaves 4/4, still stunned");
        monsterTurn(m); resolveFreezeActions();
        check(m.turns == 0 && !m.hasPower(FreezePower.POWER_ID) && FreezePower.getCounter(m) == 5 && m.intent == attack, "7 freeze: second skip spends the rest");
        monsterTurn(m);
        check(m.turns == 1, "acts again once the freeze is spent");

        reset(); m = monster(); room.monsters.monsters.add(m);
        freeze(m, 3);
        AbstractDungeon.actionManager.addToBottom(new RemoveSpecificPowerAction(m, m, FreezePower.POWER_ID));
        resolveFreezeActions();
        check(!m.hasPower(FreezePower.POWER_ID) && m.intent == attack && FreezePower.getCounter(m) == 3, "freeze cleansed at the counter restores the real intent");

        reset(); m = monster(); m.halfDead = true; room.monsters.monsters.add(m);
        f = freeze(m, 3);
        check(!FreezePower.isFrozen(m) && m.intent == attack, "half-dead monsters are not shown as frozen");
        monsterTurn(m);
        check(m.turns == 1 && f.amount == 3, "half-dead monsters still take their revive turn");

        delaysScriptedMove(vanilla(GremlinNob.class, 85, 85, 14, 6), (byte)3, AbstractMonster.Intent.BUFF, "Gremlin Nob Bellow");
        Cultist cultist = vanilla(Cultist.class, 50, 50, 6);
        setField(cultist, "firstMove", true);
        delaysScriptedMove(cultist, (byte)3, AbstractMonster.Intent.BUFF, "Cultist Incantation");
        Hexaghost hexaghost = vanilla(Hexaghost.class, 250, 250, 5, 6, -1, 2);
        setField(hexaghost, "fireTackleCount", 2); setField(hexaghost, "infernoHits", 6);
        delaysScriptedMove(hexaghost, (byte)5, AbstractMonster.Intent.UNKNOWN, "Hexaghost Activate");
        delaysScriptedMove(vanilla(TimeEater.class, 200, 456, 7, 26), (byte)5, AbstractMonster.Intent.BUFF, "Time Eater Haste");
    }
    /** Real vanilla AI: a frozen scripted move must still be pending after the skip. */
    static void delaysScriptedMove(AbstractMonster m, byte scripted, AbstractMonster.Intent intent, String label) throws Exception {
        reset(); room.monsters.monsters.add(m);
        m.rollMove(); m.createIntent();
        check(m.nextMove == scripted && m.intent == intent, label + ": opens with the scripted move");
        List<Byte> history = new ArrayList<>(m.moveHistory);
        String name = m.moveName;
        freeze(m, 3);
        check(m.intent == AbstractMonster.Intent.STUN && m.nextMove == scripted, label + ": stun intent at the counter");
        monsterTurn(m);
        check(onlyQueued(ShowMoveNameAction.class, IntentFlashAction.class, ReducePowerAction.class, WaitAction.class), label + ": takeTurn skipped, nothing re-rolled");
        check(Objects.equals(m.moveName, name), label + ": move name kept for its real turn");
        resolveFreezeActions();
        check(m.intent == intent && m.nextMove == scripted && m.moveHistory.equals(history), label + ": still pending for the next turn");
        m.rollMove(); m.createIntent();
        check(m.nextMove != scripted, label + ": a re-roll would have replaced it (the old behaviour)");
    }
    /** Patched FontHelper.identifyOrb: which icon [E] uses in text. */
    static void energyIcon() throws Exception {
        roxyOrb = bare(TextureAtlas.AtlasRegion.class);
        lastRunOrb = bare(TextureAtlas.AtlasRegion.class);
        Method identify = com.megacrit.cardcrawl.helpers.FontHelper.class.getDeclaredMethod("identifyOrb", String.class);
        identify.setAccessible(true);
        AbstractPlayer savedPlayer = AbstractDungeon.player;
        CardCrawlGame.GameMode savedMode = CardCrawlGame.mode;
        MainMenuScreen savedMenu = CardCrawlGame.mainMenuScreen;
        TextureAtlas.AtlasRegion savedRed = AbstractCard.orb_red;
        try {
            // A finished Ironclad run leaves AbstractDungeon.player set; its getOrb() is orb_red.
            AbstractCard.orb_red = lastRunOrb;
            AbstractDungeon.player = bare(Ironclad.class);
            MainMenuScreen menu = bare(MainMenuScreen.class);
            menu.charSelectScreen = bare(CharacterSelectScreen.class);
            CharacterOption roxy = bare(CharacterOption.class), ironclad = bare(CharacterOption.class);
            roxy.c = bare(OrbRoxy.class);
            ironclad.c = AbstractDungeon.player;
            menu.charSelectScreen.options = new ArrayList<>(Arrays.asList(ironclad, roxy));
            menu.screen = MainMenuScreen.CurScreen.CHAR_SELECT;
            CardCrawlGame.mainMenuScreen = menu;
            CardCrawlGame.mode = CardCrawlGame.GameMode.CHAR_SELECT;
            roxy.selected = true;
            check(identify.invoke(null, "[E]") == roxyOrb, "character select: [E] uses Roxy's icon while she is selected");
            roxy.selected = false; ironclad.selected = true;
            check(identify.invoke(null, "[E]") == lastRunOrb, "character select: other characters keep the vanilla [E]");
            roxy.selected = true; ironclad.selected = false;
            menu.screen = MainMenuScreen.CurScreen.MAIN_MENU;
            check(identify.invoke(null, "[E]") == lastRunOrb, "other menu screens keep the vanilla [E]");
            menu.screen = MainMenuScreen.CurScreen.CHAR_SELECT;
            CardCrawlGame.mode = CardCrawlGame.GameMode.GAMEPLAY;
            check(identify.invoke(null, "[E]") == lastRunOrb, "in a run [E] uses the current player's icon");
        } finally {
            AbstractDungeon.player = savedPlayer;
            AbstractCard.orb_red = savedRed;
            CardCrawlGame.mode = savedMode;
            CardCrawlGame.mainMenuScreen = savedMenu;
        }
    }
    static void highRankSpells() throws Exception {
        for (int available = 0; available <= 4; available++) {
            for (int required = 0; required <= 4; required++) {
                reset(); player.powers.add(new ChantPower(player, available));
                roxymod.util.HighRankSpellHelper.resolve(player, required);
                int missing = Math.max(0, required - available);
                int paid = Math.min(available, required);
                check(queued(ConsumeChantAction.class) == (paid > 0 ? 1 : 0), "high rank pays available chant only");
                check(queued(ApplyPowerAction.class) == (missing > 0 ? 1 : 0), "exhaustion only for shortage");
                if (missing > 0) {
                    AbstractGameAction last = AbstractDungeon.actionManager.actions.get(AbstractDungeon.actionManager.actions.size()-1);
                    check(last.amount == Math.min(2, missing), "exhaustion capped at two");
                }
            }
        }
    }
    static void powers() throws Exception {
        reset(); QuietDomain domain = new QuietDomain(player, 2);
        domain.onChantConsumed(0); domain.onChantConsumed(1); domain.onChantConsumed(3);
        check(queued(DrawCardAction.class) == 1, "domain draws once per turn");
        domain.atStartOfTurn(); domain.onChantConsumed(2);
        check(queued(DrawCardAction.class) == 2, "domain resets next turn");
        domain.onChantGained(3);
        check(AbstractDungeon.actionManager.actions.get(2).amount == 6, "stacked domain block matches description");
        reset(); EchoLessonPower echo = new EchoLessonPower(player, 3);
        echo.atEndOfTurn(false);
        check(queued(RemoveSpecificPowerAction.class) == 0, "echo survives nonplayer hook");
        echo.atEndOfTurn(true);
        check(queued(RemoveSpecificPowerAction.class) == 1, "all unused echo charges expire");
        reset(); Monster attacker = monster(); QuietCounter counter = new QuietCounter(player, 6);
        counter.onAttacked(new DamageInfo(attacker, 5, DamageInfo.DamageType.THORNS), 5);
        check(queued(DamageAction.class) == 0, "counter ignores thorns");
        counter.onAttacked(new DamageInfo(attacker, 5, DamageInfo.DamageType.NORMAL), 0);
        check(queued(DamageAction.class) == 0, "counter ignores fully blocked hit");
        counter.onAttacked(new DamageInfo(attacker, 5, DamageInfo.DamageType.NORMAL), 5);
        counter.onAttacked(new DamageInfo(attacker, 5, DamageInfo.DamageType.NORMAL), 5);
        check(queued(DamageAction.class) == 1, "counter fires once even before removal resolves");
    }
    static void lightning() throws Exception {
        for (int base : new int[]{24, 32}) {
          for (int wet = 0; wet <= 3; wet++) {
            for (int status = 0; status < 5; status++) {
                reset(); Monster m = monster();
                if (wet > 0) m.powers.add(new QuietWet(m, wet));
                if (status == 1 || status == 4) player.powers.add(new WeakPower(player, 1, false));
                if (status == 2 || status == 4) m.powers.add(new VulnerablePower(m, 1, false));
                if (status == 3) m.powers.add(new IntangiblePower(m, 1));
                RoxyLightning card = bare(RoxyLightning.class);
                card.baseDamage = base; card.damageTypeForTurn = DamageInfo.DamageType.NORMAL;
                card.type = AbstractCard.CardType.ATTACK; card.tags = new ArrayList<>();
                card.calculateCardDamage(m);
                int expected = status == 3 ? 1 : (int)Math.floor((base + 5 * wet)
                    * (status == 1 || status == 4 ? 0.75 : 1.0) * (status == 2 || status == 4 ? 1.5 : 1.0));
                check(card.damage == expected && card.baseDamage == base, "Wet participates in Weak/Vulnerable/Intangible calculation");
                new LightningStrikeAction(m, player, card).update();
                DamageAction damage = (DamageAction)AbstractDungeon.actionManager.actions.stream()
                    .filter(DamageAction.class::isInstance).findFirst().get();
                DamageInfo info = (DamageInfo)field(damage, "info");
                check(info.output == expected, "preview matches queued damage");
                check(queued(RemoveSpecificPowerAction.class) == (wet > 0 ? 1 : 0), "lightning consumes Wet once");
            }
          }
        }
    }
    static void review() throws Exception {
        reset(); RoxyLightning first = bare(RoxyLightning.class), second = bare(RoxyLightning.class);
        first.cardID = second.cardID = "roxymod:RoxyLightning";
        check(!ReviewHelper.isReview(first), "no history before first play");
        AbstractDungeon.actionManager.cardsPlayedThisCombat.add(first);
        check(!ReviewHelper.isReview(first), "first recorded play does not review");
        AbstractDungeon.actionManager.cardsPlayedThisCombat.add(second);
        check(ReviewHelper.isReview(second), "second same-ID copy reviews");
        reset(); check(!ReviewHelper.isReview(second), "review resets between combats");
    }
    public static void main(String[] args) throws Exception {
        initialize(); payments(); reactions(); freezeTurns(); energyIcon(); highRankSpells(); powers(); lightning(); review();
        System.out.println("PASS: " + checks + " engine-backed mechanics assertions (GPU rendering excluded).");
    }
}
