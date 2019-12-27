#pragma once

// standard includes
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <errno.h>
#include <unistd.h>
#include <pthread.h>
#include <getopt.h>
#include <math.h>
#include <iostream>
#include <chrono>
#include <poll.h>


// engenerring
#include <wiringPi.h>
#include <wiringPiSPI.h>


// project specific
#include "Options.h"
#include "./tools/include/threads.h"
#include "./tools/include/stringUtil.h"

// spi display
#include "Display.h"
#include "DEV_Config.h"

using namespace std;
using namespace std::chrono;


// GPIO Pins
#define ClockOutPin    1
#define ClockInPin     4
#define ClockInPinBCM  23
#define DataCapturePin 27


// sps is in kHz
#define minSPS 5
#define maxSPS 200


// trigger voltages to start sampling 
#define triggerMin 0.99
#define triggerMax 2.95


// methods
int  main(int argc, char ** argv);
void pwmDutyCycle(int pwmOutputPin, int speed);


