#include "../include/Sample.h"

Sample::Sample() {
	this->timestamp = std::chrono::microseconds();
	this->channel = 0;
	this->bits = 0;
	this->volts = 0.0;
	this->elapsed = 0;
}



void Sample::print(FILE* outputFile, std::chrono::microseconds first, std::chrono::microseconds last) {

	//                      ts  b  e delta
	fprintf(outputFile, ",%lld,%u,%d,%lld",
		this->timestamp.count()-first.count(),
		this->bits,
		this->elapsed,
		this->timestamp.count() - last.count()
	);
}