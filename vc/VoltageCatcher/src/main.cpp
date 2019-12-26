/* Voltage Catcher
 * Author: Wade ryan
 * Date:   05/23/2019
 */


#include "main.h"

Options options = Options();
Sample  samples[maxSamples + 1][MCP3008_CHANNELS] = { Sample() };
int     channels[MCP3008_CHANNELS + 1];


Sample  chartData[maxSamples + 1][MCP3008_CHANNELS];
static bool   triggerMet;
static float  lastVolts;
static bool   foundMax;
static bool   foundMin;
static float  maxVoltage;
static float  minVoltage;
//static int    triggerVector;


pthread_mutex_t screenLock;
int zetaCount = 0;
bool dataCaptureActive = false;



bool setup() {
	printf("\nProgram initialization\n");

	if (options.loadSPIDriver && system("gpio load spi") != 0) {
		fprintf(stderr, "SPI driver failed to load: %s\n", strerror(errno));
		exit(EXIT_FAILURE);
	}

	
	if (int ret = wiringPiSetup()) {
		fprintf(stderr, "Wiring Pi setup failed, ret=%d\n", ret);
		exit(EXIT_FAILURE);
	}

	// The speed parameter is an integer in the range 500,000 through 32,000,000 and represents the SPI clock speed in Hz


	// spi limit
    int saveSPISpeed = options.spiSpeed;

    if (options.refVolts > 4.5) {
        options.spiSpeed = 9000000;

        if (options.desiredSPSk < 29) {
            options.spiSpeed = 600000;
        }
        if (options.desiredSPSk < 26) {
            options.spiSpeed = 5000000;
        }
        if (options.desiredSPSk < 23) {
            options.spiSpeed = 4600000;
        }
        if (options.desiredSPSk < 17) {
            options.spiSpeed = 4000000;
        }
        if (options.desiredSPSk < 8) {
            options.spiSpeed = 3000000;
        }

    }
    else {
        options.spiSpeed = 6000000;

        if (options.desiredSPSk < 31) {
            options.spiSpeed = 2900000;
        }
        if (options.desiredSPSk < 29) {
            options.spiSpeed = 2750000;
        }
        if (options.desiredSPSk < 26) {
            options.spiSpeed = 2500000;
        }
        if (options.desiredSPSk < 23) {
            options.spiSpeed = 2300000;
        }
        if (options.desiredSPSk < 17) {
            options.spiSpeed = 2000000;
        }
        if (options.desiredSPSk < 8) {
            options.spiSpeed = 1500000;
        }
    }

    if (options.zetaMode) {
//      options.spiSpeed =  9000000;
        options.spiSpeed =  3600000;
    }

    if (options.spiOverride) {
        options.spiSpeed = saveSPISpeed;
    }

    printf("options.spiSpeed=%d\n", options.spiSpeed);

	if ((options.spiHandle = wiringPiSPISetup(options.spiChannel, options.spiSpeed)) < 0)
	{
		fprintf(stderr, "opening SPI bus failed: %s\n", strerror(errno));
		exit(EXIT_FAILURE);
	}
	

	
	//  the following statements setup the proper input or output for their respective 
	//  inputs or outputs

	pinMode(ClockInPin, INPUT);

	pinMode(ClockOutPin, PWM_OUTPUT);


	// pwmFrequency in Hz = 19.2e6 Hz / pwmClock / pwmRange
	// clock =      30000hz =  19.2e6 / pwmClock
	//           clock * 30000 = 19.2e6
	//           clock = 19.2e6 / 30000
	//               
	int clock = (19.2e6 / (((options.desiredSPSk)*1000.0)));
	int range = 2;
	int duty = 1;

	pwmSetMode(PWM_MODE_BAL);
	pwmSetClock(clock);
	pwmSetRange(range);
	pwmDutyCycle(ClockOutPin, duty);

	if (options.debugLevel) {
		printf("desired sps(k):    %d\n", options.desiredSPSk);
		printf("pwm mode:          %s\n", "balanced");
		printf("pwm Clock:         %d\n", clock);
		printf("pwm Range:         %d\n", range);
		printf("pwm Duty:          %d\n", duty);
	}


	// pwmFrequency in Hzhttps://images-na.ssl-images-amazon.com/images/I/51nZyfKbbfL._SX425_.jpg = 19.2e6 Hz / pwmClock / pwmRange.

// random number generator
	int seed;
	FILE *fp;
	fp = fopen("/dev/urandom", "r");
	fread(&seed, sizeof(seed), 1, fp);
	fclose(fp);
	srand(seed);


	return true;
}



