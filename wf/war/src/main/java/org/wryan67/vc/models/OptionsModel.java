package org.wryan67.vc.models;

import org.wryan67.vc.common.Util;

import java.util.ArrayList;
import java.util.List;

public class OptionsModel {
    public enum OptionFields {
        samples,
        frequency,
        channels,
        triggerVoltage,
        headers,
        verbose,
        outputFormat,
        outputFilename
    }

    public static final int minSamples=2;
    public static final int maxSamples=2;

    public static final int minFrequency=5;
    public static final int maxFrequency=75;

    public static final int minChannel=0;
    public static final int maxChannel=7;

    public static final int minChannels=1;
    public static final int maxChannels=8;

    public static final double minTriggerVoltage=0.5;
    public static final double maxTriggerVoltage=3.4;

    @InputStyle(width=60)
    public Integer          samples=2048;
    public Integer          frequency=10;
    public List<Integer>    channels=new ArrayList<Integer>(){{add(0);add(2);}};
    public Double           triggerVoltage=1.65;
    public String           outputFilename="data.csv";
    public Boolean          headers=true;
    public Boolean          verbose=true;
    public VCOutputFormat   outputFormat=VCOutputFormat.csv;

    @Override
    public String toString() {
        return Util.toJSON(this,this.getClass().getSimpleName());
    }
}
