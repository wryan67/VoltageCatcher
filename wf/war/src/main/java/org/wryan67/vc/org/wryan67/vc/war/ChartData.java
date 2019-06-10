package org.wryan67.vc.org.wryan67.vc.war;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.chart.renderer.xy.XYStepRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.wryan67.vc.controllers.MonitorController;
import org.wryan67.vc.controllers.SessionData;
import org.wryan67.vc.models.OptionsModel;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

@WebServlet(value = {"/chart.jpg"})

public class ChartData extends HttpServlet {
    private static final Logger logger=Logger.getLogger(MonitorController.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        OptionsModel options= SessionData.getValueOrDefault(request,SessionData.SessionVar.userOptions,new OptionsModel());

        try {
            response.setContentType("img");


            XYDataset dataset = loadData();

            NumberAxis xAxis = new NumberAxis("microseconds");
            NumberAxis yAxis = new NumberAxis("Volts");

            XYLineAndShapeRenderer renderer=null;


            switch (options.chartType) {
                case spline: {
                    renderer = new XYSplineRenderer();
                    renderer.setShapesVisible(false);
                }   break;
                case line: {
                    renderer = new XYLineAndShapeRenderer();
                    renderer.setShapesVisible(false);
                }   break;
                case stepped: {
                    renderer = new XYStepRenderer();
                    renderer.setShapesVisible(false);
                } break;
                case scatter: {
                    renderer = new XYLineAndShapeRenderer();
                    renderer.setShapesVisible(true);
                    renderer.setLinesVisible(false);
                } break;
            }

            XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);

            plot.setBackgroundPaint(new Color(238,238,238));

            JFreeChart chart = new JFreeChart("Voltage Catcher", JFreeChart.DEFAULT_TITLE_FONT, plot, true);

            ChartUtilities.writeChartAsJPEG(response.getOutputStream(),chart, 1125,300);

        } catch (FileNotFoundException e) {
            logger.error("cannot read /tmp/data.csv",e);
        }
    }

    private XYDataset loadData() throws IOException {
        XYSeriesCollection dataset = new XYSeriesCollection();

        BufferedReader input = new BufferedReader(new FileReader("/tmp/data.csv"));

        boolean hasHeaders=false;
        String line = input.readLine();
        String headers[] = line.split(",");

        if (line.startsWith("sample")) {
            hasHeaders=true;
            line = input.readLine();
        }

        String parts[] = line.split(",");

        boolean verbose=false;
        int col=0;
        for (String part : parts) {
            ++col;
            if (part.equals("s")||part.equals("d")) {
                verbose=true;
                break;
            }
        }

        int tsCol=1;
        int channels=0;
        int firstChannel=0;


        if (verbose) {
            tsCol=col;
            channels=col-3;
            firstChannel=1;
        } else {
            tsCol=1;
            firstChannel=2;
            channels=parts.length-2;
        }

        logger.info(String.format("tsCol=%d firstChannel=%d channels=%d",tsCol,firstChannel,channels));


        ArrayList<XYSeries> series = new ArrayList<XYSeries>(channels);

        for (int c=0; c<channels; ++c) {
            String label=(hasHeaders)?headers[c+firstChannel].replace("volts","channel"):"volts-" + c;
            XYSeries lineplot = new XYSeries(label);
            series.add(lineplot);
            dataset.addSeries(lineplot);

        }

        do {
            for (int i=0;i<channels;++i) {
                series.get(i).add(new Double(parts[tsCol]), new Double(parts[i+firstChannel]));
            }

            line=input.readLine();
            if (line==null) {
                break;
            }
            parts = line.split(",");

        } while (true);

        return dataset;
    }
}
