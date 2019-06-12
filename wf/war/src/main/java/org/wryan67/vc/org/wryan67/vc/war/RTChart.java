package org.wryan67.vc.org.wryan67.vc.war;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

@WebServlet(value = {"/rtchart.jpg"})

public class RTChart extends HttpServlet {
    private static final Logger logger=Logger.getLogger(MonitorController.class);


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        OptionsModel options= SessionData.getValueOrDefault(request,SessionData.SessionVar.userOptions,new OptionsModel());

        try {
            response.setContentType("img");


            XYDataset dataset = loadData(options);

            NumberAxis xAxis = new NumberAxis("microseconds");
            NumberAxis yAxis = new NumberAxis("Volts");


            XYPlot plot = new XYPlot(dataset, xAxis, yAxis, ChartData.getRenderer(options));

            Number min = 0;
            Number max = 150000;

            min = dataset.getX(0, 0);
            max = dataset.getX(0, dataset.getItemCount(0) - 1);

            plot.getDomainAxis().setRange(min.longValue(), max.longValue());
            // plot.setBackgroundPaint(new Color(238,238,238));

            JFreeChart chart = new JFreeChart("Voltage Catcher", JFreeChart.DEFAULT_TITLE_FONT, plot, true);

            ChartUtilities.writeChartAsJPEG(response.getOutputStream(), chart, 1125, 300);

        } catch (FileNotFoundException e) {
            logger.error("cannot read /tmp/data.csv", e);
        } catch (IOException e) {
            logger.error("rt chart generator: IOException: "+e.getMessage());
        } catch (IndexOutOfBoundsException e) {
            // no data available
        } catch (Exception e) {
            logger.error("rt chart generator failed",e);
        }
    }

    private XYDataset loadData(OptionsModel options) throws IOException {
        XYSeriesCollection dataset = new XYSeriesCollection();

        int channels=options.channels.size();


        ArrayList<XYSeries> series = VCReader.getData();

        for (int c=0; c < channels; ++c) {
            dataset.addSeries(series.get(c));
        }


        return dataset;
    }
}
