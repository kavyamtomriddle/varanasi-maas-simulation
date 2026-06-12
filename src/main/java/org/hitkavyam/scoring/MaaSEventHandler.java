package org.hitkavyam.scoring;

import org.matsim.api.core.v01.events.*;
import org.matsim.api.core.v01.events.handler.*;
import org.matsim.core.api.experimental.events.EventsManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

@SuppressWarnings("SpellCheckingInspection")
public class MaaSEventHandler implements
        PersonDepartureEventHandler,
        PersonArrivalEventHandler,
        PersonEntersVehicleEventHandler,
        PersonLeavesVehicleEventHandler {

    private static final Logger LOG = Logger.getLogger(MaaSEventHandler.class.getName());

    private static final double DELAY_THRESHOLD_MIN = 5.0;

    private final Map<String, Double> agentDelays     = new ConcurrentHashMap<>();
    private final Map<String, Long>   modeDepartures  = new ConcurrentHashMap<>();
    private final Map<String, String> agentCurrentMode= new ConcurrentHashMap<>();

    private final List<String[]>      modalShareLog   = new ArrayList<>();

    // In Phase 3 we approximate with a simple waiting-time heuristic.
    // This will be populated in Phase 4.
    private final Map<String, Double> vehicleScheduledDeparture = new ConcurrentHashMap<>();

    public MaaSEventHandler(EventsManager eventsManager) {
        eventsManager.addHandler(this);
    }

    @Override
    public void handleEvent(PersonDepartureEvent event) {
        String agentId = event.getPersonId().toString();
        String mode    = event.getLegMode();
        agentCurrentMode.put(agentId, mode);
        modeDepartures.merge(mode, 1L, Long::sum);
    }

    @Override
    public void handleEvent(PersonArrivalEvent event) {
        agentCurrentMode.remove(event.getPersonId().toString());
    }

    @Override
    public void handleEvent(PersonEntersVehicleEvent event) {
        String agentId = event.getPersonId().toString();
        String mode    = agentCurrentMode.getOrDefault(agentId, "unknown");

        if (mode.equals("pt")) {
            double actualBoardingTime  = event.getTime();
            double vehicleExpectedTime = vehicleScheduledDeparture
                    .getOrDefault(event.getVehicleId().toString(), actualBoardingTime);
            double waitExtraMin = (actualBoardingTime - vehicleExpectedTime) / 60.0;

            if (waitExtraMin > DELAY_THRESHOLD_MIN) {
                agentDelays.merge(agentId, waitExtraMin - DELAY_THRESHOLD_MIN, Double::sum);
            }
        }
    }

    @Override
    public void handleEvent(PersonLeavesVehicleEvent event) {}

    public void onIterationEnd(int iter, String outputDir) {
        long totalDepartures = modeDepartures.values().stream().mapToLong(v->v).sum();
        if (totalDepartures > 0) {
            String ptShare  = String.format("%.3f", modeDepartures.getOrDefault("pt",  0L) * 1.0 / totalDepartures);
            String carShare = String.format("%.3f", modeDepartures.getOrDefault("car", 0L) * 1.0 / totalDepartures);
            String iptShare = String.format("%.3f", modeDepartures.getOrDefault("taxi",0L) * 1.0 / totalDepartures);
            modalShareLog.add(new String[]{
                    String.valueOf(iter), ptShare, carShare, iptShare, String.valueOf(totalDepartures)
            });
            LOG.info(String.format("Iteration %d modal share → PT: %s | Car: %s | IPT: %s | total: %d",
                    iter, ptShare, carShare, iptShare, totalDepartures));
        }

        // FIXED: Removed the redundant '|| iter == 0' condition
        if (iter % 5 == 0) {
            writeModalShareCSV(outputDir + "/modal_share.csv");
        }

        agentDelays.clear();
        modeDepartures.clear();
        agentCurrentMode.clear();
    }

    public double getRegisteredDelay(String agentId) {
        return agentDelays.getOrDefault(agentId, 0.0);
    }

    private void writeModalShareCSV(String path) {
        try (var writer = new java.io.FileWriter(path, false)) {
            writer.write("iteration,pt_share,car_share,ipt_share,total_departures\n");
            for (String[] row : modalShareLog) {
                writer.write(String.join(",", row) + "\n");
            }
        } catch (Exception e) {
            LOG.warning("Could not write modal_share.csv: " + e.getMessage());
        }
    }

    @Override
    public void reset(int iteration) {
        agentDelays.clear();
        modeDepartures.clear();
    }
}