void dumpResults() {
	auto p2 = std::chrono::system_clock::now();
	auto end = duration_cast<microseconds>(p2.time_since_epoch());
	auto firstSample = samples[0][0];

	long elapsed = end.count() - firstSample.timestamp.count();

	
//	cout << "elapsed µs=" << elapsed2 << "\n";

	cout << "elapsed µs=" << elapsed << "\n" ;
	cout << "saving results...\n";
	

	long sumElapsed = 0;
	long minElapsed = 999999;
	long maxElapsed = 0;
	long avgElapsed = 0;

	long sumDelta = 0;
	long minDelta = 999999;
	long maxDelta = 0;
	long avgDelta = 0;
	double minVolt = 999999;
	double maxVolt = 0;

    options.sampleFile = fopen(options.sampleFileName, "w");
    if (options.sampleFile == NULL) {
        fprintf(stderr, "cannot open output file '%s': %s\n", options.sampleFileName, strerror(errno));
        exit(2);
    }


	if (!options.suppressHeaders) {

		fprintf(options.sampleFile, "sample");
		
		if (!options.verboseOutput) {
			fprintf(options.sampleFile, ",timestamp-%d", channels[0]);
		}

		for (int channelIndex = 0; channels[channelIndex] >= 0; ++channelIndex) {
			fprintf(options.sampleFile, ",volts-%d", channels[channelIndex]);
		}

		if (options.verboseOutput) {
			fprintf(options.sampleFile, ",spi,type");
			for (int channelIndex = 0; channels[channelIndex] >= 0; ++channelIndex) {
				fprintf(options.sampleFile, ",timestamp-%d,bits-%d,elapsed-%d,delta-%d",
					channels[channelIndex], channels[channelIndex],
					channels[channelIndex], channels[channelIndex]
				);
			}
		}

		fprintf(options.sampleFile, "\n");
	}


	char *type;

	if (options.channelType == MCP3008_SINGLE) {
		type = (char *)"s";
	}
	else {
		type = (char *)"d";
	}

	for (int i = 0; i < options.sampleCount; ++i) {

		Sample sample = samples[i][channels[0]];
		Sample lastSample = samples[i - 1][channels[0]];
		std::chrono::microseconds last = sample.timestamp;

		sumElapsed += sample.elapsed;
		if (sample.elapsed < minElapsed) minElapsed = sample.elapsed;
		if (sample.elapsed > maxElapsed) maxElapsed = sample.elapsed;

		if (i > 0) {
			last = lastSample.timestamp;
			long delta = sample.timestamp.count() - lastSample.timestamp.count();
			sumDelta += delta;
			if (delta < minDelta) minDelta = delta;
			if (delta > maxDelta) maxDelta = delta;
		}

		if (sample.volts < minVolt) minVolt = sample.volts;
		if (sample.volts > maxVolt) maxVolt = sample.volts;



		//               sample
		fprintf(options.sampleFile, "%d", i);

		if (!options.verboseOutput) {
			fprintf(options.sampleFile,",%lld", samples[i][channels[0]].timestamp.count()-firstSample.timestamp.count());
		}

		for (int channelIndex = 0; channels[channelIndex] >= 0; ++channelIndex) {
			fprintf(options.sampleFile, ",%f", samples[i][channels[channelIndex]].volts);
		}

		if (options.verboseOutput) {
			fprintf(options.sampleFile, ",%d,%s", options.spiChannel, type);
			for (int channelIndex = 0; channels[channelIndex] >= 0; ++channelIndex) {
				samples[i][channels[channelIndex]].print(options.sampleFile, firstSample.timestamp, last);
			}
		}


		fprintf(options.sampleFile, "\n");
	}
	fclose(options.sampleFile);
    printf("data saved...\n");
    


	avgElapsed = (double)sumElapsed / options.sampleCount;
	avgDelta   = (double)sumDelta   / options.sampleCount;

	if (options.verboseOutput) {
		printf("\nChannel %d Summary\n", channels[0]);
		printf("minElapsed,%d\n", minElapsed);
		printf("maxElapsed,%d\n", maxElapsed);
		printf("avgElapsed,%d\n", avgElapsed);

		printf("minDelta,%d\n", minDelta);
		printf("maxDelta,%d\n", maxDelta);
		printf("avgDelta,%d\n", avgDelta);

		printf("minVolt,%.3f\n", minVolt);
		printf("maxVolt,%.3f\n", maxVolt);
	}

    options.actualSPS = 1000000.0 * options.sampleCount / elapsed;
    options.capturedSPS = options.actualSPS;

	printf("sps=%d\n", options.actualSPS);


    if (!options.zetaMode) {
        options.spiHandle = wiringPiSPISetup(options.spiChannel, options.displaySPISpeed);
        digitalWrite(10, HIGH);
        digitalWrite(11, HIGH);
        digitalWrite(26, LOW);
        displayResults(options, samples, 0);
        exit(0);
    }
}


