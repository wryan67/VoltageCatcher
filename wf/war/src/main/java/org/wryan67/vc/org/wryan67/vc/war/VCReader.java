package org.wryan67.vc.org.wryan67.vc.war;

import org.apache.log4j.Logger;
import org.wryan67.vc.basetypes.VCData;
import org.wryan67.vc.controllers.MonitorController;
import org.wryan67.vc.models.OptionsModel;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class VCReader implements Runnable {
    private static final Logger logger=Logger.getLogger(MonitorController.class);

    static ArrayList<VCData> data=new ArrayList<>(40000);

    static Process vc=null;

    public static boolean run=false;
    public static OptionsModel options=new OptionsModel();

    public static void killThread() {
        if (vc != null) {
            VCReader.run = false;
            vc.destroyForcibly();
            vc=null;
        }
    }


    public static void kickThread(OptionsModel options) {
        killThread();

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

        try (BufferedReader br = Files.newBufferedReader(Paths.get("/tmp/data.pipe"))) {
            String line;
            while ((line = br.readLine()) != null && run) {
                if ((++count%1000)==0) {
                    logger.info("vc(f="+options.frequency+")::"+line);
                }
            }
            if (!run) {
                logger.info("Run terminated");
            }
            if (line==null) {
                logger.info("input stopped");
            }

        } catch (IOException e) {
            System.err.format("IOException: %s%n", e);
        }



        killThread();

    }
}
