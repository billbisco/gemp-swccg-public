package com.gempukku.swccgo.cards.set2.light;

import com.gempukku.swccgo.common.Phase;
import com.gempukku.swccgo.framework.StartingSetup;
import com.gempukku.swccgo.framework.VirtualTableScenario;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertTrue;

/**
 * Issue #946: Rebel Squad Leader moving via Shawn Valdez (battle phase, regular move)
 * must still be able to bring a squad of exactly 3 other troopers.
 */
public class Card_2_018_Tests {

    protected VirtualTableScenario GetScenario() {
        return new VirtualTableScenario(
                new HashMap<>() {{
                    put("rsl", "2_18");
                    put("shawn", "3_19");
                    put("t1", "1_28");
                    put("t2", "1_28");
                    put("t3", "1_28");
                    put("walkway", "5_79");
                }},
                new HashMap<>() {{
                    put("vader", "1_168");
                }},
                10,
                10,
                StartingSetup.DefaultLSGroundLocation,
                StartingSetup.DefaultDSGroundLocation,
                StartingSetup.NoLSStartingInterrupts,
                StartingSetup.NoDSStartingInterrupts,
                StartingSetup.NoLSShields,
                StartingSetup.NoDSShields,
                VirtualTableScenario.Open
        );
    }

    @Test
    public void RebelSquadLeaderMayBringSquadWhenMovedByShawnValdez() {
        var scn = GetScenario();
        var rsl = scn.GetLSCard("rsl");
        var shawn = scn.GetLSCard("shawn");
        var t1 = scn.GetLSCard("t1");
        var t2 = scn.GetLSCard("t2");
        var t3 = scn.GetLSCard("t3");
        var walkway = scn.GetLSCard("walkway");
        var vader = scn.GetDSCard("vader");
        var battleSite = scn.GetLSStartingLocation();

        scn.StartGame();
        scn.MoveLocationToTable(walkway);
        scn.MoveCardsToLocation(battleSite, shawn, vader);
        scn.MoveCardsToLocation(walkway, rsl, t1, t2, t3);

        scn.SkipToLSTurn(Phase.BATTLE);
        scn.LSInitiateBattle(battleSite);
        scn.PassBattleStartResponses();

        assertTrue("Shawn should offer to move adjacent troopers", scn.LSCardActionAvailable(shawn, "Move adjacent"));
        scn.LSUseCardAction(shawn, "Move adjacent");
        scn.LSChooseCard(rsl);
        scn.PassAllResponses();

        assertTrue("RSL should offer Move with squad after Shawn moves him; LS=" + scn.GetLSAvailableActions(),
                scn.LSActionAvailable("Move with 'squad'") || scn.LSCardActionAvailable(rsl, "squad"));
    }
}
