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


// engenerring
#include <wiringPi.h>
#include <wiringPiSPI.h>


// project specific
#include "Options.h"
#include "./tools/include/threads.h"

// spi display
#include "Display.h"

using namespace std;
using namespace std::chrono;


// GPIO Pins
#define ClockOutPin 1
#define ClockInPin  4



// sps is in kHz
#define minSPS 5
#define maxSPS 75


// trigger voltages to start sampling 
#define triggerMin 0.99
#define triggerMax 2.95


// methods
int  main(int argc, char ** argv);
void pwmDutyCycle(int pwmOutputPin, int speed);


