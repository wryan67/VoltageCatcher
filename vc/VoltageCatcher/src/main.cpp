/* Voltage Catcher
 * Author: Wade ryan
 * Date:   05/23/2019
 */


#include "main.h"

Options options = Options();
Sample  samples[10* maxSamples + 1][MCP3008_CHANNELS] = { Sample() };
Sample  zetaData[10* maxSamples + 1][MCP3008_CHANNELS];
int     channels[MCP3008_CHANNELS + 1];


//static float  lastVolts;
static bool   foundMax;
static bool   foundMin;
static float  maxVoltage;
static float  minVoltage;
//static int    triggerVector;


pthread_mutex_t spiBusLock;
int zetaCount = 0;
bool dataCaptureActive = false;

static volatile bool  triggerMet;
static volatile bool  verbose = false;
static volatile bool  zetaMode = false;
static volatile bool  daemonMode = false;
static volatile bool  zetaBang = false;
static volatile bool  samplingActive = false;
static volatile int   sampleCount;
static volatile int   sampleIndex;
static volatile int   maxChannels;
static volatile float refVolts;
static volatile float lastVolts;
long long daemonSample = 0;


struct zetaStruct {
    float channelVolts[8];
};

long long lastSave;



bool setup() {

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

    if (refVolts > 4.5) {
        options.spiSpeed = 8500000;

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
        options.spiSpeed = 8500000;
    }

    if (options.spiOverride) {
        options.spiSpeed = saveSPISpeed;
    }


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
	auto firstSample = samples[0][channels[0]];

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
			fprintf(options.sampleFile, ",ch-%d", channels[channelIndex]);
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

    
    unsigned char buffer[3] = { 1, 0, 0 };
	buffer[1] = (options.channelType + channel) << 4;

    pthread_mutex_lock(&spiBusLock);
    wiringPiSPIDataRW(options.spiChannel, buffer, 3);
    pthread_mutex_unlock(&spiBusLock);

	return ((buffer[1] & 3) << 8) + buffer[2];
    
}

float getVolts(int bits) {
	return ((bits)*refVolts) / 1024.0;
}

float takeSample(int channelIndex) {

    volatile int channel = channels[channelIndex];
	Sample *sample = &samples[sampleIndex][channel];

    if (verbose) {
        if (0 > channel || channel > 7) {
        	fprintf(stderr, "readChannel encountered invalid channel: %d\n", channelIndex);
        	exit(9);
        }

        if (options.debugLevel>1) {
        	printf("taking sample %d on channel %d: ", sampleIndex, channelIndex); fflush(stdout);
        }
    }

    std::chrono::microseconds start;
    if (sampleIndex == 0 || sampleIndex==sampleCount-1 || verbose) {
        auto p1 = std::chrono::system_clock::now();
        start = duration_cast<microseconds>(p1.time_since_epoch());
        sample->timestamp = start;
    }

    unsigned char buffer[3] = { 1, 0, 0 };
    buffer[1] = (options.channelType + channel) << 4;

    pthread_mutex_lock(&spiBusLock);
    wiringPiSPIDataRW(options.spiChannel, buffer, 3);
    pthread_mutex_unlock(&spiBusLock);

	volatile int   bits  = ((buffer[1] & 3) << 8) + buffer[2];
    volatile float volts = volts = ((bits)*refVolts) / 1024.0;

    if (verbose) {
        auto p2 = std::chrono::system_clock::now();
        std::chrono::microseconds end = duration_cast<microseconds>(p2.time_since_epoch());
        sample->timestamp = start;
        sample->elapsed = end.count() - start.count();
        sample->bits = bits;
        sample->channel = channel;
    }

    sample->volts = volts;

	if (verbose && options.debugLevel>1) {
		printf(" volts=%f: \n", sample->volts); fflush(stdout);
	}
	return volts;
}


static volatile int sampleClockCounter = 0;