unsigned int readChannel(int channel)
{
    pthread_mutex_lock(&screenLock);

	if (0 > channel || channel > 7) {
		return -1;
	}

	unsigned char buffer[3] = { 1 };
	buffer[1] = (options.channelType + channel) << 4;

	wiringPiSPIDataRW(options.spiChannel, buffer, 3);
    

    pthread_mutex_unlock(&screenLock);

	return ((buffer[1] & 3) << 8) + buffer[2];
}

float getVolts(int bits) {
	return ((bits)*options.refVolts) / 1024.0;
}

float takeSample(int channelIndex)
{
	int channel = channels[channelIndex];

	Sample *sample = &samples[options.sampleIndex][channel];

	if (0 > channel || channel > 7) {
		fprintf(stderr, "readChannel encountered invalid channel: %d\n", channel);
		exit(9);
	}

	if (options.debugLevel>1) {
		printf("taking sample %d on channel %d: ", options.sampleIndex, channel); fflush(stdout);
	}

	auto p1 = std::chrono::system_clock::now();
	std::chrono::microseconds start = duration_cast<microseconds>(p1.time_since_epoch());

	int bits = readChannel(channel);

	auto p2 = std::chrono::system_clock::now();
	std::chrono::microseconds end = duration_cast<microseconds>(p2.time_since_epoch());

	sample->channel = channel;
	sample->timestamp = start;
	sample->elapsed = end.count() - start.count();
	sample->bits = bits;
	sample->volts = getVolts(bits);

	if (options.debugLevel>1) {
		printf(" volts=%f: \n", sample->volts); fflush(stdout);
	}
	return sample->volts;
}







bool checkTrigger(float volts) {
	float vector = volts - options.lastVolts;
	options.lastVolts = volts;

	if (vector == 0) {
		return false;
	}

	if (options.triggerVector > 0) { // on the rise
		if (vector < 0) {    // is falling
			return false;
		} else {
			if (volts >= options.triggerVoltage) {
				if (options.debugLevel) printf("triggerd at %f volts\n", volts);
				options.triggerMet = true;
				return true;
			}
		}
	} else {                // on the fall  
		if (vector > 0) {   // is rising
			return false;
		} else {
			if (volts <= options.triggerVoltage) {
				if (options.debugLevel) printf("triggerd at %f volts\n", volts);
				options.triggerMet = true;
				return true;
			}
		}
	}
    return false;
}

void breakOut(int out) {
	if (out < 1) {
        if (options.zetaMode) {
            close(options.zetaPipes[0]);
        } else {
            fclose(options.sampleFile);
        }
		exit(0);
	}
}

