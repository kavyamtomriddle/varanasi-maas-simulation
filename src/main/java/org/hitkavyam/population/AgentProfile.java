package org.hitkavyam.population;

import java.util.Map;
import java.util.HashMap;

/**
 * Typed representation of a Hitkavyam survey agent's profile.
 * Populated by reading MATSim PersonAttributes from plans.xml.
 */
public class AgentProfile {

    // ── Identity ─────────────────────────────────────────────────
    public final String  agentId;
    public final String  beArchetype;       // Ghat_Core | Transitional | Peripheral | Fringe
    public final String  surveyMode;        // original survey text
    public final String  matsimModePref;    // car | bike | pt | taxi | walk
    public final int     incomeCategory;    // 1–5 (1 = lowest)
    public final int     shiftWillingness;  // 1–5 ordinal
    public final int     infoAccessBarrier; // 0–5 severity
    public final boolean hasWorkTrip;
    public final double  workDistanceKm;    // -1 if unknown

    // ── TAM+TPB Factor Scores ────────────────────────────────────
    public final Map<String, Double> factorScores;

    public AgentProfile(
            String agentId, String beArchetype, String surveyMode,
            String matsimModePref, int incomeCategory, int shiftWillingness,
            int infoAccessBarrier, boolean hasWorkTrip, double workDistanceKm,
            Map<String, Double> factorScores) {

        this.agentId           = agentId;
        this.beArchetype       = beArchetype       != null ? beArchetype       : "Unknown";
        this.surveyMode        = surveyMode        != null ? surveyMode        : "unknown";
        this.matsimModePref    = matsimModePref    != null ? matsimModePref    : "walk";
        this.incomeCategory    = incomeCategory;
        this.shiftWillingness  = shiftWillingness;
        this.infoAccessBarrier = infoAccessBarrier;
        this.hasWorkTrip       = hasWorkTrip;
        this.workDistanceKm    = workDistanceKm;
        this.factorScores      = factorScores != null ? factorScores : new HashMap<>();
    }

    public double getFactorScore(String key, double defaultVal) {
        return factorScores.getOrDefault(key, defaultVal);
    }

    public boolean isMaaSTarget() {
        return (matsimModePref.equals("taxi") || matsimModePref.equals("pt"))
                && shiftWillingness >= 3;
    }

    @Override
    public String toString() {
        return String.format(
                "Agent[%s | %s | mode=%s | inc=%d | shift=%d | infacc=%d | maas=%b]",
                agentId, beArchetype, matsimModePref,
                incomeCategory, shiftWillingness, infoAccessBarrier, isMaaSTarget());
    }
}