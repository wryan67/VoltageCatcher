#pragma once

#include <uuid/uuid.h>


#include "./tools/include/Sample.h"
#include "./tools/include/bcmPWMClockDivisors.h"
#include "./tools/include/stringUtil.h"
#include "./tools/include/mcp3008.h"


// API
#define maxSamples  40000

extern int channels[MCP3008_CHANNELS + 1];


class Options {
public:

	int   channelType = MCP3008_SINGLE;
	float refVolts = 3.3;

	int   desiredSPSk = -1;
	int   sampleCount = 0;
	int   sampleIndex = 0;
	bool  sampelingActive = false;
	float lastVolts;

	float triggerVoltage = 1.65;
	int   triggerVector = 1;
	bool  triggerMet = false;

	bool daemon = false;
	bool suppressHeaders = false;
	bool verboseOutput = false;
    bool zetaMode = false;

	// PWM
	int pwmClockDivider = BCM2835_PWM_CLOCK_DIVIDER_256;
	int pwmRange = 1000;
	int pwmMaxSpeed = 1000;  // typically should be the same as range

	// SPI Options
	bool loadSPIDriver = false;
	int  spiHandle = -1;
	int  spiChannel = 0;

	int   debugLevel = 0;
	char *sampleFileName;
	FILE *sampleFile;
    int  actualSPS;
    float sampleScale = 1.0;

    long long captureMessage = 0;

    int zetaPipes[2];

// methods
	void usage();

    char* getGUID();

	bool commandLineOptions(int argc, char ** argv);

};

extern Options options;