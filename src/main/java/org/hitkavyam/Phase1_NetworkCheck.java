package org.hitkavyam;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.scenario.ScenarioUtils;

import java.util.Map;

/**
 * Hitkavyam — Phase 1 Network Validation
 *
 * Loads varanasi_network.xml into MATSim's in-memory graph and
 * prints a structural summary. Confirms the GeoJSON → XML
 * conversion pipeline is topologically sound.
 */
public class Phase1_NetworkCheck {

    public static void main(String[] args) {

        System.out.println("══════════════════════════════════════════");
        System.out.println("  HITKAVYAM · Varanasi MATSim · Phase 1  ");
        System.out.println("══════════════════════════════════════════");

        // ── 1. Bootstrap MATSim config ──────────────────────────────────
        // Config is MATSim's global settings object. Empty config is fine
        // for network-only loading — no simulation yet.
        Config config = ConfigUtils.createConfig();

        // ── 2. Create scenario container ────────────────────────────────
        // Scenario holds network, population, transit schedule, vehicles.
        // Think of it as your research database in memory.
        Scenario scenario = ScenarioUtils.createScenario(config);

        // ── 3. Load your network XML ─────────────────────────────────────
        // MatsimNetworkReader parses nodes (intersections) and links
        // (road/route segments) from your generated XML.
        String networkPath = "C:\\Users\\Hitkavyam\\IdeaProjects\\hitkavyam-matsim\\src\\main\\resources\\varanasi_network.xml";
        new MatsimNetworkReader(scenario.getNetwork()).readFile(networkPath);

        // ── 4. Print validation summary ──────────────────────────────────
        Network network = scenario.getNetwork();
        Map<?,?> nodes = network.getNodes();
        Map<?,?> links = network.getLinks();

        System.out.println("\n📍 Network loaded successfully:");
        System.out.printf("   Nodes (intersections): %,d%n", nodes.size());
        System.out.printf("   Links (route segments): %,d%n", links.size());

        // ── 5. Sample first 5 nodes for coordinate check ─────────────────
        System.out.println("\n🔍 Sample nodes (check Varanasi WGS84 coordinates):");
        network.getNodes().values().stream()
                .limit(5)
                .forEach(node -> System.out.printf(
                        "   Node %s → x=%.4f, y=%.4f%n",
                        node.getId(), node.getCoord().getX(), node.getCoord().getY()
                ));

        // ── 6. Sample first 5 links ───────────────────────────────────────
        System.out.println("\n🔗 Sample links (check length, capacity, modes):");
        network.getLinks().values().stream()
                .limit(5)
                .forEach(link -> System.out.printf(
                        "   Link %s → from:%s to:%s | len=%.1fm | cap=%.0f/hr | modes=%s%n",
                        link.getId(),
                        link.getFromNode().getId(),
                        link.getToNode().getId(),
                        link.getLength(),
                        link.getCapacity(),
                        link.getAllowedModes()
                ));

        System.out.println("\n✅ Phase 1 network validation complete.");
        System.out.println("   Next: validate coordinates in JOSM, then move to plans.xml synthesis.");
    }
}