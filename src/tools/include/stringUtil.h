#pragma once

#include <string.h>
#include <stdio.h>
#include <stdarg.h>
#include <stdlib.h>

struct splitFieldType {
    char*           field;
    unsigned int    maxLength;
};

typedef struct splitFieldType splitField;

char *strtolower(char *s);

char **split(char *s, char *token);

char *joinStringArray(char **parts);

void printStringArray(char **parts);


int strsplit(splitField* fields, int expected, const char* input, const char* fieldSeparator, void (*softError)(int fieldNumber, int expected, int actual));

void ignoreSplitSoftError(int fieldNumber, int expected, int actual);

int strsplit(const char* input, int expected, const char* fieldSeparator, ...);

