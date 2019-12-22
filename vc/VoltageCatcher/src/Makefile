# Build command line VoltageCatcher
#
# Use this command to place "vc" into /usr/local/bin 
# $ make clean && make && make install 
#
USELIB     := USE_WIRINGPI_LIB
DIR_Config := ./tools/display/Config
DIR_Fonts  := ./tools/display/Fonts
DIR_LCD    := ./tools/display/LCD
DIR_GUI    := ./tools/display/GUI
DIR_OBJ    := ../obj
OBJ_C      := $(wildcard ${DIR_Config}/*.c ${DIR_LCD}/*.c ${DIR_GUI}/*.c  ${DIR_Fonts}/*.c)
OBJ_O      := $(patsubst %.c,${DIR_BIN}/%.o,$(notdir ${OBJ_C}))
OBJ_2      := $(addprefix $(DIR_OBJ),$(OBJ_O))


CC       := g++
CFLAGS   := -c -O2 -D $(USELIB) -std=gnu++11 
DFLAGS   := -c -O2 -D $(USELIB)
LDFLAGS  := -pthread -lwiringPi -lm -luuid
SOURCES  := main.cpp Options.cpp Display.cpp

TOOLS    := Sample.cpp stringUtil.cpp
TOOLDIR  := tools/util

TDIR     := $(addprefix $(TOOLDIR)/,$(TOOLS))
TOBJ     := $(TOOLS:%.cpp=../obj/$(TOOLDIR)/%.o)

SRC      := $(foreach sdir,$(SRCDIR),$(wildcard $(sdir)/*.cpp))

OBJECTS  := $(SOURCES:%.cpp=../obj/%.o)
HEADERS  := $(SOURCES:%.cpp=%.h)

EXECUTABLE=../bin/vc

INCLUDES := tools/include

all:   debug $(TDIR) $(HEADERS) $(SOURCES) $(EXECUTABLE)

debug:
	@echo ":::::::: all ::::::::::"
	@echo SOURCES=$(SOURCES)
	@echo TOOLS=$(TOOLS)
	@echo -      
	@echo OBJECTS=$(OBJECTS)
	@echo TOBJ=$(TOBJ)
	@echo -      
	@echo TOOLDIR=$(TOOLDIR)
	@echo TDIR=$(TDIR)
	@echo HEADERS=$(HEADERS)
	@echo SRC=$(SRC)
	@echo OBJ=$(OBJ)
	@echo OBJ_C=$(OBJ_C)
	@echo OBJ_O=$(OBJ_O)
	@echo OBJ_2=$(OBJ_2)
	@echo ":::::::: all ::::::::::"

tasks: all

$(EXECUTABLE): $(OBJECTS) $(TOBJ) $(OBJ_O)
	@echo linking...
	@mkdir -p ../bin
	@$(CC) $(OBJECTS) $(TOBJ) $(LDFLAGS) $(OBJ_2) -o $@

../obj/$(TOOLDIR)/%.o: 
	@echo compiling tool $(*D)/$(TOOLDIR)/$(*F).cpp
	@echo "$< -> $@"
	@mkdir -p ../obj/tools/util
	$(CC) $(CFLAGS) $(*D)/$(TOOLDIR)/$(*F).cpp -o $@  

../obj/%.o: %.h %.cpp
	@echo compiling module $(*D)/$(*F).cpp
	@echo "$< -> $@"
	@mkdir -p ../obj
	$(CC) $(CFLAGS) $(*D)/$(*F).cpp -o $@ -I $(DIR_Config)  -I $(DIR_LCD)  -I $(DIR_GUI)   -I $(DIR_Fonts)


${DIR_BIN}/%.o : $(DIR_GUI)/%.c
	gcc $(DFLAGS) -c  $< -o $(DIR_OBJ)/$@ $(LIB) -I $(DIR_Config)  -I $(DIR_LCD)  -I $(DIR_Fonts)

${DIR_BIN}/%.o : $(DIR_LCD)/%.c
	gcc $(DFLAGS) -c  $< -o $(DIR_OBJ)/$@ $(LIB) -I $(DIR_Config)

${DIR_BIN}/%.o : $(DIR_Fonts)/%.c
	gcc $(DFLAGS) -c  $< -o $(DIR_OBJ)/$@ $(LIB) -I $(DIR_Config)

${DIR_BIN}/%.o : $(DIR_Config)/%.c
	gcc $(DFLAGS) -c  $< -o $(DIR_OBJ)/$@ $(LIB)




install: all
	@echo Installing to /usr/local/bin
	@sudo cp ../bin/vc /usr/local/bin

clean:
	@rm -rf ../obj/ $(EXECUTABLE)

