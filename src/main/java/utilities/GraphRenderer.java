/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.DeviationRenderer;
import org.jfree.data.xy.YIntervalSeries;
import org.jfree.data.xy.YIntervalSeriesCollection;
import org.jfree.ui.RectangleInsets;

/**
 *
 * @author martin
 */
public class GraphRenderer {

    YIntervalSeriesCollection dataset;
    String chartTitle;

    Map<String, Map<Integer, List<Long>>> seriesData;

    public GraphRenderer(String chartTitle) {
        this.seriesData = new HashMap<>();
        this.dataset = new YIntervalSeriesCollection();
        this.chartTitle = chartTitle;
    }

    public void addValuesToSeries(String seriesLabel, List<Integer> xValues, List<Long> yValues) {
        for (int i = 0; i < xValues.size(); i++) {
            if (!seriesData.get(seriesLabel).containsKey(xValues.get(i))) {
                seriesData.get(seriesLabel).put(xValues.get(i), new ArrayList<>());
            }
            seriesData.get(seriesLabel).get(xValues.get(i)).add(yValues.get(i));
        }
    }

    public void newSeries(String name) {
        seriesData.put(name, new HashMap<>());
    }

    private void finalizeSeries() {
//        vypocet odchylky z hodnot + pridanie series do datasetu

        seriesData.entrySet().forEach((serie) -> {
            YIntervalSeries newSeries = new YIntervalSeries(serie.getKey());

            serie.getValue().entrySet().forEach(point -> {
                double mean = point.getValue().stream().collect(Collectors.summingLong(Long::longValue)) / point.getValue().size();

                double meanSum = point.getValue().stream()
                        .map((y) -> Math.pow(y - mean, 2))
                        .collect(Collectors.summingDouble(Double::doubleValue));

                double stddev = Math.sqrt(meanSum / point.getValue().size());

                newSeries.add(point.getKey(), mean, mean - stddev, mean + stddev);
            });

            dataset.addSeries(newSeries);
        });
    }

    public void createGraph() {
        finalizeSeries();

        JFreeChart lineChart = ChartFactory.createXYLineChart(
                chartTitle,
                "Number of Rules", "Time (µs)",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );

        lineChart.setBackgroundPaint(Color.white);

        XYPlot plot = (XYPlot) lineChart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        DeviationRenderer renderer = new DeviationRenderer(true, false);
        Stroke stroke = new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        Random rand = new Random();
        for (int i = 0; i < dataset.getSeriesCount(); i++) {
            renderer.setSeriesStroke(i, stroke);
//            Color color = new Color((int) (Math.random() * 256), (int) (Math.random() * 256), (int) (Math.random() * 256)).darker();
//            System.out.println("utilities.GraphRenderer.createGraph(): " + i * (360 / dataset.getSeriesCount()));
            Color color = Color.getHSBColor(i * (1.0f / dataset.getSeriesCount()), 1, 1).darker();
            renderer.setSeriesPaint(i, color);
            renderer.setSeriesFillPaint(i, color);
        }
        renderer.setAlpha(0.12f);

        plot.setRenderer(renderer);

        int width = 1300;
        /* Width of the image */
        int height = 600;
        /* Height of the image */

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH-mm-ss__yyyy-MM-dd");
            File chartFile = new File("charts/" + dateFormat.format(new Date()) + ".jpg");
            ChartUtilities.saveChartAsJPEG(chartFile, lineChart, width, height);
        } catch (Exception e) {
        }
    }

}
