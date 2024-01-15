import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.ui.RectangleInsets;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Die Klasse SewingsRitter ist eine Erweiterung der Klasse MyBot und implementiert eine spezialisierte Strategie
 * für ein Kartenspiel. Diese Klasse verwendet verschiedene Analyse- und Entscheidungsfindungstechniken,
 * um im Spiel zu gewinnen. Sie nutzt historische Daten, führt Simulationen durch und passt ihre Strategien
 * dynamisch an, um die Leistung im Spiel zu optimieren.
 *
 * Zu den Hauptfunktionen gehören die Analyse der vergangenen Spiele, die Bewertung der Effektivität verschiedener
 * Strategien dagegen, die Anpassung der Strategien basierend auf den gewonnenen Erkenntnissen und die dynamische
 * Anpassung wichtiger Parameter wie Epsilon und leider nicht Smoothing-Faktor, da ich heute abgeben muss.
 */
public class SewingsRitter extends MyBot {

    private int gameNumber;
    private int wins;
    private List<Integer> gamePointsHistory = new ArrayList<>(1000);
    private List<Strategy> usedStrategies = new ArrayList<>(1000);
    private Strategy nextStrategy = null;
    private List<Strategy> strategies = new ArrayList<>(1000);
    private List<Counter> pastGames = new ArrayList<>(1000);
    private int numOfTestGames = 100;
    private int numOfAnalysisGames = 15;
    private Simulation simulation;
    List<int[]> strategyPerformanceData = new ArrayList<>();
    private Strategy currentStrategy;
    private List<Integer> winsHistory = new ArrayList<>();
    private ChartPanel chartPanel;
    private double smoothingFactor = 0.05;
    private double epsilon = 0.7; // FIX ME 0.7
    private int consecutiveLoss = 0;
    private int consecutiveWins = 0;
    private static final double LOSS_THRESHOLD = 2;
    private static final double WIN_THRESHOLD = 4;

    private static final double DELTA_EPSILON = 0.1;
    private static final double MAX_EPSILON = 1;
    private static final double MIN_EPSILON = 0;

    public SewingsRitter() {
        super();

        simulation = new Simulation();

        winsHistory = new ArrayList<>();
        SwingUtilities.invokeLater(this::createAndShowChart);
    }

    /**
     * Passt den Epsilon-Wert basierend auf der Anzahl der aufeinanderfolgenden Gewinne und Verluste an.
     */
    private void adjustEpsilon() {
        if(consecutiveLoss > consecutiveWins) {
            if (consecutiveLoss == LOSS_THRESHOLD) {
                epsilon = Math.min(epsilon + DELTA_EPSILON, MAX_EPSILON);
                consecutiveLoss = 0;
                consecutiveWins = 0;
            }
        } else {
            if(consecutiveWins == WIN_THRESHOLD) {
                epsilon = Math.max(epsilon - DELTA_EPSILON, MIN_EPSILON);
                consecutiveLoss = 0;
                consecutiveWins = 0;
            }
        }
    }

    /**
     * Erstellt und zeigt ein Diagramm mit den Gewinnen über die Zeit.
     */
    private void createAndShowChart() {
        JFreeChart chart = createChart(createDataset());
        chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(800, 400));