volatile static long long daemonSample = 0;

struct zetaStruct {
    long long sample;
    long long timestamp;
    float channelVolts[8];
};

long long lastSave;

void displayCapturingLock();

void dataCapture();
void dataCaputreActivation(void) {
    piLock(3);
    long long now = currentTimeMillis();
    long elapsed = now - lastSave;
    if (elapsed < 1000 || dataCaptureActive) {
        piUnlock(3);
        return;
    }
    dataCaptureActive = true;
    printf("data capture begins...\n");

    options.sampelingActive = false;
    options.daemon = false;
    displayCapturingLock();

    dataCapture();

    while (options.sampelingActive) {
        delay(10);
    }

    printf("end capture detected\n");
    options.captureMessage = currentTimeMillis();
    options.daemon = true;
    options.sampelingActive = true;
    lastSave = now;
    dataCaptureActive = false;
    piUnlock(3);
}

void takeSampleActivation(void) {
	piLock(1);

	if (options.sampelingActive) {

		if (options.daemon) {

			for (int i = 0; channels[i] >= 0; ++i) {
				takeSample(i);
			}

            long long timestamp = samples[options.sampleIndex][channels[0]].timestamp.count() - samples[0][channels[0]].timestamp.count();

            if (options.zetaMode) {
                struct zetaStruct zeta;
                zeta.sample = daemonSample;
                zeta.timestamp = timestamp;
                for (int i = 0; channels[i] >= 0; ++i) {
                    zeta.channelVolts[i]=samples[options.sampleIndex][channels[i]].volts;
                }
                write(options.zetaPipes[1], &zeta, sizeof(zeta));
            }
            else {
                breakOut(fprintf(options.sampleFile, "%lld,%lld", daemonSample, timestamp));

                for (int i = 0; channels[i] >= 0; ++i) {
                    breakOut(fprintf(options.sampleFile, ",%f", samples[options.sampleIndex][channels[i]].volts));
                }

                breakOut(fprintf(options.sampleFile, "\n"));
            }
            daemonSample++;

			piUnlock(1);
			return;
		}


		float volts=takeSample( 0 );

		if (!options.triggerMet) {
			if (options.debugLevel) {
				printf("options.triggerMet= %d; voltage=%f, options.lastVolts=%f\n", options.triggerMet, 
					samples[options.sampleIndex][channels[0]].volts, options.lastVolts);
			}

			if (!checkTrigger( volts )) {
                piUnlock(1);
				return;
			}
		} 


		for (int i = 1; channels[i] >= 0; ++i) {
			takeSample( i );
		}

		if (++options.sampleIndex >= options.sampleCount) {
			options.sampelingActive = false;
			dumpResults();
            options.sampleIndex = 0;
		}
	}
    piUnlock(1);
}

void pwmDutyCycle(int pwmOutputPin, int speed) {

	if (speed < 0 ) {
		speed = 0;
	}
	else if (speed > options.pwmMaxSpeed) {
		speed = options.pwmMaxSpeed;
	}

	pwmWrite(pwmOutputPin, speed);

}

