package startermod;

import basemod.BaseMod;
import basemod.helpers.KeywordColorInfo;
import basemod.interfaces.EditCharactersSubscriber;
import basemod.interfaces.EditCardsSubscriber;
import basemod.interfaces.EditKeywordsSubscriber;
import basemod.interfaces.EditRelicsSubscriber;
import basemod.interfaces.EditStringsSubscriber;
import basemod.interfaces.PostInitializeSubscriber;
import com.badlogic.gdx.graphics.Color;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.localization.PotionStrings;
import com.megacrit.cardcrawl.localization.RelicStrings;
import com.megacrit.cardcrawl.unlock.UnlockTracker;
import startermod.characters.Roxy;
import startermod.cards.*;
import startermod.potions.*;
import startermod.relics.*;

@SpireInitializer
public class StarterMod implements EditCardsSubscriber, EditCharactersSubscriber, EditKeywordsSubscriber, EditRelicsSubscriber, EditStringsSubscriber, PostInitializeSubscriber {
    public static final String MOD_ID = "startermod";

    public static String makeID(String name) {
        return MOD_ID + ":" + name;
    }

    public static String assetPath(String path) {
        return MOD_ID + "/" + path;
    }

    public StarterMod() {
        registerColor();
        BaseMod.subscribe(this);
    }

    public static void initialize() {
        new StarterMod();
    }

    @Override
    public void receivePostInitialize() {
        registerPotions();
        System.out.println("[StarterMod] Loaded successfully.");
    }

    @Override
    public void receiveEditCards() {
        addAndRevealCard(new RoxyWaterBullet());
        addAndRevealCard(new RoxyDefend());
        addAndRevealCard(new RoxyBeginnerChant());
        addAndRevealCard(new RoxyWaterCurtain());
        addAndRevealCard(new RoxyMinorElementalMagic());
        addAndRevealCard(new RoxyFrostNova());
        addAndRevealCard(new RoxyCrystalCharm());
        addAndRevealCard(new RoxyChantShorten());
        addAndRevealCard(new RoxyWaterBlade());
        addAndRevealCard(new RoxyAquaRing());
        addAndRevealCard(new RoxyWaterSplash());
        addAndRevealCard(new RoxyManaLecture());
        addAndRevealCard(new RoxyMistStep());
        addAndRevealCard(new RoxySmallIcicle());
        addAndRevealCard(new RoxyQuietStudy());
        addAndRevealCard(new RoxyManaThread());
        addAndRevealCard(new RoxyRiverPush());
        addAndRevealCard(new RoxySimpleBarrier());
        addAndRevealCard(new RoxyCrystalShard());
        addAndRevealCard(new RoxyBookMark());
        addAndRevealCard(new RoxyDampCape());
        addAndRevealCard(new RoxyFocusedDrop());
        addAndRevealCard(new RoxyChantlessWaterMagic());
        addAndRevealCard(new RoxyWaterNeedle());
        addAndRevealCard(new RoxyTorrentImpact());
        addAndRevealCard(new RoxyLabyrinthInsight());
        addAndRevealCard(new RoxyLessonPrep());
        addAndRevealCard(new RoxyCrystalStudy());
        addAndRevealCard(new RoxyManaCycle());
        addAndRevealCard(new RoxyIciclePrison());
        addAndRevealCard(new RoxyFlowingCast());
        addAndRevealCard(new RoxySaintWaterLance());
        addAndRevealCard(new RoxyRapidChant());
        addAndRevealCard(new RoxyFloodWall());
        addAndRevealCard(new RoxySpellCorrection());
        addAndRevealCard(new RoxyCrystalStaff());
        addAndRevealCard(new RoxyAquaMirror());
        addAndRevealCard(new RoxyBlizzardStorm());
        addAndRevealCard(new RoxyManaReserve());
        addAndRevealCard(new RoxyCalmTutor());
        addAndRevealCard(new RoxyTidalCut());
        addAndRevealCard(new RoxyBlueCrystal());
        addAndRevealCard(new RoxyLabyrinthMap());
        addAndRevealCard(new RoxyIcicleField());
        addAndRevealCard(new RoxyFlowBarrier());
        addAndRevealCard(new RoxyMeasuredCast());
        addAndRevealCard(new RoxyTeacherGaze());
        addAndRevealCard(new RoxyManaWeave());
        addAndRevealCard(new RoxyWaterWhip());
        addAndRevealCard(new RoxySilentChant());
        addAndRevealCard(new RoxyCrystalWard());
        addAndRevealCard(new RoxyBlueMeteor());
        addAndRevealCard(new RoxyRecast());
        addAndRevealCard(new RoxyWaterSigil());
        addAndRevealCard(new RoxyDrippingFocus());
        addAndRevealCard(new RoxyWaveCounter());
        addAndRevealCard(new RoxyManaCondense());
        addAndRevealCard(new RoxyRainShelterPlus());
        addAndRevealCard(new RoxyWaterSaint());
        addAndRevealCard(new RoxyEchoLesson());
        addAndRevealCard(new RoxyManaFocus());
        addAndRevealCard(new RoxyLightning());
        addAndRevealCard(new RoxyRainShelter());
        addAndRevealCard(new RoxyLabyrinthRescue());
        addAndRevealCard(new RoxyGreatFlood());
        addAndRevealCard(new RoxyKingClassChant());
        addAndRevealCard(new RoxyCumulonimbus());
        addAndRevealCard(new RoxyAbsoluteWater());
        addAndRevealCard(new RoxyZeroChant());
        addAndRevealCard(new RoxyAquaCathedral());
        addAndRevealCard(new RoxyCometRain());
        addAndRevealCard(new RoxyGrandTutor());
        addAndRevealCard(new RoxyCrystalDomain());
        addAndRevealCard(new RoxyManaOverflow());
        addAndRevealCard(new RoxyEndlessBlue());
        System.out.println("[StarterMod] Added card: " + RoxyWaterBullet.ID);
    }

