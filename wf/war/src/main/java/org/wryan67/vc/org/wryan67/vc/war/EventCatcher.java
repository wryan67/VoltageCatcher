package org.wryan67.vc.org.wryan67.vc.war;


/**
 ***  Event Catcher
 ***  Wade Ryan
 ***  2019-06-15
 **/

import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.io.gpio.event.PinEventType;
import com.pi4j.wiringpi.Gpio;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;



public class EventCatcher {
    private static final Logger logger=Logger.getLogger(EventCatcher.class);


    // Options
    private static final int frequency    = 10;
    private static final int samples      = 4096;
    private static final int maxSamples   = 40000;


    // GPIO Pins
    private static final Pin ClockOutPin  = RaspiPin.GPIO_01;
    private static final Pin ClockInPin   = RaspiPin.GPIO_04;

    private static final GpioController         gpio            = GpioFactory.getInstance();
    private static final GpioPinDigitalInput    clockInputPin   = gpio.provisionDigitalInputPin(ClockInPin, PinPullResistance.PULL_DOWN);
    private static final GpioPinPwmOutput       pwm             = gpio.provisionPwmOutputPin(ClockOutPin);


    // Counters
    private volatile  static Boolean  lock=new Boolean(false);
    private volatile  static Integer  count=0;
    private static long     frame=0;


    // Buffers
    private static long sampleTimestamps[] = new long[maxSamples];



    public static void main(String args[]) throws InterruptedException {
        startMonitor();


        logger.info("monitor started");

        while(true) {
            Thread.sleep(500);
        }

    }


    public static void stopMonitor() {
        clockInputPin.removeAllListeners();
    }


    public static void startMonitor() {
        stopMonitor();

        // pwmFrequency in Hz = 19.2e6 Hz / pwmClock / pwmRange
        // clock =      30000hz =  19.2e6 / pwmClock
        //           clock * 30000 = 19.2e6
        //           clock = 19.2e6 / 30000
        //
        Double clock = (19.2e6 / (((frequency)*1000.0)));
        int range = 2;
        int duty = 1;

        Gpio.pwmSetMode(Gpio.PWM_MODE_BAL);
        Gpio.pwmSetClock(clock.intValue());
        Gpio.pwmSetRange(range);
        pwm.setPwm(duty);


        clockInputPin.setMode(PinMode.DIGITAL_INPUT);
        clockInputPin.setPullResistance(PinPullResistance.PULL_DOWN);
        clockInputPin.setDebounce(0);
        clockInputPin.addListener(new GpioPinListenerDigital() {
            @Override
            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
                if (!event.getEventType().equals(PinEventType.DIGITAL_STATE_CHANGE)) {
                    logger.error("non digitial pin state change occured");
                }

                captureVoltage();
            }

        });

    }


    private static void captureVoltage() {

        synchronized (lock) {
            sampleTimestamps[count]=System.nanoTime()/1000;
            ++count;

            if (count >= samples) {
                if (((++frame)%5)==0) logResults();

                count = 0;
            }
        }
    }




    private static void logResults() {

        ArrayList<String> messages=new ArrayList<>(3000);

        messages.add("input pin="+clockInputPin.getPin());

        long elapsed=0;
        try {
            elapsed=sampleTimestamps[count-1]-sampleTimestamps[0];
        } catch (Exception e) {
            return;
        }

        messages.add(String.format("frame=%-4d  samples=%-6d  elapsed=%-8s  sps=%-6.0f", frame, count, (elapsed)+"us", 1000000.0 * count / elapsed));

        messages.add("sample,timestamp,delta");

        long lastTime=sampleTimestamps[0];

        for (int i=0;i<count;++i) {
            messages.add(String.format("%d,%d,%d", i, sampleTimestamps[i], sampleTimestamps[i]-lastTime));
            lastTime=sampleTimestamps[i];
        }

        logger.info(join(messages,"\n"));
    }




    public static String join(List inputList, String separator) {
        if (separator==null) return "";
        if (inputList==null) return "";
        if (inputList.size()<1) return "";
        if (inputList.size()==1) {
            if (inputList.get(0)==null) {
                return "";
            } else {
                return inputList.get(0).toString();
            }
        }

        int elementSize=(int)(inputList.get(0).toString().length()*1.2);

        StringBuffer tmpstr=new StringBuffer(inputList.size()*elementSize);

        int i;
        for (i=0;i<inputList.size()-1;++i) {
            if (inputList.get(i)!=null) tmpstr.append(inputList.get(i));
            tmpstr.append(separator);
        }   if (inputList.get(i)!=null) tmpstr.append(inputList.get(i));

        return tmpstr.toString();
    }


}

