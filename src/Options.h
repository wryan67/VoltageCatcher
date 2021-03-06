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
	float refVolts = 5.0;

	int   desiredSPSk = -1;
	int   sampleCount = 0;
	int   sampleIndex = 0;
	float lastVolts;

    bool  autoTrigger = true;
	float triggerVoltage = 0;
	int   triggerVector = 1;

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
    bool spiOverride = false;
    int  spiSpeed = -1;
//    int  displaySPISpeed = 90000000;  // max speed on short cable
    int  displaySPISpeed = 60000000;  // can go faster if display cable is really short

	int   debugLevel = 0;
	char *sampleFileName;
	FILE *sampleFile;
    int   actualSPS;
    int   capturedSPS;
    float sampleScale = 1.0;

    long long captureMessage = 0;

    int zetaPipes[2];

// methods
	void  usage();
    void  displayParameters();
    char* getGUID();
    bool  commandLineOptions(int argc, char ** argv);
};

extern Options options;