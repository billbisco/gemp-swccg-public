package com.gempukku.swccgo.cards.set9.light;

import com.gempukku.swccgo.common.CardSubtype;
import com.gempukku.swccgo.common.CardType;
import com.gempukku.swccgo.common.Icon;
import com.gempukku.swccgo.common.Keyword;
import com.gempukku.swccgo.common.Phase;
import com.gempukku.swccgo.common.Rarity;
import com.gempukku.swccgo.common.Side;
import com.gempukku.swccgo.common.Uniqueness;
import com.gempukku.swccgo.common.Zone;
import com.gempukku.swccgo.framework.StartingSetup;
import com.gempukku.swccgo.framework.VirtualTableScenario;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Issue #939: firing X-wing Laser Cannons from Red Squadron 4 should offer
 * "X=3 using Red Squadron 4 (use 2 Force)" so 2 Force sets X=3, not X=2.
 */
public class Card_9_082_Tests {

    protected VirtualTableScenario GetScenario() {
        return new VirtualTableScenario(
                new HashMap<>() {{
                    put("rs4", "9_82");
                    put("hobbie", "3_5");
                    put("xwlc", "7_162");
                }},
                new HashMap<>() {{
                    put("tie", "1_304");
                }},
                10,
                10,
                StartingSetup.DefaultLSSpaceSystem,
                StartingSetup.DefaultDSSpaceSystem,
                StartingSetup.NoLSStartingInterrupts,
                StartingSetup.NoDSStartingInterrupts,
                StartingSetup.NoLSShields,
                StartingSetup.NoDSShields,
                VirtualTableScenario.Open
        );
    }

    @Test
    public void RedSquadron4StatsAndIconsAreCorrect() {
        var scn = GetScenario();
        var card = scn.GetLSCard("rs4").getBlueprint();
        assertEquals("Red Squadron 4", card.getTitle());
        assertEquals(Side.LIGHT, card.getSide());
        assertEquals(Uniqueness.UNIQUE, card.getUniqueness());
        assertEquals(Rarity.U, card.getRarity());
        assertEquals(CardSubtype.STARFIGHTER, card.getCardSubtype());
        assertEquals(2, card.getDestiny());
        assertEquals(2, card.getDeployCost(), 0.01);
        assertEquals(3, card.getPower(), 0.01);
        assertEquals(4, card.getManeuver(), 0.01);
        assertEquals(5, card.getHyperspeed(), 0.01);
        assertEquals(5, card.getForfeit(), 0.01);
        scn.BlueprintCardTypeCheck(scn.GetLSCard("rs4"), new ArrayList<>() {{
            add(CardType.STARSHIP);
        }});
        scn.BlueprintIconCheck(scn.GetLSCard("rs4"), new ArrayList<>() {{
            add(Icon.DEATH_STAR_II);
            add(Icon.STARSHIP);
            add(Icon.NAV_COMPUTER);
            add(Icon.SCOMP_LINK);
        }});
        scn.BlueprintKeywordCheck(scn.GetLSCard("rs4"), new ArrayList<>() {{
            add(Keyword.RED_SQUADRON);
        }});
    }

    @Test
    public void RedSquadron4MayUseTwoForceToMakeXwingLaserCannonX3() {
        var scn = GetScenario();
        var rs4 = scn.GetLSCard("rs4");
        var hobbie = scn.GetLSCard("hobbie");
        var xwlc = scn.GetLSCard("xwlc");
        var tie = scn.GetDSCard("tie");
        var system = scn.GetLSStartingLocation();

        scn.StartGame();
        scn.MoveCardsToLocation(system, rs4, tie);
        scn.BoardAsPilot(rs4, hobbie);
        scn.AttachCardsTo(rs4, xwlc);
        scn.EnsureLSForcePile(2);

        scn.SkipToLSTurn(Phase.BATTLE);
        scn.LSInitiateBattle(system);
        scn.PassBattleStartResponses();
        scn.PassAllResponses();

        assertTrue(scn.LSCardActionAvailable(xwlc, "Fire"));
        scn.PrepareLSDestiny(1);
        scn.LSUseCardAction(xwlc, "Fire");
        scn.LSChooseCard(tie);
        assertTrue(scn.LSDecisionAvailable("Choose X for this firing"));
        scn.LSChooseOption("X=3 using Red Squadron 4");
        scn.PassWeaponFireWithDestinyDraw();
        scn.PassAllResponses();

        assertEquals("2 Force pays for X=3, so Used Pile should receive those 2 Force", 0, scn.GetLSForcePileCount());
        assertTrue("X=3 must lose the TIE (destiny 1 + X 3 > maneuver 3)",
                tie.getZone() == Zone.LOST_PILE || tie.getZone() == Zone.TOP_OF_LOST_PILE);
    }
}
