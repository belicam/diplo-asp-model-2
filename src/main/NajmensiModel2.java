/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import core.Program;
import utilities.ProgramGenerator;
import utilities.ProgramParser;
import core.Router;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.JFrame;
import messages.InitMessage;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 *
 * @author martin
 */
public class NajmensiModel2 {

    static Router router = new Router();

    public static void createGraph(List<Long> xValues, List<Integer> yValues) throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (int i = 0; i < xValues.size(); i++) {
            System.out.println("[" + xValues.get(i) + ", " + yValues.get(i) + "]");
            dataset.addValue(xValues.get(i), "", yValues.get(i));
        }

        JFreeChart lineChart = ChartFactory.createLineChart(
                "models test",
                "Number of Rules", "Time (ms)",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );

        int width = 1300;
        /* Width of the image */
        int height = 600;
        /* Height of the image */
        File chartFile = new File("LineChart.jpeg");
        ChartUtilities.saveChartAsJPEG(chartFile, lineChart, width, height);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        int programsCount = 500;
        String[] baseLits = "a,b,c,d,e,f".split(",");
        int maxBodySize = 10;
        int maxRulesCount = 60;

        List<String> generated = ProgramGenerator.generate(programsCount, baseLits, maxRulesCount, maxBodySize);
        List<Program> programs = new ArrayList<>();

//        try (Stream<String> stream = Files.lines(Paths.get("rules1.txt"))) {
//            programs = ProgramParser.parseStream(stream);
//        } catch (IOException ex) {
//            Logger.getLogger(NajmensiModel2.class.getName()).log(Level.SEVERE, null, ex);
//        }
        programs = ProgramParser.parseStream(generated.stream());

        ExecutorService executor = Executors.newCachedThreadPool();
        programs.stream().forEach((Program p) -> {
            p.setRouter(router);
            router.addProgram(p);
            executor.execute(p);
        });

        router.sendMessage(programs.get(0).getLabel(), new InitMessage());
        executor.shutdown();

        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
        }

        List<Long> time = programs.stream()
                .filter(p -> p.isParticipationConfirmed())
                .sorted((p1, p2) -> Integer.compare(p1.getRules().size(), p2.getRules().size()))
                .map(p -> p.getEndTime() - p.getStartTime())
                .collect(Collectors.toList());

        List<Integer> rulesCount = programs.stream()
                .filter(p -> p.isParticipationConfirmed())
                .sorted((p1, p2) -> Integer.compare(p1.getRules().size(), p2.getRules().size()))
                .map(p -> p.getRules().size())
                .collect(Collectors.toList());

        try {
            createGraph(time, rulesCount);
        } catch (Exception e) {
        }
    }

}
