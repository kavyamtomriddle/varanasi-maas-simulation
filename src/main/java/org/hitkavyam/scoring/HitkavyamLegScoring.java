package org.hitkavyam.scoring;

import org.hitkavyam.population.AgentProfile;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.core.scoring.SumScoringFunction;

public class HitkavyamLegScoring implements SumScoringFunction.LegScoring {

    private final AgentProfile profile;
    private final BehavioralCoefficients betas;
    private final MaaSEventHandler eventHandler;

    private double score = 0.0;

    public HitkavyamLegScoring(AgentProfile profile, BehavioralCoefficients betas, MaaSEventHandler eventHandler) {
        this.profile = profile;
        this.betas = betas;
        this.eventHandler = eventHandler;
    }

    // FIXED: The correct interface method required by MATSim 15
    @Override
    public void handleLeg(Leg leg) {
        String mode = leg.getMode();
        // Fallback to 0 if travel time isn't explicitly set, though it normally is by the routing engine
        double travelTimeMin = leg.getTravelTime().orElse(0.0) / 60.0;

        this.score += computeLegUtility(mode, travelTimeMin);
    }

    @Override
    public void finish() {}

    @Override
    public double getScore() {
        return score;
    }

    private double computeLegUtility(String mode, double travelTimeMin) {
        double timeBeta = switch (mode) {
            case "pt"   -> betas.betaTravelTimePT;
            case "taxi" -> betas.betaTravelTimeIPT;
            case "car"  -> betas.betaTravelTimeCar;
            case "bike" -> -0.030;
            case "walk" -> -0.015;
            default     -> -0.025;
        };

        double utility = timeBeta * travelTimeMin;

        if (mode.equals("pt") || mode.equals("taxi")) {
            // Using your exact Phase 2 variables
            String[] fsKeys = {"FS_ITU", "FS_PEOU", "FS_PR", "FS_AW", "FS_FC", "FS_PE"};

            for (String key : fsKeys) {
                double factorScore = profile.getFactorScore(key, 0.0);
                double beta = betas.mnl(mode, key);
                utility += beta * factorScore;
            }

            utility += betas.mnl(mode, "Infacc_Num") * profile.infoAccessBarrier;

            utility += switch (profile.beArchetype) {
                case "Transitional" -> betas.deltaTransitional;
                case "Peripheral"   -> betas.deltaPeripheral;
                case "Fringe"       -> betas.deltaFringe;
                default             -> 0.0;
            };

            if (profile.shiftWillingness >= 4) { utility += 0.50; }
            else if (profile.shiftWillingness <= 1) { utility -= 0.75; }

            // FIXED: Using the eventHandler to apply the Dynamic Delay Feedback!
            if (mode.equals("pt") && eventHandler != null) {
                double delayMin = eventHandler.getRegisteredDelay(profile.agentId);
                if (delayMin > 0) {
                    double infoBarrierNorm = profile.infoAccessBarrier / 5.0; // normalise 0→1
                    utility += betas.delayPenaltyPerMinute * delayMin * infoBarrierNorm;
                }
            }
        }
        return utility;
    }
}