#include "Display.h"

double PI = 3.14159;

void  Handler(int signo) {
    //System Exit
    printf("\r\nHandler:exit\r\n");
    DEV_ModuleExit();

    exit(0);
}

void arcPoint(int x, int y, int radius, double degree, int* xPoint, int* yPoint) {
    double px = radius * cos(degree * PI / 180.0);
    double py = radius * sin(degree * PI / 180.0);

    int ix = x + round(px);
    int iy = y + round(py);

    *xPoint = ix;
    *yPoint = iy;
}

int imageSize = LCD_WIDTH * LCD_HEIGHT * 2;


void writeBmp(FILE* fp, UBYTE* localImage, int width, int height) {

    BMPFILEHEADER bmpFileHeader;  //Define a bmp file header structure
    BMPINFOHEADER bmpInfoHeader;  //Define a bmp info header structure

    bmpFileHeader.bType = 'M' << 8 | 'B';
    bmpFileHeader.bSize = sizeof(bmpFileHeader) + sizeof(bmpInfoHeader) + 2 * width * height;
    bmpFileHeader.bReserved1 = 0;
    bmpFileHeader.bReserved2 = 0;
    bmpFileHeader.bOffset = sizeof(bmpFileHeader) + sizeof(bmpInfoHeader);

    bmpInfoHeader.biInfoSize = sizeof(BMPINFOHEADER);
    bmpInfoHeader.biWidth = height;
    bmpInfoHeader.biHeight= width;
    bmpInfoHeader.biPlanes = 1;
    bmpInfoHeader.biBitCount = 24;
    bmpInfoHeader.biCompression = 0;
    bmpInfoHeader.bimpImageSize = 0;
    bmpInfoHeader.biXPelsPerMeter = 0;
    bmpInfoHeader.biYPelsPerMeter = 0;
    bmpInfoHeader.biClrUsed = 0;
    bmpInfoHeader.biClrImportant = 16;


    fwrite(&bmpFileHeader, sizeof(BMPFILEHEADER), 1, fp);    //sizeof(BMPFILEHEADER) must be 14
    fwrite(&bmpInfoHeader, sizeof(BMPINFOHEADER), 1, fp);    //sizeof(BMPFILEHEADER) must be 50

    for (int Ypoint = 0; Ypoint < width; ++Ypoint) {
        for (int Xpoint = 0; Xpoint < height; ++Xpoint) {
            int X, Y;
            switch (ROTATE_270) {
            case 0:
                X = Xpoint;
                Y = Ypoint;
                break;
            case 90:
                X = Paint.WidthMemory - Ypoint - 1;
                Y = Xpoint;
                break;
            case 180:
                X = Paint.WidthMemory - Xpoint - 1;
                Y = Paint.HeightMemory - Ypoint - 1;
                break;
            case 270:
                X = Ypoint;
                Y = Paint.HeightMemory - Xpoint - 1;
                break;

            default:
                return;
            }

            switch (MIRROR_HORIZONTAL) {
            case MIRROR_NONE:
                break;
            case MIRROR_HORIZONTAL:
                X = Paint.WidthMemory - X - 1;
                break;
            case MIRROR_VERTICAL:
                Y = Paint.HeightMemory - Y - 1;
                break;
            case MIRROR_ORIGIN:
                X = Paint.WidthMemory - X - 1;
                Y = Paint.HeightMemory - Y - 1;
                break;
            default:
                return;
            }

            UDOUBLE Addr = X * 2 + Y * Paint.WidthByte;
            uint16_t color = (Paint.Image[Addr] << 8) | Paint.Image[Addr + 1];
            
            uint8_t blue  = (color & 0xF800) >> 11;
            uint8_t green = (color & 0x07E0) >> 5;
            uint8_t red   = (color & 0x001f);

            red   = (red   / (double)32.0) * 256;
            green = (green / (double)64.0) * 256;
            blue  = (blue  / (double)32.0) * 256;



            fwrite(&red, 1, 1, fp);   
            fwrite(&green, 1, 1, fp);
            fwrite(&blue, 1, 1, fp);

        }
    }

    fclose(fp);
}