void frame(long frame) {
    

    UBYTE* BlackImage;
    UDOUBLE Imagesize = LCD_WIDTH * LCD_HEIGHT * 2;
    //printf("Imagesize = %ld\r\n", Imagesize);
    if ((BlackImage = (UBYTE*)malloc(Imagesize)) == NULL) {
        printf("Failed to allocate memory for black image...\r\n");
        exit(0);
    }

    Paint_NewImage(BlackImage, LCD_WIDTH, LCD_HEIGHT, 0, WHITE);
    Paint_Clear(BLACK);
    Paint_SetRotate(270);


    int maxX = LCD_HEIGHT;
    int maxY = LCD_WIDTH;
    int midY = LCD_WIDTH / 2;
    //                    320           240
//    Paint_DrawCircle(LCD_HEIGHT / 2, LCD_WIDTH / 2, 25, GREEN, DRAW_FILL_EMPTY, DOT_PIXEL_2X2);

    // x-axis
    Paint_DrawLine(1, maxY, maxX, maxY, WHITE, LINE_STYLE_SOLID, DOT_PIXEL_1X1);

    for (int v = 1; v < options.refVolts; ++v) {
        int y = maxY - ((v / options.refVolts) * maxY);
        Paint_DrawLine(1, y, maxX, y, BROWN, LINE_STYLE_SOLID, DOT_PIXEL_1X1);
    }   Paint_DrawLine(1, 1, maxX, 1, DARKBLUE, LINE_STYLE_SOLID, DOT_PIXEL_1X1);


    // y-axis
    Paint_DrawLine(1, maxY, 1, 1, WHITE, LINE_STYLE_SOLID, DOT_PIXEL_1X1);


    int lineColor[MCP3008_CHANNELS + 1] = {
        //1      2     3        4      5         6        7      8    
        //                            orange
        YELLOW, GREEN, MAGENTA, CYAN,  BRRED,   RED, LIGHTBLUE, LGRAY
    };

    char message[32];
    sprintf(message, "%4.2f", options.refVolts);
    Paint_DrawString_EN(1, 1, message, &Font24, BLACK, LGRAY);

    sprintf(message, "%ld-frame", frame);
    Paint_DrawString_EN(maxX - (17 * strlen(message)), 1, message, &Font24, BLACK, WHITE);
    LCD_Display(BlackImage);

    free(BlackImage);
}




void resetTrigger(float volts) {
    triggerMet = false;
    lastVolts = volts;

    foundMax = false;
    foundMin = false;

    maxVoltage = options.triggerVoltage * 1.1;
    minVoltage = options.triggerVoltage * 0.9;

//    triggerVector = options.triggerVector;
}

bool checkTriggerZeta(float volts) {
    if (triggerMet) return triggerMet;

    float vector = volts - lastVolts;
    lastVolts = volts;

    if (vector == 0) {
        return false;
    }

    if (options.triggerVector > 0) {  // trigger on the rise
        if (!foundMax) {
            if (volts >= maxVoltage) {
                foundMax = true;
            }
            return false;
        }
        if (!foundMin) {
            if (volts <= minVoltage) {
                foundMin = true;
            }
            return false;
        }

        if (vector < 0) {    // is falling
            return false;
        }
        else {
            if (volts >= options.triggerVoltage) {
                triggerMet = true;
                return true;
            }
        }

    }
    else {  // trigger on the fall
        if (!foundMin) {
            if (volts > minVoltage) {
                return false;
            }
            foundMin = true;
            return false;
        }
        if (!foundMax) {
            if (volts < maxVoltage) {
                return false;
            }
            foundMax = true;
        }

        if (vector > 0) {   // is rising
            return false;
        }
        else {
            if (volts <= options.triggerVoltage) {
                triggerMet = true;
                return true;
            }
        }
    }


    return triggerMet;
}



void displayChart(int fps) {

    pthread_mutex_lock(&screenLock);

    close(options.spiHandle);
    options.spiHandle = wiringPiSPISetup(options.spiChannel, options.displaySPISpeed);
    digitalWrite(10, HIGH);
    digitalWrite(11, HIGH);
    digitalWrite(26, LOW);
    
    for (int channelIndex = 0; channels[channelIndex] >= 0; ++channelIndex) {
        Sample *s = &chartData[1][channelIndex];
    }

    displayResults(options, chartData, fps);

    close(options.spiHandle);
    options.spiHandle = wiringPiSPISetup(options.spiChannel, options.spiSpeed);
    digitalWrite(10, LOW);
    digitalWrite(11, LOW);
    digitalWrite(26, HIGH);
    usleep(1500);

    pthread_mutex_unlock(&screenLock);
}