        JFrame frame = new JFrame("Wins Over Time");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(chartPanel);
        frame.pack();
        frame.setVisible(true);
    }

    /**
     * Erstellt ein Datenset für das Diagramm basierend auf der Historie der Gewinne.
     */
    private XYDataset createDataset() {
        DefaultXYDataset dataset = new DefaultXYDataset();
        double[][] data = new double[2][winsHistory.size()];

        for (int i = 0; i < winsHistory.size(); i++) {
            data[0][i] = i;
            data[1][i] = winsHistory.get(i);
        }

        dataset.addSeries("Wins Over Time", data);
        return dataset;
    }

    /**
     * Erstellt ein Liniendiagramm basierend auf dem übergebenen Datenset.
     */
    private JFreeChart createChart(XYDataset dataset) {
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Wins Over Time",
                "Game Number",
                "Wins",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        XYPlot plot = chart.getXYPlot();
        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        plot.setDomainPannable(true);
        plot.setRangePannable(true);

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, false);
        plot.setRenderer(renderer);

        return chart;
    }

    /**
     * Aktualisiert die Anzahl der Gewinne und aktualisiert das Diagramm.
     */
    public void updateWins(int newWins) {
        wins = newWins;
        winsHistory.add(wins);

        if (winsHistory.size() > 1000) {
            winsHistory.remove(0);
        }

        if (chartPanel != null) {
            JFreeChart chart = createChart(createDataset());
            chartPanel.setChart(chart);
        }
    }

    /**
     * Setzt den Zustand des Bots für einen neuen Durchlauf zurück.
     */
    @Override
    public void reset() {
        if (turnNumber == 15) {
            turnNumber = 0;
            gamePointsHistory.add(myPoints);
            if (myPoints > hisPoints) {
                wins++;
                consecutiveWins++;
                consecutiveLoss = 0;
            } else {
                consecutiveLoss++;
                consecutiveWins = 0;
            }

            updateWins(wins);

            pastGames.add(new Counter(hisCardsPlayed, specialCardsPlayed));
            evaluation();
            patternAnalysis();
            if (nextStrategy != null) {
                currentStrategy = nextStrategy;
                usedStrategies.add(nextStrategy);
                ((MyBot) currentStrategy).reset();
            }
            gameNumber++;
            adjustEpsilon();
        }
        super.reset();
    }

    /**
     * Wählt eine Karte basierend auf der aktuellen Strategie oder der ersten Runde.
     */
    @Override
    public int gibKarte(int nextCard) {
        int myCard = 0;
        if(nextStrategy == null)
            myCard = firstRun(nextCard);
        else
            myCard = currentStrategy.giveCard(nextCard);
        giveCard(nextCard, myCard);
        return myCard;
    }

    /**
     * Führt eine Musteranalyse durch, um die Leistung neuer Strategien zu bewerten.
     * Diese Methode vergleicht eine neue Strategie mit allen vergangenen Spielen, um ihre Wirksamkeit zu beurteilen.
     * Basierend auf dieser Analyse werden die Leistungsdaten aktualisiert und zukünftige Strategien angepasst.
     */
    private void patternAnalysis() {
        int pastGamesSize = pastGames.size();
        Strategy newStrategy = strategies.get(strategies.size() - 1); // Get the latest strategy
        int[] newStrategyPoints = new int[pastGamesSize]; // Array to store points for the new strategy

        // Evaluate the new strategy against all past games
        for (int j = 0; j < pastGamesSize; j++) {
            Counter pastStrategy = pastGames.get(j);
            for (int k = 0; k < numOfAnalysisGames; k++) {
                Collections.shuffle(specialCardsPlayed);
                simulation.resetSimulation();
                simulation.setSpecialCards(specialCardsPlayed);
                simulation.setStrategies(newStrategy, pastStrategy);
                newStrategyPoints[j] += simulation.playGame();
            }
        }
        // Update the performance data for the new strategy
        strategyPerformanceData.add(newStrategyPoints);

        // Update performance data for past strategies to align with the new strategy
        updatePastStrategyPerformanceData(pastGamesSize);

        // Analyze the performance and update future strategies
        Map<Strategy, Double> performanceResults = analyzeStrategyPerformance();
        updateFutureStrategies(performanceResults);
    }

    /**
     * Aktualisiert die Leistungsdaten vergangener Strategien, um sie mit neuen Strategien abzugleichen.
     * Erweitert die vorhandenen Leistungsdaten, um sie mit der aktuellen Größe der vergangenen Spiele zu synchronisieren.
     */
    private void updatePastStrategyPerformanceData(int pastGamesSize) {
        for (int i = 0; i < strategyPerformanceData.size() - 1; i++) {
            int[] currentData = strategyPerformanceData.get(i);
                int[] extendedData = new int[pastGamesSize];
                System.arraycopy(currentData, 0, extendedData, 0, currentData.length);
                Arrays.fill(extendedData, currentData.length, extendedData.length, -1);
                strategyPerformanceData.set(i, extendedData);
        }
    }

    /**
     * Analysiert die Leistung der Strategien und berechnet den exponentiell gleitenden Durchschnitt (EMA) für jede.
     * Verwendet die EMA-Werte, um die Leistung der Strategien zu bewerten.
     */
    private Map<Strategy, Double> analyzeStrategyPerformance() {
        Map<Strategy, Double> strategyPerformance = new HashMap<>();
        for (int i = 0; i < strategies.size(); i++) {
            double ema = calculateExponentialMovingAverage(strategyPerformanceData.get(i));
            strategyPerformance.put(strategies.get(i), ema);
        }
        return strategyPerformance;
    }

    /**
     * Berechnet den exponentiell gleitenden Durchschnitt (EMA) für eine gegebene Reihe von Punkten.
     * Behandelt '-1'-Werte als spezielle Fälle, indem sie leicht angepasst werden, bevor sie in die Berechnung einfließen.
     */
    private double calculateExponentialMovingAverage(int[] points) {
        double ema = 0.0;
        boolean isFirstValidPoint = true;

        for (int point : points) {
            double adjustedPoint = point;
            if (point == -1) {
                adjustedPoint *= 0.995;
            }

            if (isFirstValidPoint) {
                ema = adjustedPoint; // Initialize EMA with the first (adjusted) data point
                isFirstValidPoint = false;
            } else {
                ema = smoothingFactor * adjustedPoint + (1 - smoothingFactor) * ema;
            }
        }

        return ema;
    }

    /**
     * Aktualisiert die zukünftige Strategie basierend auf den Leistungsergebnissen.
     * Entscheidet zwischen Exploration (Auswahl einer zufälligen Strategie) und Exploitation
     * (Auswahl der leistungsstärksten Strategie basierend auf den EMA-Werten), abhängig von einem Zufallswert
     * und dem Epsilon-Wert.
     */
    private void updateFutureStrategies(Map<Strategy, Double> performanceResults) {
        if (Math.random() < epsilon) {
            // Exploration: Choose a random strategy
            int randomIndex = Utils.random.nextInt(strategies.size());
            nextStrategy = strategies.get(randomIndex);
        } else {
            // Exploitation: Choose the best-performing strategy based on EMA scores
            List<Strategy> sortedStrategies = performanceResults.entrySet().stream()
                    .sorted(Map.Entry.<Strategy, Double>comparingByValue().reversed())
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            if (!sortedStrategies.isEmpty()) {
                nextStrategy = sortedStrategies.get(0);
            }
        }
    }

    /**
     * Bewertet verschiedene Strategien basierend auf Simulationsergebnissen und wählt die effektivste Strategie aus.
     * Diese Methode verwendet eine Kombination aus Permutationsanalyse und Simulation, um die leistungsstärksten
     * Strategien zu identifizieren und zur Strategieliste hinzuzufügen.
     */
    public void evaluation() {
        int[] permArray; // Array to store successful permutations
        Set<int[]> winningStrategies = new HashSet<>();

        while (winningStrategies.isEmpty()) {
            permArray = new int[15];
            Arrays.fill(permArray, -1);

            for (int i = 1; i < 16; i++) { // Iterate over numbers 1 to 15
                int[] winPoints = new int[15];

                for (int position = 0; position < 15; position++) { // Iterate over positions
                    if (permArray[position] != -1) {
                        continue; // Skip locked positions
                    }

                    // Run the simulation for each permutation
                    for (int k = 0; k < numOfTestGames; k++) {
                        int[] testArray = new int[15];
                        for (int l = 0; l < 15; l++) {
                            if (permArray[l] != -1) {
                                testArray[l] = permArray[l];
                            } else {
                                testArray[l] = -1; // Reset only the positions that are not locked
                            }
                        }

                        testArray[position] = i; // Place number i in the current position

                        // Fill the rest of testArray with unique random numbers
                        ArrayList<Integer> availableNumbers = new ArrayList<>(15);
                        for (int num = 1; num <= 15; num++) {
                            int finalNum = num;
                            if (!Arrays.stream(testArray).anyMatch(x -> x == finalNum)) {
                                availableNumbers.add(num);
                            }
                        }
                        Collections.shuffle(availableNumbers); // Randomize the list of available numbers
                        int availIndex = 0;
                        for (int j = 0; j < 15; j++) {
                            if (testArray[j] == -1) {
                                testArray[j] = availableNumbers.get(availIndex++);
                            }
                        }

                        // Run the simulation with the current permutation
                        List<Integer> testArrayList = new ArrayList<>(testArray.length);
                        for (int value : testArray) {
                            testArrayList.add(value);
                        }
                        simulation.resetSimulation();
                        simulation.setSpecialCards(specialCardsPlayed);
                        simulation.setStrategies(new Last(testArrayList), new Last(hisCardsPlayed));
                        int pointsWon = simulation.playGame();
                        if (pointsWon > 35) {
                            winningStrategies.add(testArray.clone()); // Clone the array before adding
                        }
                        winPoints[position] += pointsWon;
                    }
                }

                // Find the most successful position for number i
                int winnerIndex = -1;
                int maxPoints = -1;
                for (int j = 0; j < winPoints.length; j++) {
                    if (winPoints[j] > maxPoints && permArray[j] == -1) {
                        winnerIndex = j;
                        maxPoints = winPoints[j];
                    }
                }

                if (winnerIndex != -1) {
                    permArray[winnerIndex] = i;
                }
            }
        }

        int mostPoints = 0;
        //Final Test
        Strategy finalStrategy = null;
        Strategy hisStrategy = new Counter(hisCardsPlayed, specialCardsPlayed);
        for(int[] strategy : winningStrategies) {
            Strategy theStrategy = new Counter(Arrays.stream(strategy).boxed().collect(Collectors.toList()), specialCardsPlayed);
            int points = 0;
            for(int i = 0; i<5; i++) {
                Collections.shuffle(specialCardsPlayed);
                simulation.resetSimulation();
                simulation.setSpecialCards(specialCardsPlayed);
                simulation.setStrategies(theStrategy, hisStrategy);
                points += simulation.playGame();
            }
            if(points > mostPoints) {
                finalStrategy = theStrategy;
                mostPoints = points;
            }
        }
        if(finalStrategy != null) {
            strategies.add(finalStrategy);
        }
    }

    /**
     * Wählt eine Karte in der ersten Runde aus.
     */
    private int firstRun(int nextCard) {
        return myCards.get(Utils.random.nextInt(myCards.size()));
    }
}