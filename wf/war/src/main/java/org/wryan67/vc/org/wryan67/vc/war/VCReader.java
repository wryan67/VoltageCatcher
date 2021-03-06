package org.wryan67.vc.org.wryan67.vc.war;

import org.apache.log4j.Logger;
import org.jfree.data.xy.XYSeries;
import org.wryan67.vc.controllers.MonitorController;
import org.wryan67.vc.models.OptionsModel;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class VCReader implements Runnable {
    private static final Logger logger=Logger.getLogger(VCReader.class);

    private static ArrayList<XYSeries> data=new ArrayList<>();

    static Process vc=null;

    public static boolean run=false;
    public static OptionsModel options=new OptionsModel();
    private boolean triggerMet=false;
    private float  lastVolts;
    private boolean foundMax =false;
    private boolean foundMin =false;
    private int triggerVector=1;
    private float max;
    private float min ;


    public static void stopMonitor() {
        if (vc != null) {
            VCReader.run = false;
            vc.destroyForcibly();
            vc=null;
        }
    }

    public static ArrayList<XYSeries> getData() {
        synchronized (data) {
            return data;
        }
    }

    public static void startMonitor(OptionsModel options) {
        stopMonitor();

        VCReader.options=options;

        try {
            Runtime.getRuntime().exec("rm -f /tmp/data.pipe").waitFor();
            Runtime.getRuntime().exec("mknod /tmp/data.pipe p").waitFor();
            String cmd=MonitorController.buildCommand(VCReader.options, "/tmp/data.pipe");

            vc =  new ProcessBuilder(cmd.concat(" -m").split(" ")).inheritIO().start();


            new Thread(new VCReader()).start();


        } catch (IOException e) {
            logger.error(e);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        if (run) {
            logger.error("VCReader requested start, but was already running");
            return;
        }

        long count=0;
        run=true;

        int channels=options.channels.size();

        ArrayList<XYSeries> series = new ArrayList<XYSeries>(channels);

        initSeries(series, channels);

        boolean firstVoltage=true;


        try (BufferedReader br = Files.newBufferedReader(Paths.get("/tmp/data.pipe"))) {
            String line;

            while ((line = br.readLine()) != null && run) {
                String parts[] = line.split(",");
                Float currVoltage=new Float((parts[0+2]));

                if (firstVoltage) {
                    resetTrigger(currVoltage);
                    firstVoltage=false;
                }

                if (!checkTrigger(currVoltage)) {
                    continue;
                }

                if (++count%options.samples==0) {
                    saveChartData(series);
                    initSeries(series, channels);
                    count=0;
                    resetTrigger(currVoltage);
                    continue;
                }

                Float x=new Float(parts[1]);
                for (int i=0;i<channels;++i) {
                    series.get(i).add(x, new Double(parts[i+2]));
                }
          }
            if (!run) {
                logger.info("Run terminated");
            }
            if (line==null) {
                logger.info("input stopped");
            }

        } catch (IOException e) {
            System.err.format("named pipe IOException: %s%n", e);
        }
        stopMonitor();
    }

    private void resetTrigger(float volts) {
        triggerMet=options.triggerVoltage==null;
        lastVolts=volts;

        foundMax = false;
        foundMin = false;

        max = options.triggerVoltage*(float)1.1;
        min = options.triggerVoltage*(float)0.9;

        triggerVector=options.getTriggerVector();
    }

    private boolean checkTrigger(float volts) {
        if (triggerMet) return triggerMet;

        float vector = volts - lastVolts;
        lastVolts = volts;

        if (vector == 0) {
            return false;
        }

        if (triggerVector > 0) {  // trigger on the rise
            if (!foundMax) {
                if (volts >= max) {
                    foundMax = true;
                }
                return false;
            }
            if (!foundMin) {
                if (volts<=min) {
                    foundMin =true;
                }
                return false;
            }

            if (vector < 0) {    // is falling
                return false;
            } else {
                if (volts >= options.triggerVoltage) {
                    triggerMet = true;
                    return true;
                }
            }

        } else {  // trigger on the fall
            if (!foundMin) {
                if (volts>min) {
                    return false;
                }
                foundMin =true;
                return false;
            }
            if (!foundMax) {
                if (volts<max) {
                    return false;
                }
                foundMax =true;
            }

            if (vector > 0) {   // is rising
                return false;
            } else {
                if (volts <= options.triggerVoltage) {
                    triggerMet = true;
                    return true;
                }
            }
        }


        return triggerMet;
    }



    private void saveChartData(ArrayList<XYSeries> series) {
        synchronized (data) {
//            int items=series.get(0).getItemCount();
//            XYDataItem item0 = (XYDataItem) series.get(0).getItems().get(0);
//            XYDataItem item1 = (XYDataItem) series.get(0).getItems().get(1);
//
//            logger.info("row 0: X="+item0.getX()+" Y="+item0.getY());
//            logger.info("row 1: X="+item1.getX()+" Y="+item1.getY());

           // long elapsed=lastItem.getX().longValue()-firstItem.getX().longValue();

//            logger.info(String.format("elapsed = %dms  sps=%6.0f", elapsed/1000, 1000000.0 * items / elapsed));

            data.clear();
            for (int c = 0; c < series.size(); ++c) {
                data.add(series.get(c));
            }
        }
    }

    private void initSeries(ArrayList<XYSeries> series, int channels) {
        series.clear();
        for (int c=0; c < channels; ++c) {
            String label="channel-" + options.channels.get(c);
            XYSeries lineplot = new XYSeries(label);
            series.add(lineplot);
        }
    }



}
