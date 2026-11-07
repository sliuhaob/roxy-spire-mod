package startermod;

import com.evacipated.cardcrawl.modthespire.lib.SpireEnum;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.helpers.CardLibrary;

public class RoxyEnums {
    @SpireEnum(name = "STARTERMOD_ROXY")
    public static AbstractPlayer.PlayerClass ROXY;

    @SpireEnum(name = "STARTERMOD_ROXY_COLOR")
    public static AbstractCard.CardColor ROXY_COLOR;

    @SpireEnum(name = "STARTERMOD_ROXY_COLOR")
    public static CardLibrary.LibraryType ROXY_LIBRARY;
}

