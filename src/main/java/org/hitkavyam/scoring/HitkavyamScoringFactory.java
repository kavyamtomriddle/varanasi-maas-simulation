package org.hitkavyam.scoring;

import org.hitkavyam.population.AgentProfile;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.scoring.ScoringFunction;
import org.matsim.core.scoring.ScoringFunctionFactory;
import org.matsim.core.scoring.SumScoringFunction;
import org.matsim.core.scoring.functions.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HitkavyamScoringFactory implements ScoringFunctionFactory {

    private final Scenario scenario;
    private final BehavioralCoefficients betas;
    private final ScoringParametersForPerson params;
    private final MaaSEventHandler eventHandler;

    // FIXED: Exact keys
    private static final List<String> FS_KEYS = List.of("FS_ITU", "FS_PEOU", "FS_PR", "FS_AW", "FS_FC", "FS_PE");

    public HitkavyamScoringFactory(Scenario scenario, BehavioralCoefficients betas, MaaSEventHandler eventHandler) {
        this.scenario = scenario;
        this.betas = betas;
        this.eventHandler = eventHandler;
        this.params = new SubpopulationScoringParameters(scenario);
    }

    @Override
    public ScoringFunction createNewScoringFunction(Person person) {
        SumScoringFunction ssf = new SumScoringFunction();
        final ScoringParameters p = params.getScoringParameters(person);

        ssf.addScoringFunction(new CharyparNagelActivityScoring(p));
        ssf.addScoringFunction(new CharyparNagelAgentStuckScoring(p));
        ssf.addScoringFunction(new CharyparNagelMoneyScoring(p));

        AgentProfile profile = buildProfile(person);
        ssf.addScoringFunction(new HitkavyamLegScoring(profile, betas, eventHandler));

        return ssf;
    }

    private AgentProfile buildProfile(Person person) {
        var raw = person.getAttributes();
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

        return new AgentProfile(person.getId().toString(), archetype, surveyMode, matsimMode, income, shift, infacc, hasWork, distKm, scores);
    }

    @SuppressWarnings("unchecked")
    private <T> T attr(org.matsim.utils.objectattributes.attributable.Attributes attrs, String key, Class<T> type, T defaultVal) {
        Object raw = attrs.getAttribute(key);
        if (raw == null) return defaultVal;
        try { return type.cast(raw); } catch (Exception e) { return defaultVal; }
    }
}