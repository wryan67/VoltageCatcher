# Build command line VoltageCatcher
#
# Use this command to place "vc" into /usr/local/bin 
# $ make clean && make && make install 
#

CC       := g++
CFLAGS   := -c -O2 -std=gnu++11
LDFLAGS  := -pthread -lwiringPi
SOURCES  := main.cpp Options.cpp 

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
	@echo ":::::::: all ::::::::::"

tasks: all

$(EXECUTABLE): $(OBJECTS) $(TOBJ)
	@echo linking...
	@mkdir -p ../bin
	@$(CC) $(OBJECTS) $(TOBJ) $(LDFLAGS) -o $@

../obj/$(TOOLDIR)/%.o: 
	@echo compiling $(*D)/$(TOOLDIR)/$(*F).cpp
	@echo "$< -> $@"
	@mkdir -p ../obj/tools/util
	$(CC) $(CFLAGS) $(*D)/$(TOOLDIR)/$(*F).cpp -o $@


#tools/util/Sample.cpp: 
#	@$(CC) $(CFLAGS) $(*D)/$(*F).cpp -o $@

# works
../obj/%.o: %.h %.cpp
	@echo compiling $(*D)/$(*F).cpp
	@echo "$< -> $@"
	@mkdir -p ../obj
	@$(CC) $(CFLAGS) $(*D)/$(*F).cpp -o $@

install: all
	@echo Installing to /usr/local/bin
	@sudo cp ../bin/vc /usr/local/bin

clean:
	@rm -rf ../obj/ $(EXECUTABLE)

