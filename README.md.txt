# 🚖 Varanasi MaaS Co-Evolutionary Simulation Engine

[![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=java&logoColor=white)](https://www.java.com/)
[![MATSim](https://img.shields.io/badge/MATSim-15.0-blue?style=for-the-badge)](https://matsim.org/)
[![Python](https://img.shields.io/badge/Python-3.9%2B-3776AB?style=for-the-badge&logo=python&logoColor=white)](https://www.python.org/)
[![Uber H3](https://img.shields.io/badge/Uber_H3-Hexagons-black?style=for-the-badge&logo=uber&logoColor=white)](https://h3geo.org/)
[![Pydeck](https://img.shields.io/badge/Pydeck-3D_Vis-FF4B4B?style=for-the-badge)](https://deckgl.readthedocs.io/)

A large-scale, agent-based traffic simulation pipeline that models the adoption of Mobility-as-a-Service (MaaS) in Varanasi, India. 

Unlike standard traffic models that route agents based strictly on travel time and financial cost, this engine features a **custom-built Java Behavioral Routing Engine** that overrides default MATSim utilities with human cognitive psychology metrics (TAM + TPB constructs) derived from Multinomial and Ordinal Logit machine learning models.

---

## 📊 Project Highlights & Results
* **The Engine:** Handled 6,400+ daily commuter trips using a 50-iteration genetic algorithm (Co-Evolutionary learning loop) to find system-wide Nash Equilibrium.
* **The Result:** Mathematically proved that resolving the *Information Access Barrier* (via MaaS integration) triggers a massive modal shift—driving Public Transit (PT) adoption from **5.0% to 36.4%**.
* **Memory-Safe Pipelines:** Processed gigabytes of compressed `.xml.gz` MATSim logs using highly optimized, memory-safe Python `iterparse` streams.
* **Spatial Analytics:** Aggregated results using Uber's H3 Hexbins (Resolution 8) to render interactive 3D WebGL maps proving the spatial mismatch between the Ghat-Core and Industrial Fringe.

*(Note to recruiters: Click the image below to view the interactive 3D WebGL Map)*

<div align="center">
  <a href="https://your-github-username.github.io/varanasi-maas-simulation/deliverables/varanasi_equilibrium_3d.html">
    <img src="deliverables/nash_equilibrium.png" alt="Nash Equilibrium Convergence" width="800"/>
  </a>
</div>

---

## ⚙️ Tech Stack & Architecture

This project is divided into a **Python Data Science** frontend and a **Java Enterprise** backend simulation engine.

### 1. Data Science & Machine Learning (Python)
* **Spatial ETL:** Cleaned and projected raw UPSRTC GeoJSON bus routes into a strict topological graph (UTM Zone 44N metric projections).
* **Predictive Modeling:** Executed K-means clustering (Built-Environment Archetypes), Confirmatory Factor Analysis (CFA), and Logit regression to extract behavioral Beta coefficients (e.g., *Facilitating Conditions*, *Awareness*).
* **Big Data Parsing:** Built custom XML streaming parsers utilizing `xml.etree.ElementTree` and `gzip` to extract agent equilibrium states without crashing system RAM.

### 2. Co-Evolutionary Simulation (Java / MATSim 15 API)
* **Custom Scoring Factory:** Wrote `HitkavyamLegScoring.java` to hijack the open-source MATSim API. Injected psychological modifiers dynamically into the `SubtourModeChoice` replanning strategy.
* **Dynamic Feedback Loops:** Engineered an `EventHandler` interface to dynamically punish agent utility scores if their transit vehicle experienced schedule delays, amplifying the penalty for agents with high *Information Access Barriers*.
* **Parallel Execution:** Configured the `QSim` microsimulation engine for multi-threaded execution to simulate 3,246 unique commuter profiles simultaneously.

---

## 📂 Repository Structure

```text
varanasi-maas-simulation/
│
├── src/main/java/org/hitkavyam/
│   ├── population/          # Java Records & XML Parsers for Agent Profiles
│   ├── scoring/             # The Custom Behavioral Utility Logit Engine
│   └── RunMaaSSimulation    # The Main Simulation Runner & Configuration
│
├── notebooks/
│   ├── phase1_spatial_etl.ipynb
│   ├── phase3_logit_modeling.ipynb
│   └── phase5_parsing_and_vis.ipynb
│
├── deliverables/
│   ├── nash_equilibrium.png           # Co-evolutionary convergence chart
│   └── varanasi_equilibrium_3d.html   # Interactive Pydeck map (Host via GitHub pages)
│
└── pom.xml                  # Maven Dependencies (GeoTools, MATSim, Guice)

## 🚀 How to Run Locally

### Prerequisites
* Java 17+ (Eclipse Temurin recommended)
* Apache Maven
* Python 3.9+ (with `pandas`, `h3`, `pydeck`, `matplotlib`)

### 1. Run the Java Simulation
1. Clone the repository and open it in IntelliJ IDEA.
2. Ensure Maven dependencies are loaded.
3. Open `RunMaaSSimulation.java`.
4. **Critical:** Edit your Run Configuration VM Options to allocate sufficient heap memory: `-Xmx6g -Xms2g`.
5. Run `main()`. The engine will process 50 iterations and output results to the `/output` folder.

### 2. Run the Python Visualizations
1. Open the `notebooks/phase5_parsing_and_vis.ipynb` notebook.
2. Point the script to the freshly generated `output_plans.xml.gz` and `modal_share.csv`.
3. Run the cells to dynamically generate the H3 Hexbin maps and convergence charts.

---

## 👨‍💻 Author

**Hitkavyam** B.Tech Civil Engineering | Indian Institute of Technology (BHU) Varanasi  
*Aspiring SDE / Data Scientist*

[![LinkedIn](https://img.shields.io/badge/LinkedIn-Profile-blue?logo=linkedin)](linkedin.com/in/hitkavyam-sharma-a87061345) 
[![Email](https://img.shields.io/badge/Email-Contact_Me-red?logo=gmail)](mailto:hitkavyam.student.cd.civ24@itbhu.ac.in)