void* sampleClockRateTPS(void*) {
    printf("sampleClockRateTPS()::start\n");
    sampleClockCounter = 0;

    while (1) {
        usleep(1000000);
        printf("desired sps=%dk; actual SPS=%d\n", options.desiredSPSk, sampleClockCounter); //fflush(stdout);
        sampleClockCounter = 0;
    }
}



bool checkTrigger(float volts) {
	float vector = volts - lastVolts;
	lastVolts = volts;

	if (vector == 0) {
		return false;
	}

	if (options.triggerVector > 0) { // on the rise
		if (vector < 0) {    // is falling
			return false;
		} else {
			if (volts >= options.triggerVoltage) {
				if (options.debugLevel) printf("triggerd at %f volts\n", volts);
				triggerMet = true;
				return true;
			}
		}
	} else {                // on the fall  
		if (vector > 0) {   // is rising
			return false;
		} else {
			if (volts <= options.triggerVoltage) {
				if (options.debugLevel) printf("triggerd at %f volts\n", volts);
				triggerMet = true;
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
    daemonMode = false;
    samplingActive = false;
    dataCaptureActive = true;
    delay(100);
    printf("data capture begins...\n");

    displayCapturingLock();
    dataCapture();

    while (samplingActive) {
        delay(10);
    }

    printf("end capture detected\n");
    options.captureMessage = currentTimeMillis();
    sampleIndex = 0;
    daemonMode = true;
    lastSave = now;
    dataCapture();


    dataCaptureActive = false;
    piUnlock(3);
}



void takeSampleActivation(void) {
    if (!samplingActive) {
        return;
    }

    for (int i = 0; i < maxChannels; ++i) {
        takeSample(i);
    }

    if (daemonMode && zetaMode) {
        zetaBang=1;
        return;
    }


    if (++sampleIndex >= sampleCount) {
        samplingActive = false;
        dumpResults();
        sampleIndex = 0;
    }
}

void* takeSamplePolling(void*) {
    struct pollfd pfd;
    int    fd;
    char   buf[128];

    sprintf(buf, "gpio export %d in", ClockInPinBCM);
    system(buf);
    sprintf(buf, "/sys/class/gpio/gpio%d/value", ClockInPinBCM);

    if ((fd = open(buf, O_RDONLY)) < 0) {
        fprintf(stderr, "Failed, gpio %d not exported.\n", ClockInPinBCM);
        exit(1);
    }

    pfd.fd = fd;
    pfd.events = POLLPRI;

    char lastValue = 0;
    int  xread = 0;

    lseek(fd, 0, SEEK_SET);    /* consume any prior interrupt */
    read(fd, buf, sizeof buf);

    while (true) {
        //  poll(&pfd, 1, -1);         /* wait for interrupt */
        lseek(fd, 0, SEEK_SET);    /* consume interrupt */
        xread = read(fd, buf, sizeof(buf));

        if (xread > 0) {
            if (buf[0] != lastValue) {
                ++sampleClockCounter;
                lastValue = buf[0];
                takeSampleActivation();
            }
        }
    }
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

    for (int v = 1; v < refVolts; ++v) {
        int y = maxY - ((v / refVolts) * maxY);
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
    sprintf(message, "%4.2f", refVolts);
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
    samplingActive = false;
    usleep(100);

    close(options.spiHandle);
    options.spiHandle = wiringPiSPISetup(options.spiChannel, options.displaySPISpeed);
    digitalWrite(10, HIGH);
    digitalWrite(11, HIGH);
    digitalWrite(26, LOW);
    
    for (int channelIndex = 0; channels[channelIndex] >= 0; ++channelIndex) {
        Sample *s = &zetaData[1][channels[channelIndex]];
    }

    displayResults(options, zetaData, fps);

    close(options.spiHandle);
    options.spiHandle = wiringPiSPISetup(options.spiChannel, options.spiSpeed);
    digitalWrite(10, LOW);
    digitalWrite(11, LOW);
    digitalWrite(26, HIGH);
    usleep(1500);

    samplingActive = true;

}

void displayCapturingLock() {
    samplingActive = false;
    usleep(100);

    pthread_mutex_lock(&spiBusLock);

    close(options.spiHandle);
    options.spiHandle = wiringPiSPISetup(options.spiChannel, options.displaySPISpeed);
    digitalWrite(10, HIGH);
    digitalWrite(11, HIGH);
    digitalWrite(26, LOW);

    displayCapturing();

    close(options.spiHandle);
    options.spiHandle = wiringPiSPISetup(options.spiChannel, options.spiSpeed);
    digitalWrite(10, LOW);
    digitalWrite(11, LOW);
    digitalWrite(26, HIGH);
    delay(100);

    pthread_mutex_unlock(&spiBusLock);
}


void* zetaRead(void*) {
    bool firstVoltage = true;
    int  frameCount = 0;
    int  lastFPS = 0;
    int  fps = 0;
    auto beginSampleTime = std::chrono::system_clock::now();

    auto second = duration_cast<seconds>(beginSampleTime.time_since_epoch());
    auto lastSecond = second;


    while (true) {
        while (!zetaBang); zetaBang = 0;

        int channel = channels[0];
        if (firstVoltage) {
            resetTrigger(samples[0][channel].volts);
            firstVoltage = false;
        }

        if (!checkTriggerZeta(samples[0][channel].volts)) {
            continue;
        }

        if (zetaCount == 0) {
            beginSampleTime = std::chrono::system_clock::now();
        }


        for (int channelIndex = 0; channels[channelIndex] >= 0; ++channelIndex) {
            channel = channels[channelIndex];
            Sample* s = &zetaData[zetaCount][channel];
            s->channel = channels[channelIndex];
            s->volts = samples[0][channel].volts;
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
            resetTrigger(samples[0][channels[0]].volts);
        }
    }

    printf("zetaRead-ends\n"); fflush(stdout);

}

void setupZeta() {
    if (pthread_mutex_init(&spiBusLock, NULL) != 0) {
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

    for (int i = 0; i < sampleCount; ++i) {
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
    if (options.autoTrigger) {
        options.triggerVoltage = min + (max - min) / 2;
        printf("trigger voltage=%5.3f\n", options.triggerVoltage);
    }

    if (min > max) {
        fprintf(stderr, "Not enough voltage variance to detect cycle\n");
        exit(0);
    }


    volts = getVolts(readChannel(channels[0]));
    if (options.debugLevel) printf("starting volts:     %f\n", volts);

    resetTrigger(volts);

    while (!checkTriggerZeta(getVolts(readChannel(channels[0]))));

    printf("taking samples...\n");
    sampleIndex = 0;
    samplingActive = true;
}

int main(int argc, char **argv)
{
	if (setuid(0) != 0) {
		fprintf(stderr, "please use sudo to execute this command\n");
		exit(2);
	}
    piHiPri(99);

    char cmd[128];
    printf("Program initialization\n");


	sprintf(cmd, "ps -ef | awk '{if (/usr.local.bin.vc / && !/awk/ && $2!=%d && $2!=%d) system(sprintf(\"kill -9 %%d\",$2))}'", getpid(),getppid());
	system(cmd);

	if (!options.commandLineOptions(argc, argv)) {
		exit(1);
	}

    verbose     = options.verboseOutput;
    daemonMode  = options.daemon;
    zetaMode    = options.zetaMode;
    refVolts    = options.refVolts;
    sampleCount = options.sampleCount;
    sampleIndex = 0;


    for (int i = 0; channels[i] >= 0; ++i) {
        maxChannels = i+1;
    }
    if (verbose) {
        printf("verbose=%d\n", verbose);
        printf("maxChannels=%d\n", maxChannels);
    }

	if (!setup()) {
		printf("setup failed\n");
		exit(2);
	}

    options.displayParameters();
    printf("setup event triggers\n");


//    threadCreate(sampleClockRateTPS, "sampleRateTPS");

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





    threadCreate(takeSamplePolling, "takeSamplePoling");

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
