package org.hitkavyam.population;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.scenario.ScenarioUtils;

import java.util.*;
import java.util.stream.Collectors;

public class PopulationValidator {

    // Using the absolute path that worked for your network file
    private static final String PLANS_PATH =
            "C:\\Users\\Hitkavyam\\IdeaProjects\\hitkavyam-matsim\\src\\main\\resources\\varanasi_plans.xml";

    // UPDATED to match your exact Python output keys
    private static final List<String> FS_KEYS = List.of(
            "FS_ITU", "FS_PEOU", "FS_PR", "FS_AW", "FS_FC", "FS_PE"
    );

    public static void main(String[] args) {

        System.out.println("══════════════════════════════════════════════");
        System.out.println("  HITKAVYAM · Population Validator · Phase 2");
        System.out.println("══════════════════════════════════════════════");

        Config config = ConfigUtils.createConfig();
        Scenario scenario = ScenarioUtils.createScenario(config);

        try {
            new PopulationReader(scenario).readFile(PLANS_PATH);
        } catch (Exception e) {
            System.err.println("❌ CRITICAL: Could not find varanasi_plans.xml at " + PLANS_PATH);
            return;
        }

        Population population = scenario.getPopulation();
        int totalAgents = population.getPersons().size();
        System.out.printf("%n📍 Population loaded: %,d agents%n", totalAgents);

        List<AgentProfile> profiles = new ArrayList<>();
        List<String> malformed = new ArrayList<>();

        for (Person person : population.getPersons().values()) {
            try {
                AgentProfile p = readProfile(person);
                profiles.add(p);
            } catch (Exception e) {
                malformed.add(person.getId().toString() + ": " + e.getMessage());
            }
        }

        System.out.println("\n🔍 Plan Structure Validation:");
        int fullPlan = 0, homePlan = 0, badPlan = 0;

        for (Person person : population.getPersons().values()) {
            Plan plan = person.getSelectedPlan();
            if (plan == null) {
                badPlan++;
                continue;
            }
            List<PlanElement> elements = plan.getPlanElements();
            if (elements.size() == 5) {
                fullPlan++;
            } else if (elements.size() == 1) {
                homePlan++;
            } else {
                badPlan++;
            }
        }

        System.out.printf("   Full plans  (home→work→home): %,d%n", fullPlan);
        System.out.printf("   Home-only plans:               %,d%n", homePlan);
        System.out.printf("   ⚠  Malformed plans:            %,d%n", badPlan);

        System.out.println("\n📊 Attribute Coverage Report:");
        String[] coreAttrs = {"BE_Archetype", "Shift_Willingness", "InfoAccess_Barrier", "Income_Category"};
        for (String attr : coreAttrs) {
            long count = population.getPersons().values().stream()
                    .filter(p -> p.getAttributes().getAttribute(attr) != null).count();
            System.out.printf("   %-28s → %,d / %,d (%.0f%%)%n", attr, count, totalAgents, count * 100.0 / totalAgents);
        }

        for (String key : FS_KEYS) {
            long count = profiles.stream().filter(p -> p.factorScores.containsKey(key)).count();
            System.out.printf("   %-28s → %,d / %,d (%.0f%%)%n", key, count, totalAgents, count * 100.0 / totalAgents);
        }

        System.out.println("\n📈 Population Statistics:");
        long maasTargets = profiles.stream().filter(AgentProfile::isMaaSTarget).count();
        System.out.printf("   MaaS target agents (IPT/PT + willing to shift): %,d (%.1f%%)%n",
                maasTargets, maasTargets * 100.0 / profiles.size());

        System.out.println("\n🔬 Sample Agent Profiles (first 3):");
        profiles.stream().limit(3).forEach(p -> System.out.println("   " + p));

        System.out.println("\n══════════════════════════════════════════════");
        if (badPlan == 0) {
            System.out.println("  ✅ Population valid — ready for Phase 3.");
        }
        System.out.println("══════════════════════════════════════════════");
    }

    private static AgentProfile readProfile(Person person) {
        org.matsim.utils.objectattributes.attributable.Attributes raw = person.getAttributes();
        String archetype = attr(raw, "BE_Archetype", String.class, "Unknown");
        String surveyMode = attr(raw, "Survey_Mode", String.class, "unknown");
        String matsimMode = attr(raw, "MATSim_Mode_Pref", String.class, "walk");
        int income = attr(raw, "Income_Category", Integer.class, 0);
        int shift = attr(raw, "Shift_Willingness", Integer.class, 0);
        int infacc = attr(raw, "InfoAccess_Barrier", Integer.class, 0);
        boolean hasWork = attr(raw, "Has_Work_Trip", Boolean.class, false);
        double distKm = attr(raw, "Work_Distance_km", Double.class, -1.0);

        Map<String, Double> scores = new HashMap<>();
        for (String key : FS_KEYS) {
            Double val = attr(raw, key, Double.class, null);
            if (val != null) scores.put(key, val);
        }

        return new AgentProfile(person.getId().toString(), archetype, surveyMode, matsimMode,
                income, shift, infacc, hasWork, distKm, scores);
    }

    @SuppressWarnings("unchecked")
    private static <T> T attr(org.matsim.utils.objectattributes.attributable.Attributes attrs, String key, Class<T> type, T defaultVal) {
        Object raw = attrs.getAttribute(key);
        if (raw == null) return defaultVal;
        try {
            return type.cast(raw);
        } catch (ClassCastException e) {
            // THE FIX: If Java reads it as a String, safely parse it into the correct number format
            try {
                String s = raw.toString();
                if (type == Integer.class) return type.cast(Integer.parseInt(s));
                if (type == Double.class) return type.cast(Double.parseDouble(s));
                if (type == Boolean.class) return type.cast(Boolean.parseBoolean(s));
                return type.cast(raw);
            } catch (Exception ex) {
                return defaultVal;
            }
        }
    }
}