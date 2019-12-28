#pragma once

#include <iostream>
#include <chrono>

using namespace std;
using namespace std::chrono;

class Sample {
public:
	microseconds	timestamp;
	double			volts;
	long			elapsed;
	unsigned int	bits;
	int				channel;

	Sample();

	void print(FILE * outputFile, std::chrono::microseconds first, std::chrono::microseconds last);

};