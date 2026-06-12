package org.hitkavyam.scoring;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Logger;

public final class BehavioralCoefficients {

    private static final Logger LOG = Logger.getLogger(BehavioralCoefficients.class.getName());

    private final Map<String, Double> mnlBetas;
    private final Map<String, Double> ordinalBetas;
    public final double deltaTransitional, deltaPeripheral, deltaFringe;
    public final double betaTravelTimePT, betaTravelTimeCar, betaTravelTimeIPT;
    public final double delayPenaltyPerMinute;

    private BehavioralCoefficients(
            Map<String, Double> mnlBetas, Map<String, Double> ordinalBetas,
            double deltaTransitional, double deltaPeripheral, double deltaFringe,
            double betaTravelTimePT, double betaTravelTimeCar, double betaTravelTimeIPT,
            double delayPenaltyPerMinute) {
        this.mnlBetas = Collections.unmodifiableMap(mnlBetas);
        this.ordinalBetas = Collections.unmodifiableMap(ordinalBetas);
        this.deltaTransitional = deltaTransitional;
        this.deltaPeripheral = deltaPeripheral;
        this.deltaFringe = deltaFringe;
        this.betaTravelTimePT = betaTravelTimePT;
        this.betaTravelTimeCar = betaTravelTimeCar;
        this.betaTravelTimeIPT = betaTravelTimeIPT;
        this.delayPenaltyPerMinute = delayPenaltyPerMinute;
    }

    public static BehavioralCoefficients fromCSV(String mnlCsvPath, String ordinalCsvPath) {
        // We will implement CSV reading later. For now, fall back to defaults.
        return defaults();
    }

    public static BehavioralCoefficients defaults() {
        LOG.warning("Using RESEARCH DEFAULTS for behavioral coefficients.");
        Map<String, Double> mnl = new LinkedHashMap<>();
        Map<String, Double> ordinal = new LinkedHashMap<>();

        // UPDATED: Replaced FS_PU with FS_ITU to match your Python output
        mnl.put("PT|FS_ITU",          +1.85);
        mnl.put("PT|FS_FC",           +2.31);
        mnl.put("PT|FS_AW",           +1.67);
        mnl.put("PT|FS_PE",           +0.89);
        mnl.put("PT|FS_PR",           -0.94);
        mnl.put("PT|Infacc_Num",      -1.42);

        mnl.put("IPT|FS_ITU",         +0.91);
        mnl.put("IPT|FS_FC",          +1.44);
        mnl.put("IPT|Infacc_Num",     -1.78);

        return new BehavioralCoefficients(
                mnl, ordinal,
                -0.30, -0.65, -1.10,
                -0.040, -0.020, -0.035,
                -0.15
        );
    }

    public double mnl(String matsimMode, String predictor) {
        String key = normMode(matsimMode) + "|" + predictor;
        return mnlBetas.getOrDefault(key, 0.0);
    }

    private static String normMode(String mode) {
        return switch (mode.toLowerCase()) {
            case "pt"   -> "PT";
            case "taxi" -> "IPT";
            case "bike" -> "TWO_WHEELER";
            case "car"  -> "Car";
            case "walk" -> "NMT";
            default     -> mode.toUpperCase();
        };
    }
}