    @Override
    public void receiveEditCharacters() {
        BaseMod.addCharacter(
            new Roxy(Roxy.NAME),
            assetPath("images/charSelect/button.png"),
            assetPath("images/charSelect/portrait.png"),
            RoxyEnums.ROXY
        );
        System.out.println("[StarterMod] Added character: " + Roxy.NAME);
    }

    @Override
    public void receiveEditRelics() {
        BaseMod.addRelicToCustomPool(new RoxyManaCrystal(), RoxyEnums.ROXY_COLOR);
        BaseMod.addRelicToCustomPool(new RoxyChantPrimer(), RoxyEnums.ROXY_COLOR);
        BaseMod.addRelicToCustomPool(new RoxyTeachingPointer(), RoxyEnums.ROXY_COLOR);
        BaseMod.addRelicToCustomPool(new RoxyHydraulicSeal(), RoxyEnums.ROXY_COLOR);
        BaseMod.addRelicToCustomPool(new RoxyTravelFlask(), RoxyEnums.ROXY_COLOR);
        BaseMod.addRelicToCustomPool(new RoxyManaCrystalCore(), RoxyEnums.ROXY_COLOR);
        BaseMod.addRelicToCustomPool(new RoxyWaterSigilRelic(), RoxyEnums.ROXY_COLOR);
        BaseMod.addRelicToCustomPool(new RoxyLessonNotebook(), RoxyEnums.ROXY_COLOR);
        BaseMod.addRelicToCustomPool(new RoxyClearWaterPendant(), RoxyEnums.ROXY_COLOR);
        BaseMod.addRelicToCustomPool(new RoxyMilisCharm(), RoxyEnums.ROXY_COLOR);
        System.out.println("[StarterMod] Added relic: " + RoxyManaCrystal.ID);
    }

    @Override
    public void receiveEditStrings() {
        BaseMod.loadCustomStringsFile(
            CardStrings.class,
            assetPath("localization/zhs/CardStrings.json")
        );
        BaseMod.loadCustomStringsFile(
            PotionStrings.class,
            assetPath("localization/zhs/PotionStrings.json")
        );
        BaseMod.loadCustomStringsFile(
            RelicStrings.class,
            assetPath("localization/zhs/RelicStrings.json")
        );
    }

