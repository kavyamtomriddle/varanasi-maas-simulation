package org.hitkavyam;

import org.hitkavyam.scoring.BehavioralCoefficients;
import org.hitkavyam.scoring.HitkavyamScoringFactory;
import org.hitkavyam.scoring.MaaSEventHandler;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.*;
import org.matsim.core.config.groups.PlansCalcRouteConfigGroup.*;
import org.matsim.core.config.groups.QSimConfigGroup;
import org.matsim.core.config.groups.StrategyConfigGroup.*;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.core.scenario.ScenarioUtils;
import java.util.Set;

public class RunMaaSSimulation {

    private static final int LAST_ITERATION = 50; // Testing with 2 iterations
    private static final String NETWORK_FILE = "C:\\Users\\Hitkavyam\\IdeaProjects\\hitkavyam-matsim\\src\\main\\resources\\varanasi_network.xml";
    private static final String PLANS_FILE   = "C:\\Users\\Hitkavyam\\IdeaProjects\\hitkavyam-matsim\\src\\main\\resources\\varanasi_plans.xml";
    private static final String OUTPUT_DIR   = "C:\\Users\\Hitkavyam\\IdeaProjects\\hitkavyam-matsim\\output";

    public static void main(String[] args) {
        System.out.println("══════════════════════════════════════════════════");
        System.out.println("  HITKAVYAM · Varanasi MaaS Co-Evolutionary Sim");
        System.out.println("══════════════════════════════════════════════════");

        BehavioralCoefficients betas = BehavioralCoefficients.defaults();

        Config config = ConfigUtils.createConfig();

        // 1. THE SPATIAL FIX: Tell MATSim to dynamically translate coordinates!
        config.global().setCoordinateSystem("EPSG:32644"); // The Global Network Metric System (UTM 44N)
        config.plans().setInputCRS("EPSG:4326");           // The Plans GPS System (WGS84)
        config.network().setInputCRS("EPSG:32644");        // The Network System

        config.network().setInputFile(NETWORK_FILE);
        config.plans().setInputFile(PLANS_FILE);
        config.controler().setOutputDirectory(OUTPUT_DIR);
        config.controler().setLastIteration(LAST_ITERATION);
        config.controler().setOverwriteFileSetting(OverwriteFileSetting.deleteDirectoryIfExists);

        config.qsim().setEndTime(30 * 3600);
        config.qsim().setStartTime(5 * 3600);
        config.qsim().setNumberOfThreads(2);
        config.qsim().setSimStarttimeInterpretation(QSimConfigGroup.StarttimeInterpretation.onlyUseStarttime);

        // 2. THE BIKE CRASH FIX: Only route 'car' on the physical network.
        config.plansCalcRoute().setNetworkModes(Set.of("car"));

        ModeRoutingParams ptParams = new ModeRoutingParams("pt");
        ptParams.setTeleportedModeFreespeedFactor(0.4);
        config.plansCalcRoute().addModeRoutingParams(ptParams);

        ModeRoutingParams taxiParams = new ModeRoutingParams("taxi");
        taxiParams.setTeleportedModeFreespeedFactor(0.6);
        config.plansCalcRoute().addModeRoutingParams(taxiParams);

        ModeRoutingParams walkParams = new ModeRoutingParams("walk");
        walkParams.setTeleportedModeSpeed(1.2);
        config.plansCalcRoute().addModeRoutingParams(walkParams);

        // Teleport bikes at ~15 km/h to prevent the 0-node crash
        ModeRoutingParams bikeParams = new ModeRoutingParams("bike");
        bikeParams.setTeleportedModeSpeed(4.1);
        config.plansCalcRoute().addModeRoutingParams(bikeParams);

        ActivityParams home = new ActivityParams("home");
        home.setTypicalDuration(12 * 3600);
        config.planCalcScore().addActivityParams(home);

        ActivityParams work = new ActivityParams("work");
        work.setTypicalDuration(8 * 3600);
        work.setOpeningTime(7 * 3600);
        work.setClosingTime(21 * 3600);
        config.planCalcScore().addActivityParams(work);

        StrategySettings keepBest = new StrategySettings();
        keepBest.setStrategyName("KeepLastSelected");
        keepBest.setWeight(0.70);
        config.strategy().addStrategySettings(keepBest);

        StrategySettings reRoute = new StrategySettings();
        reRoute.setStrategyName("ReRoute");
        reRoute.setWeight(0.15);
        config.strategy().addStrategySettings(reRoute);

        StrategySettings modeChoice = new StrategySettings();
        modeChoice.setStrategyName("SubtourModeChoice");
        modeChoice.setWeight(0.15);
        config.strategy().addStrategySettings(modeChoice);

        config.subtourModeChoice().setModes(new String[]{"car","bike","pt","walk","taxi"});
        config.strategy().setMaxAgentPlanMemorySize(5);

        Scenario scenario = ScenarioUtils.loadScenario(config);
        Controler controler = new Controler(scenario);
        MaaSEventHandler eventHandler = new MaaSEventHandler(controler.getEvents());
        controler.setScoringFunctionFactory(new HitkavyamScoringFactory(scenario, betas, eventHandler));

        controler.addOverridingModule(new AbstractModule() {
            @Override
            public void install() {
                addControlerListenerBinding().toInstance((IterationEndsListener) event ->
                        eventHandler.onIterationEnd(event.getIteration(), OUTPUT_DIR)
                );
            }
        });

        System.out.println("\n🚀 Starting MATSim Execution...");
        controler.run();
    }
}