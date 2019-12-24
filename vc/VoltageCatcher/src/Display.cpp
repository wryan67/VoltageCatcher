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

void displayResults(Options options, Sample  samples[maxSamples + 1][MCP3008_CHANNELS], int fps) {

    signal(SIGINT, Handler);


    //DEV_ModuleInit();
   // DEV_SPI_Init(90000000);

   // digitalWrite(7, HIGH);
   // digitalWrite(8, HIGH);
   // digitalWrite(26, LOW);
//    LCD_Reset();
//    LCD_Init();
    //LCD_Clear(BLACK);

    UBYTE* BlackImage;
    UDOUBLE Imagesize = LCD_WIDTH * LCD_HEIGHT * 2;
    if ((BlackImage = (UBYTE*)malloc(Imagesize)) == NULL) {
        printf("Failed to allocate memory for black image...\r\n");
        exit(0);
    }

    Paint_NewImage(BlackImage, LCD_WIDTH, LCD_HEIGHT, 0, WHITE);
    Paint_Clear(BLACK);
    Paint_SetRotate(270);


    int maxX = LCD_HEIGHT;
    int maxY = LCD_WIDTH;
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
    sprintf(message, "%4.2f", options.refVolts);
    Paint_DrawString_EN(1, 1, message, &Font24, BLACK, LGRAY);


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

    
    sprintf(message, "ch", options.refVolts);
    Paint_DrawString_EN(maxX - (17 * strlen(message)), 24, message, &Font24, DARKBLUE, LGRAY);

    for (int channelIndex = 0; channels[channelIndex] >= 0; ++channelIndex) {
        sprintf(message, "%d", channels[channelIndex]);
        Paint_DrawString_EN(maxX - (17 * strlen(message)), 24 * (channelIndex + 2), message, &Font24, BLACK, lineColor[channels[channelIndex]]);
    }

    if (fps > 0) {
        sprintf(message, "%d-fps", fps);
        Paint_DrawString_EN(maxX - (17 * strlen(message)), maxY - 26, message, &Font24, BLACK, LIGHTBLUE);
    }

    LCD_Display(BlackImage);

    free(BlackImage);
  //  printf("lcd display paint\n"); fflush(stdout);

//printf("lcd close\n"); fflush(stdout);

//DEV_ModuleExit();
    return;
}
