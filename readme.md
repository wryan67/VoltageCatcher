Voltage Catcher
---------------

Voltage catcher uses a Raspberry Pi and and MCP3008 chip to capture line voltages and save samples to csv files.  The progam can work with or without a ST7789 LED display.  See the wiring diagram in the readme folder for instructions on connecting the MCP3008 and/or the LED display to your Raspberry Pi.   Once the data has been captured, you can then use WinScp or your favorite SFTP/SCP utility to copy the files to your PC or laptop.


## Requirements
On your Raspberry Pi, please use the raspi-config program to enable the SPI interface.

	$ sudo raspi-config


## Prerequisites

This Library is based on [WiringPi](http://wiringpi.com/), so, you'll need make sure you 
have WiringPi installed before you can succesfully compile this library.  


## Install

To compile this library, navigate into the src folder and use the make utility to compile 
and install the library.

    $ cd [project folder]
    $ cd src
    $ make
    $ sudo make install


## Usage

Below are the options with a basic description.  

    $ sudo vc
    usage: vc -s samples
    Options:    
      -c = channels [0-7],[0-7],etc.
      -d = debug level [0-2]; default 0 (none)
      -f = desired clock frequency [5-200] kHz
      -h = suppress headers
      -i = override default spi speed
      -l = gpio load spi
      -o = output file name
      -r = reference voltage, default=5.0
      -s = samples [1-40000]
      -t = trigger voltage [+/-][0.15-4.85]; default=auto
              0 volts--disable triggering
              + volts--trigger when rising
              - volts--trigger when falling
      -v = verbose output    
      -x = sample display scale (default=1.0)
      -z = zeta mode (when using ST7789 240x320 display)

## Examples

    $ sudo vc -s 5000 -f 20 -c 0,1 -o /home/wryan/test.csv
    Program initialization
    -----------------------------------------------------
    Samples:            5000
    Desired SPS:        20k
    Reference voltage:  5.00
    Trigger voltage:    auto
    Trigger vector:     rising
    SPI Channel:        0
    SPI Speed:          4600000
    Output file:        /home/wryan/test.csv
    Display scale:      1.00
    -----------------------------------------------------
    setup event triggers
    priming...
    trigger voltage=2.156
    taking samples...
    elapsed µs=398091
    saving results...
    data saved...
    actual sps=12559

When using zeta mode, the button on BCM GPIO pin 16 to activate the data capture.  The output file will be overwritten each time a data capture is initiated.

