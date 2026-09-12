package com.gempukku.swccgo.cards.set9.dark;

import com.gempukku.swccgo.common.CardSubtype;
import com.gempukku.swccgo.common.CardType;
import com.gempukku.swccgo.common.ExpansionSet;
import com.gempukku.swccgo.common.Icon;
import com.gempukku.swccgo.common.Phase;
import com.gempukku.swccgo.common.Rarity;
import com.gempukku.swccgo.common.Side;
import com.gempukku.swccgo.common.Title;
import com.gempukku.swccgo.common.Uniqueness;
import com.gempukku.swccgo.common.Zone;
import com.gempukku.swccgo.framework.StartingSetup;
import com.gempukku.swccgo.framework.VirtualTableScenario;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.swccgo.framework.Assertions.assertAtLocation;
import static com.gempukku.swccgo.framework.Assertions.assertInZone;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Young Fool (9_141) ? issue #52: after Carbon-Freezing / The Emperor's Prize brings
 * frozen Luke to the Throne Room, Young Fool lights but failed to unfreeze because
 * TargetCardOnTableEffect omitted SpotOverride.INCLUDE_CAPTIVE.
 */
public class Card_9_141_Tests {
    protected VirtualTableScenario GetScenario() {
        return new VirtualTableScenario(
                new HashMap<>() {{
                    put("luke", "1_19");
                }},
                new HashMap<>() {{
                    put("youngFool", "9_141");
                    put("vader", "1_168");
                    put("emperor", "9_109");
                }},
                10,
                10,
                StartingSetup.DefaultLSGroundLocation,
                StartingSetup.BHBMObjective,
                StartingSetup.NoLSStartingInterrupts,
                StartingSetup.NoDSStartingInterrupts,
                StartingSetup.NoLSShields,
                StartingSetup.NoDSShields,
                VirtualTableScenario.Open
        );
    }

    @Test
    public void YoungFoolStatsAndKeywordsAreCorrect() {
        /**
         * Title: Young Fool
         * Uniqueness: Unique
         * Side: Dark
         * Type: Interrupt
         * Subtype: Lost
         * Destiny: 6
         * Game Text: If opponent's character present with Emperor was just lost, lose 1 Force to place that character
         *             out of play. OR Release frozen Luke at your Throne Room (Luke may not be battled until end of your
         *             next turn) OR Cancel NOOOOOOOOOOOO!
         * Set: Death Star II
         * Rarity: R
         */
        var scn = GetScenario();
        var card = scn.GetDSCard("youngFool").getBlueprint();

        assertEquals(Title.Young_Fool, card.getTitle());
        assertEquals(Uniqueness.UNIQUE, card.getUniqueness());
        assertEquals(Side.DARK, card.getSide());
        assertTrue(card.isCardType(CardType.INTERRUPT));
        assertEquals(CardSubtype.LOST, card.getCardSubtype());
        assertEquals(6, card.getDestiny(), scn.epsilon);
        assertEquals(1, card.getIconCount(Icon.DEATH_STAR_II));
        assertEquals(ExpansionSet.DEATH_STAR_II, card.getExpansionSet());
        assertEquals(Rarity.R, card.getRarity());
    }

    /**
     * Real-path for #52 (Carbon-Freezing / Emperor's Prize end-state): frozen Luke at
     * DS Throne Room. Young Fool must unfreeze Luke. BHBM for-remainder-of-game text may
     * immediately re-seize non-frozen Luke present with Vader afterward ? that is expected
     * and distinct from the freeze bug.
     */
    @Test
    public void YoungFoolUnfreezesFrozenLukeAtThroneRoom() {
        var scn = GetScenario();

        var luke = scn.GetLSCard("luke");
        var youngFool = scn.GetDSCard("youngFool");
        var vader = scn.GetDSCard("vader");
        var emperor = scn.GetDSCard("emperor");
        var throne = scn.GetDSCard("throne");
        var bhbm = scn.GetDSCard("bhbm");

        scn.StartGame();
        scn.MoveCardsToHand(youngFool);

        scn.MoveCardsToLocation(throne, vader, emperor);
        scn.MoveCardsToLocation(throne, luke);
        scn.DSActivateMaxForceAndPass();

        assertTrue(luke.isCaptive());
        assertEquals(vader, luke.getEscort());
        assertTrue(bhbm.isFlipped());

        // Carbon-Freezing / Prize path leaves Luke frozen at Throne Room
        scn.FreezeCard(luke);
        assertTrue(luke.isFrozen());
        assertTrue(luke.isCaptive());
        assertAtLocation(throne, vader, emperor);

        scn.SkipToPhase(Phase.MOVE);

        assertTrue("Young Fool should light for frozen Luke at Throne Room", scn.DSCardPlayAvailable(youngFool));
        assertTrue(scn.DSCardActionAvailable(youngFool, "Release frozen Luke"));

        scn.DSPlayCard(youngFool, "Release frozen Luke");
        assertTrue("Targeting must include frozen captive Luke", scn.DSHasCardChoiceAvailable(luke));
        scn.DSChooseCard(luke);
        scn.PassAllResponses();

        if (scn.DSDecisionAvailable("Choose character to release")) {
            scn.DSChooseCard(luke);
        }
        if (scn.LSDecisionAvailable("Choose where to rally")) {
            scn.LSChooseCard(throne);
        }
        scn.PassAllResponses();

        // #52: Young Fool must clear the freeze (card lights but play previously failed here)
        assertFalse("Young Fool must unfreeze Luke", luke.isFrozen());
        assertInZone(Zone.LOST_PILE, youngFool);

        // BHBM remainder-of-game may re-seize Luke present with Vader as a non-frozen captive
        if (luke.isCaptive()) {
            assertEquals(vader, luke.getEscort());
            assertFalse(luke.isFrozen());
        } else {
            assertAtLocation(throne, luke);
        }
    }
}
