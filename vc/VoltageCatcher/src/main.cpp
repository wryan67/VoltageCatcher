/* Voltage Catcher
 * Author: Wade ryan
 * Date:   05/23/2019
 */


#include "main.h"


Options options = Options();
Sample  samples[maxSamples + 1][MCP3008_CHANNELS] = { Sample() };
int     channels[MCP3008_CHANNELS + 1];





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
	int spiSpeed = 6000000;

	if (options.desiredSPSk < 31) {
		spiSpeed = 2900000;
	}

	if (options.desiredSPSk < 29) {
		spiSpeed = 2750000;
	}
	if (options.desiredSPSk < 26) {
		spiSpeed = 2500000;
	}
	if (options.desiredSPSk < 23) {
		spiSpeed = 2300000;
	}
	if (options.desiredSPSk < 17) {
		spiSpeed = 2000000;
	}
	if (options.desiredSPSk < 8) {
		spiSpeed = 1500000;
	}


	if ((options.spiHandle = wiringPiSPISetup(options.spiChannel, spiSpeed)) < 0)
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


	// pwmFrequency in Hz = 19.2e6 Hz / pwmClock / pwmRange.

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

	long sps = 1000000.0 * options.sampleCount / elapsed;
	printf("sps=%'ld\n", sps);

	
	exit(0);
}


unsigned int readChannel(int channel)
{
	if (0 > channel || channel > 7) {
		return -1;
	}

	unsigned char buffer[3] = { 1 };
	buffer[1] = (options.channelType + channel) << 4;

	wiringPiSPIDataRW(options.spiChannel, buffer, 3);

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
}

void breakOut(int out) {
	if (out < 1) {
		fclose(options.sampleFile);
		exit(0);
	}
}

volatile static long long daemonSample = 0;

void takeSampleActivation(void) {
	piLock(1);

	if (options.sampelingActive) {

		if (options.daemon) {

			for (int i = 0; channels[i] >= 0; ++i) {
				takeSample(i);
			}


			breakOut(fprintf(options.sampleFile, "%lld,%lld", daemonSample++,  samples[options.sampleIndex][channels[0]].timestamp.count() - samples[0][channels[0]].timestamp.count()));

			for (int i = 0; channels[i] >= 0; ++i) {
				breakOut(fprintf(options.sampleFile, ",%f", samples[options.sampleIndex][channels[i]].volts));
			}

			breakOut(fprintf(options.sampleFile, "\n")); 

			if (options.sampleIndex == 0) {
				++options.sampleIndex;
			}


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

int main(int argc, char **argv)
{
	setuid(0);

	if (!options.commandLineOptions(argc, argv)) {
		return 1;
	}

	options.sampleFile = fopen(options.sampleFileName, "w");
	if (options.sampleFile == NULL) {
		fprintf(stderr, "cannot open output file '%s': %s\n", options.sampleFileName, strerror(errno));
		exit(2);
	}

	if (!setup()) {
		printf("setup failed\n");
		return 1;
	}

	printf("setup event triggers\n");


	printf("output file: %s\n", options.sampleFileName);
	printf("daemon mode: %s\n", (options.daemon)?"true":"false");



	if (wiringPiISR(ClockInPin, INT_EDGE_BOTH, &takeSampleActivation) < 0)
	{
		fprintf(stderr, "Unable to setup ISR: %s\n", strerror(errno));
		return 1;
	}




	printf("priming...\n");

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
		primeCount=5000;
	}
	if (options.desiredSPSk < 5) {
		primeCount = 25000;
	}

	for (int i = 0; i < primeCount; ++i) {
		readChannel(channels[0]);
	}



	float vector   = 0;
	float volts     = getVolts(readChannel(channels[0]));

	float max = 0;
	float min = 999999;

	for (int i = 0;i <options.sampleCount; ++i) {
		delayMicroseconds(10);
		volts = getVolts(readChannel(channels[0]));
		if (volts > max) max = volts;
		if (volts < min) min = volts;
	}

	max *= .9;
	min *= 1.1;

	if (options.debugLevel) {
		printf("trigger max=%f\n",max);
		printf("trigger min=%f\n",min);
	}
	fflush(stdout);
	fflush(stderr);

	if (options.triggerVoltage > 0) {
		if (max < options.triggerVoltage) {
			fprintf(stderr, "variant voltage is lower than specified trigger, max=%f\n",max);
			exit(0);
		}

		if (min > options.triggerVoltage) {
			fprintf(stderr, "variant voltage is higher than specified trigger, min=%f\n", min);
			exit(0);
		}
	}


	if (min>max) {
		fprintf(stderr, "Not enough voltage variance to detect cycle\n");
		exit(0);
	}
	volts = getVolts(readChannel(channels[0]));
	if (options.debugLevel) printf("starting volts:     %f\n", volts);

	// wait for mid-cycle
	for (int i = 0; i < options.sampleCount; ++i) {
		delayMicroseconds(10);
		volts = getVolts(readChannel(channels[0]));

		if (options.triggerVector > 0) {
			if (volts > min) {
				break;
			}
		} else {
			if (volts < max) {
				break;
			}
		}
	}
	if (options.debugLevel) printf("pre-cycle:          %f\n", volts);


	// wait for cycle
	for (int i = 0; i < options.sampleCount; ++i) {
		delayMicroseconds(10);
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

	while (true) {
		fflush(stdout);
		delay(1000);
	}
	return 0;

}