void displayCapturingLock() {

    pthread_mutex_lock(&screenLock);

    close(options.spiHandle);
    options.spiHandle = wiringPiSPISetup(options.spiChannel, options.displaySPISpeed);
    digitalWrite(10, HIGH);
    digitalWrite(11, HIGH);
    digitalWrite(26, LOW);

    for (int channelIndex = 0; channels[channelIndex] >= 0; ++channelIndex) {
        Sample* s = &chartData[1][channelIndex];
    }

    displayCapturing();

    close(options.spiHandle);
    options.spiHandle = wiringPiSPISetup(options.spiChannel, options.spiSpeed);
    digitalWrite(10, LOW);
    digitalWrite(11, LOW);
    digitalWrite(26, HIGH);

    pthread_mutex_unlock(&screenLock);
}


void* zetaRead(void*) {
    bool firstVoltage = true;
    int frameCount = 0;
    int lastFPS = 0;
    int fps = 0;
    auto beginSampleTime = std::chrono::system_clock::now();

    auto second = duration_cast<seconds>(beginSampleTime.time_since_epoch());
    auto lastSecond = second;

    struct zetaStruct zeta;


    while (read(options.zetaPipes[0], &zeta, sizeof(zeta))) {

        if (firstVoltage) {
            resetTrigger(zeta.channelVolts[0]);
            firstVoltage = false;
        }

        if (!checkTriggerZeta(zeta.channelVolts[0])) {
            continue;
        }
        if (zetaCount == 0) {
            beginSampleTime = std::chrono::system_clock::now();
        }

        int channelIndex = 0;

        Sample* s = &chartData[zetaCount][0];
        s->channel = channels[channelIndex];
        s->volts = zeta.channelVolts[0];



        for (channelIndex = 1; channels[channelIndex] >= 0; ++channelIndex) {
            Sample* s = &chartData[zetaCount][channelIndex];
            s->channel = channels[channelIndex];
            s->volts = zeta.channelVolts[channelIndex];
        }

        int maxX = LCD_HEIGHT * options.sampleScale;
        int end = (maxX > options.sampleCount) ? options.sampleCount : maxX;

        if (++zetaCount % (int)(options.sampleScale * LCD_HEIGHT) == 0) {
            auto now = std::chrono::system_clock::now();
            auto second = duration_cast<seconds>(now.time_since_epoch());

            ++fps;
            if (lastSecond != second) {
                lastFPS = fps;
                lastSecond = second;
                fps = 0;
            }

            auto p2 = std::chrono::system_clock::now();
            auto start = duration_cast<microseconds>(beginSampleTime.time_since_epoch());
            auto end = duration_cast<microseconds>(p2.time_since_epoch());

            long elapsed = end.count() - start.count();


            // fprintf(stderr, "frame=%d, count=%d, elapsed=%ld \n", ++frameCount, zetaCount, elapsed);

            options.actualSPS = 1000000.0 * zetaCount / elapsed;


            displayChart(lastFPS);


            zetaCount = 0;
            resetTrigger(zeta.channelVolts[0]);
        }
    }

    printf("zetaRead-ends\n"); fflush(stdout);

}

void setupZeta() {
    if (pthread_mutex_init(&screenLock, NULL) != 0) {
        printf("\n mutex init has failed\n");
        exit(9);
    }

    if (pipe(options.zetaPipes) < 0) {
        fprintf(stderr, "open zeta pipes failed\n");
        exit(2);
    }

    pthread_t zetaReadId = threadCreate(zetaRead, "zetaRead");


}

