#include "main.h"


void Options::usage() {
	fprintf(stderr, "usage: vc -s samples\n");
	fprintf(stderr, "  Options:\n");
	fprintf(stderr, "  -c = channels [0-%d],[0-%d],etc.\n", MCP3008_CHANNELS-1, MCP3008_CHANNELS-1);
	fprintf(stderr, "  -d = debug level [0-2]; default 0 (none)\n");
	fprintf(stderr, "  -f = desired clock frequency [%d-%d] kHz\n", minSPS, maxSPS);
	fprintf(stderr, "  -h = suppress headers\n");
	fprintf(stderr, "  -l = gpio load spi\n");
	fprintf(stderr, "  -m = daemon mode\n");
	fprintf(stderr, "  -o = output file name\n");
    fprintf(stderr, "  -r = reference voltage, default=3.3\n");
    fprintf(stderr, "  -s = samples [1-40000]\n");
	fprintf(stderr, "  -t = trigger voltage [+/-][%.2f-%.2f]; default=1.65\n", triggerMin, triggerMax);
	fprintf(stderr, "          0 volts--disable triggering\n");
	fprintf(stderr, "          + volts--trigger when rising\n");
	fprintf(stderr, "          - volts--trigger when falling\n");
	fprintf(stderr, "  -v = verbose output\n");

	exit(1);
}

bool Options::commandLineOptions(int argc, char **argv) {
	int c, index;

	if (argc < 2) {
		usage();
	}

	const char* shortOptions = "c:d:f:hlmo:r:s:t:v";

	static struct option longOptions[] = {
		{"channel",     required_argument, NULL, 'c'},
		{"debug",       optional_argument, NULL, 'd'},
		{"freq",        required_argument, NULL, 'f'},
		{"headers",     optional_argument, NULL, 'h'},
		{"loadSPI",     optional_argument, NULL, 'l'},
		{"daemon",      optional_argument, NULL, 'm'},
		{"output",      required_argument, NULL, 'o'},
        {"refVolts",    optional_argument, NULL, 'r'},
        {"samples",     required_argument, NULL, 's'},
		{"trigger",     optional_argument, NULL, 't'},
		{"verbose",     optional_argument, NULL, 'v'},
		{0, 0, 0, 0}
	};

	while ((c = getopt_long(argc, argv, shortOptions, longOptions, &index)) != -1) {
		switch (c) {
		case 'c': {
			memset(&channels, -1, sizeof(channels));
			char **parts = split(optarg, ",");

			for (int i = 0; parts[i] != NULL; ++i) {
				if (i >= MCP3008_CHANNELS) {
					fprintf(stderr, "too many channels max is %d\n", MCP3008_CHANNELS);
					usage();
				}
				sscanf(parts[i], "%d", &channels[i]);

				if (channels[i] < 0 || channels[i] >= MCP3008_CHANNELS) {
					fprintf(stderr, "a channel is out of range\n");
					usage();
				}
			}

		}   break;

		case 'd':
			sscanf(optarg, "%d", &debugLevel);
			break;

		case 'f':
			sscanf(optarg, "%d", &desiredSPSk);
			break;

		case 'h':
			suppressHeaders = true;
			break;

		case 'l':
			loadSPIDriver = true;
			break;

		case 'm':
			daemon = true;
			break;

		case 'o':
			sampleFileName = optarg;
			break;

        case 'r':
            sscanf(optarg, "%f", &refVolts);
            break;

		case 's':
			sscanf(optarg, "%d", &sampleCount);
			break;

		case 't':
			sscanf(optarg, "%f", &triggerVoltage);
			break;

		case 'v':
			verboseOutput = true;
			break;


		case '?':
			if (optopt == 'm' || optopt == 't')
				fprintf(stderr, "Option -%c requires an argument.\n", optopt);
			else if (isprint(optopt))
				fprintf(stderr, "Unknown option `-%c'.\n", optopt);
			else
				fprintf(stderr, "Unknown option character \\x%x.\n", optopt);

			usage();

		default:
			usage();
		}
	}



	//	for (int index = optind; index < argc; index++)
	//		printf("Non-option argument %s\n", argv[index]);


	if (1 > sampleCount || sampleCount > maxSamples) {
		fprintf(stderr, "invalid sample count\n");
		usage();
	}

	if (minSPS > desiredSPSk || desiredSPSk > maxSPS) {
		fprintf(stderr, "invalid desired clock frequence\n");
		usage();
	}


	if (triggerVoltage < 0) {
		triggerVector = -1;
	}
	triggerVoltage = abs(triggerVoltage);

	if (triggerVoltage > 0) {
		if (triggerVoltage<triggerMin || triggerVoltage>triggerMax) {
			fprintf(stderr, "invalid trigger voltage\n");
			usage();
		}
	} else {
		triggerMet = true;
	}

	printf("samples=%d\n", sampleCount);
	printf("freq=%d\n", desiredSPSk);
    printf("reference voltage=%f\n", refVolts);
    printf("trigger voltage=%f\n", triggerVoltage);
	printf("trigger vector=%s\n", (triggerVector > 0) ? "rising" : "falling");



	fflush(stdout);
	return true;
}