void displayResults(Options options, Sample  samples[maxSamples + 1][MCP3008_CHANNELS], int fps, bool writeBMP) {

    signal(SIGINT, Handler);
    UBYTE* localImage = NULL;

    localImage = (UBYTE*)malloc(imageSize);


    if (localImage == NULL) {
        printf("Failed to allocate memory for black image...\r\n");
        exit(0);
    }

    Paint_NewImage(localImage, LCD_WIDTH, LCD_HEIGHT, 0, BLACK);
    Paint_Clear(BLACK);
    Paint_SetRotate(270);


    int maxX = LCD_HEIGHT;  // 320
    int maxY = LCD_WIDTH;   // 240
    int midY = LCD_WIDTH / 2;
    //                    320           240
//    Paint_DrawCircle(LCD_HEIGHT / 2, LCD_WIDTH / 2, 25, GREEN, DRAW_FILL_EMPTY, DOT_PIXEL_2X2);

    // x-axis
    Paint_DrawLine(1, maxY, maxX, maxY, WHITE, LINE_STYLE_SOLID, DOT_PIXEL_1X1);

    for (int v = 1; v < options.refVolts; ++v) {
        int y= maxY - ((v / options.refVolts) * maxY);
        Paint_DrawLine(1, y, maxX, y, BROWN, LINE_STYLE_SOLID, DOT_PIXEL_1X1);
    }   Paint_DrawLine(1, 1, maxX, 1, DARKBLUE, LINE_STYLE_SOLID, DOT_PIXEL_1X1);


    // y-axis
    Paint_DrawLine(1, maxY, 1,    1,    WHITE, LINE_STYLE_SOLID, DOT_PIXEL_1X1);


    int lineColor[MCP3008_CHANNELS + 1] = {
        //1      2     3        4      5         6        7      8    
        //                            orange
        YELLOW, GREEN, MAGENTA, CYAN,  BRRED,   RED, LIGHTBLUE, LGRAY
    };

    char message[32];
    sprintf(message, "%4.2fv", options.refVolts);
    Paint_DrawString_EN(1, 1, message, &Font24, BLACK, LGRAY);

    if (fps > 0) {
        sprintf(message, "%d-fps", fps);
        Paint_DrawString_EN(maxX - (17 * strlen(message)), maxY - 26, message, &Font24, BLACK, LIGHTBLUE);
    }


    Sample s;
    int ly[MCP3008_CHANNELS + 1];

    for (int channelIndex = 0; channels[channelIndex] >= 0; ++channelIndex) {
        s = samples[0][channels[channelIndex]];
        ly[channelIndex] = maxY - ((s.volts / options.refVolts) * maxY);
        //printf("channelIndex=%d\n", channelIndex);
    }

    for (int x = 1; x < LCD_HEIGHT; ++x) {
        for (int channelIndex = 0; channels[channelIndex] >= 0; ++channelIndex) {
            int sx = x * options.sampleScale;
            s = samples[sx][channels[channelIndex]];
            int y = maxY - ((s.volts / options.refVolts) * maxY);
            Paint_DrawLine(x - 1, ly[channelIndex], x, y, lineColor[channels[channelIndex]], LINE_STYLE_SOLID, DOT_PIXEL_1X1);
            ly[channelIndex] = y;
        }
    }

    sprintf(message, "%d-sps", options.actualSPS);
    Paint_DrawString_EN(maxX - (17 * strlen(message)), 1, message, &Font24, BLACK, WHITE);
    strcpy(message, "ch");
    Paint_DrawString_EN(maxX - (17 * strlen(message)), 24, message, &Font24, DARKBLUE, LGRAY);

    for (int channelIndex = 0; channels[channelIndex] >= 0; ++channelIndex) {
        sprintf(message, "%d", channels[channelIndex]);
        Paint_DrawString_EN(maxX - (17 * strlen(message)), 24 * (channelIndex + 2), message, &Font24, BLACK, lineColor[channels[channelIndex]]);
    }

    long long now = currentTimeMillis();
    long long elapsed = now - options.captureMessage;
    if (elapsed < 6000) {
        sprintf(message, "Data Saved");
        Paint_DrawString_EN(maxX/2 - (17 * strlen(message)/2), maxY/2 - 24, message, &Font24, BLACK, WHITE);
        sprintf(message, "SPS: %d",options.capturedSPS);
        Paint_DrawString_EN(maxX / 2 - (17 * strlen(message) / 2), maxY / 2 , message, &Font24, BLACK, WHITE);
    }

    //memcpy(chartImage, localImage, imageSize);


    LCD_Display(localImage);

    if (writeBMP) {
        char imageFileName[512];
        char tmpstr[512];
        strcpy(imageFileName, options.sampleFileName);
        strcpy(tmpstr, options.sampleFileName);
        int l1 = strlen(tmpstr);
        strtolower(tmpstr);
        if (strcmp(&tmpstr[l1 - 5], ".csv")) {
            imageFileName[l1 - 4] = 0;
        }
        strcat(imageFileName, ".bmp");


        FILE* bmpFile = fopen(imageFileName, "w");
        if (bmpFile) {
            writeBmp(bmpFile, localImage, LCD_WIDTH, LCD_HEIGHT);
        }
    }
    free(localImage);

//printf("lcd close\n"); fflush(stdout);

//DEV_ModuleExit();
    return;
}


void displayCapturing() {
    int maxX = LCD_HEIGHT;
    int maxY = LCD_WIDTH;
    int midY = LCD_WIDTH / 2;

    UBYTE* chartImage = (UBYTE*)malloc(imageSize);

    if (chartImage == NULL) {
        printf("Failed to allocate memory for black image...\r\n");
        exit(0);
    }

    Paint_NewImage(chartImage, LCD_WIDTH, LCD_HEIGHT, 0, BLACK);
    Paint_Clear(BLACK);
    Paint_SetRotate(270);

    char message[32];
    sprintf(message, "Capturing Data");
    Paint_DrawString_EN(maxX / 2 - (17 * strlen(message) / 2), maxY / 2 - 12, message, &Font24, BLACK, WHITE);

    LCD_Display(chartImage);

    free(chartImage);
}