    @Override
    public void receiveEditKeywords() {
        KeywordColorInfo chantColor = new KeywordColorInfo();
        chantColor.color = new Color(1.0f, 0.86f, 0.25f, 1.0f);

        BaseMod.addKeyword(
            null,
            "咏唱",
            new String[] {"咏唱"},
            "洛琪希的施法资源。咏唱会跨回合保留；部分卡牌会消耗咏唱获得额外效果，并触发相关能力和遗物。",
            chantColor
        );

        KeywordColorInfo spellPhraseColor = new KeywordColorInfo();
        spellPhraseColor.color = new Color(1.0f, 0.86f, 0.25f, 1.0f);

        BaseMod.addKeyword(
            null,
            "起式",
            new String[] {"起式"},
            "打出后获得接续。",
            spellPhraseColor
        );
        BaseMod.addKeyword(
            null,
            "结式",
            new String[] {"结式"},
            "若拥有接续，消耗接续并触发本牌的额外效果。",
            spellPhraseColor
        );
        BaseMod.addKeyword(
            null,
            "接续",
            new String[] {"接续"},
            "最多 1 层，回合结束时移除。下一张结式牌会消耗接续。",
            spellPhraseColor
        );

        KeywordColorInfo exhaustionColor = new KeywordColorInfo();
        exhaustionColor.color = new Color(0.72f, 0.55f, 0.90f, 1.0f);

        BaseMod.addKeyword(
            null,
            "枯竭",
            new String[] {"枯竭"},
            "最多 2 层。下回合开始时，每层失去 1 点能量，随后移除。",
            exhaustionColor
        );

        KeywordColorInfo reviewColor = new KeywordColorInfo();
        reviewColor.color = new Color(1.0f, 0.86f, 0.25f, 1.0f);

        BaseMod.addKeyword(
            null,
            "复习",
            new String[] {"复习"},
            "本场战斗中第二次及以后打出同名牌时，触发牌面写明的额外效果。",
            reviewColor
        );

        KeywordColorInfo sageTimeColor = new KeywordColorInfo();
        sageTimeColor.color = new Color(1.0f, 0.86f, 0.25f, 1.0f);

        BaseMod.addKeyword(
            null,
            "贤者时间",
            new String[] {"贤者时间"},
            "获得格挡、造成攻击伤害、获得咏唱时，额外获得或造成等同于层数的数值。每回合开始减少 1 层。",
            sageTimeColor
        );

        KeywordColorInfo wetColor = new KeywordColorInfo();
        wetColor.color = new Color(0.35f, 0.78f, 1.0f, 1.0f);

        BaseMod.addKeyword(
            null,
            "湿润",
            new String[] {"湿润"},
            "最多 3 层。受到冰冻时，消耗 1 层湿润并额外获得 1 层冰冻；雷光会消耗全部湿润，每层额外造成 5 点伤害；敌人回合结束时减少 1 层。",
            wetColor
        );

        KeywordColorInfo freezeColor = new KeywordColorInfo();
        freezeColor.color = new Color(0.55f, 0.90f, 1.0f, 1.0f);

        BaseMod.addKeyword(
            null,
            "冰冻",
            new String[] {"冰冻"},
            "每个怪物拥有独立的冰冻计数器，初始为 3。冰冻达到计数器时，跳过其回合，移除等同计数器的冰冻，计数器随后增加 1。",
            freezeColor
        );
    }

    private static void addAndRevealCard(AbstractCard card) {
        BaseMod.addCard(card);
        // Opt-in development convenience; normal play discovers cards naturally.
        if (Boolean.getBoolean("roxy.dev.revealCards")) {
            UnlockTracker.unlockCard(card.cardID);
            UnlockTracker.markCardAsSeen(card.cardID);
        }
    }

    private static void registerPotions() {
        BaseMod.addPotion(
            RoxyChantPotion.class,
            new Color(0.20f, 0.70f, 1.0f, 1.0f),
            new Color(0.70f, 0.95f, 1.0f, 1.0f),
            new Color(0.95f, 0.85f, 0.25f, 1.0f),
            RoxyChantPotion.POTION_ID,
            RoxyEnums.ROXY
        );
        BaseMod.addPotion(
            RoxyWaterBarrierPotion.class,
            new Color(0.10f, 0.55f, 0.95f, 1.0f),
            new Color(0.65f, 0.95f, 1.0f, 1.0f),
            new Color(0.15f, 0.95f, 0.85f, 1.0f),
            RoxyWaterBarrierPotion.POTION_ID,
            RoxyEnums.ROXY
        );
        BaseMod.addPotion(
            RoxySageTimePotion.class,
            new Color(0.15f, 0.45f, 0.95f, 1.0f),
            new Color(1.0f, 0.86f, 0.25f, 1.0f),
            new Color(0.95f, 0.95f, 1.0f, 1.0f),
            RoxySageTimePotion.POTION_ID,
            RoxyEnums.ROXY
        );
    }

    private static void registerColor() {
        Color roxyColor = new Color(0.25f, 0.75f, 1.0f, 1.0f);

        BaseMod.addColor(
            RoxyEnums.ROXY_COLOR,
            roxyColor,
            assetPath("images/512/bg_attack_roxy.png"),
            assetPath("images/512/bg_skill_roxy.png"),
            assetPath("images/512/bg_power_roxy.png"),
            assetPath("images/512/card_roxy_orb.png"),
            assetPath("images/1024/bg_attack_roxy.png"),
            assetPath("images/1024/bg_skill_roxy.png"),
            assetPath("images/1024/bg_power_roxy.png"),
            assetPath("images/1024/card_roxy_orb.png"),
            assetPath("images/512/card_small_orb.png")
        );
    }
}


