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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.DeviationRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.YIntervalSeries;
import org.jfree.data.xy.YIntervalSeriesCollection;
import org.jfree.ui.RectangleInsets;

/**
 *
 * @author martin
 */
public class GraphRenderer {

    Map<Integer, List<Long>> data;

    public GraphRenderer() {
        data = new HashMap<>();
    }

    public void addValues(List<Integer> xValues, List<Long> yValues) {
        for (int i = 0; i < xValues.size(); i++) {
            if (!data.containsKey(xValues.get(i))) {
                data.put(xValues.get(i), new ArrayList<>());
            }
            data.get(xValues.get(i)).add(yValues.get(i));
        }
    }

    private XYDataset createDataset() {
        YIntervalSeriesCollection dataset = new YIntervalSeriesCollection();
        YIntervalSeries s0 = new YIntervalSeries("");

        data.entrySet().forEach(point -> {
//            double mean = (1 / point.getValue().size()) * point.getValue().stream().collect(Collectors.summingLong(Long::longValue));
            double mean = point.getValue().stream().collect(Collectors.summingLong(Long::longValue)) / point.getValue().size();

            double meanSum = point.getValue().stream()
                    .map((y) -> Math.pow(y - mean, 2))
                    .collect(Collectors.summingDouble(Double::doubleValue));

//            double stddev = Math.sqrt((1 / point.getValue().size()) * meanSum);
            double stddev = Math.sqrt(meanSum / point.getValue().size());

            s0.add(point.getKey(), mean, mean - stddev, mean + stddev);
        });

        dataset.addSeries(s0);
        return dataset;
    }

    public void createGraph() {
        XYDataset dataset = createDataset();

        JFreeChart lineChart = ChartFactory.createXYLineChart(
                "models test",
                "Number of Rules", "Time",
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
        if (dataset.getSeriesCount() == 1) {
            renderer.setSeriesStroke(0, stroke);
            renderer.setSeriesPaint(0, Color.RED);
            renderer.setSeriesFillPaint(0, Color.RED);
        } else {
//            for (int i = 0; i < dataset.getSeriesCount(); i++) {
//                renderer.setSeriesStroke(i, stroke);
//                Color color = renderer.getColorProvider().getPointColor((double)i / (double)(dataset.getSeriesCount() - 1));
//                renderer.setSeriesPaint(i, color);
//                renderer.setSeriesFillPaint(i, color);
//            }
        }
        renderer.setAlpha(0.12f);

        plot.setRenderer(renderer);

        int width = 1300;
        /* Width of the image */
        int height = 600;
        /* Height of the image */

        try {
            File chartFile = new File("LineChart.jpeg");
            ChartUtilities.saveChartAsJPEG(chartFile, lineChart, width, height);
        } catch (Exception e) {
        }
    }

}
