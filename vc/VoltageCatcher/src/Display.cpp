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

void displayResults(Options options, Sample  samples[maxSamples + 1][MCP3008_CHANNELS]) {

    printf("lcd display init\n"); fflush(stdout);
    signal(SIGINT, Handler);

    DEV_ModuleInit();
    LCD_Init();
    LCD_Clear(WHITE);


    UBYTE* BlackImage;
    UDOUBLE Imagesize = LCD_WIDTH * LCD_HEIGHT * 2;
    printf("Imagesize = %ld\r\n", Imagesize);
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
    }   Paint_DrawLine(1, 1, maxX, 1, BLUE, LINE_STYLE_SOLID, DOT_PIXEL_1X1);


    // y-axis
    Paint_DrawLine(1, maxY, 1,    1,    WHITE, LINE_STYLE_SOLID, DOT_PIXEL_1X1);

    Sample s = samples[0][0];

    int ly = maxY - ((s.volts / options.refVolts) * maxY);

    for (int x = 1; x < LCD_HEIGHT; ++x) {
        int sx = x * options.sampleScale;
        s = samples[sx][0];
        int y = maxY - ((s.volts / options.refVolts) * maxY);
        Paint_DrawLine(x - 1, ly, x, y, YELLOW, LINE_STYLE_SOLID, DOT_PIXEL_1X1);
        ly = y;
    }


    LCD_Display(BlackImage);

    printf("lcd display paint\n"); fflush(stdout);

    printf("lcd close\n"); fflush(stdout);

    DEV_ModuleExit();
    return;
}
