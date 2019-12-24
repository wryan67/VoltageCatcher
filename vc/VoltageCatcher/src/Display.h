#pragma once

#include <stdio.h>
#include <stdlib.h>
#include <signal.h>
#include <math.h>

#include "Options.h"
#include "DEV_Config.h"
#include "GUI_Paint.h"
#include "GUI_BMPfile.h"
#include "image.h"

#include "tools/include/threads.h"


void displayResults(Options options, Sample  samples[maxSamples + 1][MCP3008_CHANNELS], int fps);

void displayCapturing();