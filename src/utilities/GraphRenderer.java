/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 *
 * @author martin
 */
public class GraphRenderer {

    XYSeriesCollection dataset = new XYSeriesCollection();

    public void addValues(List<Integer> xValues, List<Long> yValues) {
        final XYSeries s = new XYSeries(0);
        for (int i = 0; i < xValues.size(); i++) {
//            System.out.println("[" + xValues.get(i) + ", " + yValues.get(i) + "]");
//            dataset.addValue(xValues.get(i), "", yValues.get(i));
            s.add(xValues.get(i), yValues.get(i));
        }

        dataset.addSeries(s);
    }

    public void createGraph() {
        JFreeChart lineChart = ChartFactory.createXYLineChart(
                "models test",
                "Number of Rules", "Time",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );

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
