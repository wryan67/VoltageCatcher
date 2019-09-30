package org.wryan67.vc.models;

import org.wryan67.vc.common.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class OptionsModel extends JsonObject {

    public enum OptionFields {
        samples,
        frequency,
        channels,
        triggerVoltage,
        headers,
        verbose,
        outputFormat,
        outputFilename,
        chartType;
    }

    public static final int minSamples = 2;
    public static final int maxSamples = 40000;

    public static final int minFrequency = 5;
    public static final int maxFrequency = 75;

    public static final int minChannel = 0;
    public static final int maxChannel = 7;

    public static final int minChannels = 1;
    public static final int maxChannels = 8;

    public static final float minTriggerVoltage = (float) 0.5;
    public static final float maxTriggerVoltage = (float) 3.4;


    @InputStyle(width=60)
    public Integer              samples         = 2048;
    public Integer              frequency       = 10;
    public List<Integer>        channels        = new ArrayList<Integer>(){{add(0);add(2);}};
    public Float                triggerVoltage  = (float) 1.65;
    public String               outputFilename  = "data.csv";
    public Boolean              headers         = true;
    public Boolean              verbose         = false;
    public VCOutputFormat       outputFormat    = VCOutputFormat.csv;
    public SupportedChartTypes  chartType       = SupportedChartTypes.line;
    public String               clockOutPin     = "1";


    public int getTriggerVector() {
        if (triggerVoltage==null || triggerVoltage>=0) {
            return 1;
        } else {
            return 0;
        }
    }


}