void dataCapture() {
    float vector = 0;
    float volts = getVolts(readChannel(channels[0]));

    float max = 0;
    float min = 999999;

    for (int i = 0; i < options.sampleCount; ++i) {
        //delayMicroseconds(10);
        volts = getVolts(readChannel(channels[0]));
        if (volts > max) max = volts;
        if (volts < min) min = volts;
    }

    max *= .9;
    min *= 1.1;

    if (options.debugLevel) {
        printf("trigger max=%f\n", max);
        printf("trigger min=%f\n", min);
    }
    fflush(stdout);
    fflush(stderr);

    if (options.triggerVoltage > 0) {
        if (max < options.triggerVoltage) {
            fprintf(stderr, "variant voltage is lower than specified trigger, max=%f\n", max);
            exit(0);
        }

        if (min > options.triggerVoltage) {
            fprintf(stderr, "variant voltage is higher than specified trigger, min=%f\n", min);
            exit(0);
        }
    }


    if (min > max) {
        fprintf(stderr, "Not enough voltage variance to detect cycle\n");
        exit(0);
    }
    volts = getVolts(readChannel(channels[0]));
    if (options.debugLevel) printf("starting volts:     %f\n", volts);

    // wait for mid-cycle
    for (int i = 0; i < options.sampleCount; ++i) {
        //delayMicroseconds(10);
        volts = getVolts(readChannel(channels[0]));

        if (options.triggerVector > 0) {
            if (volts > min) {
                break;
            }
        }
        else {
            if (volts < max) {
                break;
            }
        }
    }
    if (options.debugLevel) printf("pre-cycle:          %f\n", volts);


    // wait for cycle
    for (int i = 0; i < options.sampleCount; ++i) {
        //delayMicroseconds(10);
        volts = getVolts(readChannel(channels[0]));

        if (options.triggerVector > 0) {
            if (volts < min) {
                break;
            }
        }
        else {
            if (volts > max) {
                break;
            }
        }
    }

    if (options.debugLevel) printf("cycle start:        %f\n", volts);

    printf("taking samples...\n");
    options.lastVolts = volts;
    options.sampelingActive = true;
}

int main(int argc, char **argv)
{
	if (setuid(0) != 0) {
		fprintf(stderr, "please use sudo to execute this command\n");
		exit(2);
	}
	char cmd[128];


	sprintf(cmd, "ps -ef | awk '{if (/usr.local.bin.vc / && !/awk/ && $2!=%d && $2!=%d) system(sprintf(\"kill -9 %%d\",$2))}'", getpid(),getppid());
	system(cmd);

	if (!options.commandLineOptions(argc, argv)) {
		exit(1);
	}

 

	if (!setup()) {
		printf("setup failed\n");
		exit(2);
	}
    
    GPIO_Config();

    pinMode(10, OUTPUT);
    pinMode(11, OUTPUT);
    pinMode(26, OUTPUT);

    options.spiHandle = wiringPiSPISetup(options.spiChannel, options.displaySPISpeed);
    digitalWrite(10, HIGH);
    digitalWrite(11, HIGH);
    digitalWrite(26, LOW);

    LCD_Init();
    LCD_Clear(BLACK);

    options.spiHandle = wiringPiSPISetup(options.spiChannel, options.spiSpeed);
    digitalWrite(10, LOW);
    digitalWrite(11, LOW);
    digitalWrite(26, HIGH);



    if (options.zetaMode) {
        setupZeta();

        if (wiringPiISR(DataCapturePin, INT_EDGE_RISING, &dataCaputreActivation) < 0) {
            fprintf(stderr, "Unable to setup ISR: %s\n", strerror(errno));
            return 1;
        }
    }




	printf("setup event triggers\n");


	printf("output file: %s\n", options.sampleFileName);
	printf("daemon mode: %s\n", (options.daemon)?"true":"false");

    

	if (wiringPiISR(ClockInPin, INT_EDGE_BOTH, &takeSampleActivation) < 0)
	{
		fprintf(stderr, "Unable to setup ISR: %s\n", strerror(errno));
		return 1;
	}


    if (!options.zetaMode) {

        printf("priming...\n"); fflush(stdout);

        int primeCount = 150000;

        if (options.desiredSPSk < 30) {
            primeCount = 80000;
        }
        if (options.desiredSPSk < 25) {
            primeCount = 40000;
        }
        if (options.desiredSPSk < 15) {
            primeCount = 20000;
        }
        if (options.desiredSPSk < 10) {
            primeCount = 5000;
        }
        if (options.desiredSPSk < 5) {
            primeCount = 25000;
        }

        for (int i = 0; i < primeCount; ++i) {
            readChannel(channels[0]);
        }
    }


    dataCapture();

	while (true) {
		fflush(stdout);
		delay(1000);
	}
	return 0;

}
