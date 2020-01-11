#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include "../include/stringUtil.h"

#pragma GCC diagnostic push
#pragma GCC diagnostic ignored "-Wconversion"
#pragma GCC diagnostic ignored "-Wsign-conversion"

char *strtolower(char *s) {
	if (s == 0) {
		return 0;
	}
	for (int i = 0; s[i]; ++i) {
		s[i] = tolower(s[i]);
	}
	return s;
}

char **split(char *s, char *t) {
	if (t == NULL || s == NULL) {
		return NULL;
	}
	int tokenLen = strlen(t);
	int found = 0;

	for (int i = 0; i < strlen(s) - tokenLen; ++i) {
		if (strncmp(&s[i], t, tokenLen) == 0) {
			++found;
			i += tokenLen - 1;
		}
	}

	int arraysize = (sizeof(char *))*(found + 2);
	char **parts = (char **)malloc(arraysize);
	memset(parts, 0, arraysize);


	int part = 0;
	int s1 = 0;
	int i = 0;
	for (i = 0; i < strlen(s); ++i) {
		if (strncmp(&s[i], t, tokenLen) == 0) {
			int partLen = i - s1;
			parts[part] = (char *)malloc(partLen + 1);
			strncpy(parts[part], &s[s1], partLen);
			parts[part][partLen] = 0;
			s1 = i + tokenLen;
			i += tokenLen - 1;
			++part;
		}
	}

	int partLen = i - s1;
	parts[part] = (char *)malloc(partLen);
	strncpy(parts[part], &s[s1], partLen);
	s1 = i + tokenLen;
	i += tokenLen - 1;


	return parts;
}

char *joinStringArray(char **parts) {
	if (parts == NULL) {
		return NULL;
	}
	int i = 0;
	int len = 0;
	for (i = 0; parts[i] != NULL; ++i) {
		len += strlen(parts[i]) + 1;
	}

	char *s = (char *)malloc(len);

	int p = 0;
	for (int i = 0; parts[i] != NULL; ++i) {
		strcpy(&s[p], parts[i]);
		p += strlen(parts[i]);
		strcpy(&s[p++], ",");
	}
	s[len - 1] = 0;
	return s;
}



void printStringArray(char **parts) {
	if (parts == NULL) {
		return;
	}
	int i = 0;
	for (char *p = parts[i]; p != NULL; p = parts[++i]) {
		printf("%d,%s\n", i, p);
	}
}



int strsplit(splitField* fields, int expected, const char* input, const char* fieldSeparator, void (*softError)(int fieldNumber, int expected, int actual)) {
    int i;
    int fieldSeparatorLen = strlen(fieldSeparator);
    const char* tNext, * tLast = input;

    for (i = 0; i < expected && (tNext = strstr(tLast, fieldSeparator)) != NULL; ++i) {
        unsigned int len = tNext - tLast;
        if (len >= fields[i].maxLength) {
            softError(i, fields[i].maxLength - 1, len);
            len = fields[i].maxLength - 1;
        }
        fields[i].field[len] = 0;
        strncpy(fields[i].field, tLast, len);
        tLast = tNext + fieldSeparatorLen;
    }
    if (i < expected) {
        if (strlen(tLast) > fields[i].maxLength) {
            softError(i, fields[i].maxLength, strlen(tLast));
        }
        else {
            strcpy(fields[i].field, tLast);
        }
        return i + 1;
    }
    else {
        return i;
    }
}

void ignoreSplitSoftError(int fieldNumber, int expected, int actual) {
}


int strsplit(const char* input, int expected, const char* fieldSeparator, ...) {
    va_list args;
    va_start(args, fieldSeparator);

    const char* last = input;
    const char* next;

    int ct = 0;
    while (ct < expected && (next = strstr(last, fieldSeparator)) != NULL) {
        char* target = va_arg(args, char*);
        if (target != NULL) {
            strncpy(target, last, next - last);
            target[next - last] = 0;
            ++ct;
        }
        last = next + 2;
    }
    if (ct < expected) {
        char* target = va_arg(args, char*);
        if (target != NULL) {
            strcpy(target, last);
        }
    }

    va_end(args);
    return ct + 1;
}

