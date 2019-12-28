Voltage Catcher
---------------

Voltage catcher uses a Raspberry Pi with a MCP3008 chip to capture line voltages and save samples to csv files.  The progam can work with or without a [ST7789 LCD display with 240×320 resolution](https://www.amazon.com/dp/B081Q79X2F).  See the wiring diagram in the readme folder for instructions on connecting the MCP3008 and/or the LED display to your Raspberry Pi.   Once the data has been captured, you can then use WinScp or your favorite SFTP/SCP utility to copy the files to your PC or laptop.


## Requirements
On your Raspberry Pi, please use the raspi-config program to enable the SPI interface.

	$ sudo raspi-config


## Prerequisites

This Library is based on [WiringPi](http://wiringpi.com/), so, you'll need make sure you 
have WiringPi installed before you can succesfully compile this library.  


## Download
Use git to download the software from github.com:

    $ cd projects   { or wherever you put downloads }
    $ git clone https://github.com/wryan67/VoltageCatcher.git
    $ cd VoltageCatcher/vc/VoltageCatcher/src


## Install

To compile this library, navigate into the src folder and use the make utility to compile 
and install the library.  The sudo is required to place the final "vc" executable into /usr/local/bin.  

    $ cd [project folder]
    $ cd VoltageCatcher/vc/VoltageCatcher/src
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

If you receive errors about a missing vc command, make sure /usr/local/bin is in root's path, or just spell out the full path on the command line ($ sudo /usr/local/bin/vc)

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

When using zeta mode, use the button on BCM GPIO pin 16 to activate the data capture and save to disk feature.  The output file will be overwritten each time a data capture is initiated.  Currently the only screen supported in zeta mode is the ST7789 LCD display with 240×320.  

    $ sudo /usr/local/bin/vc  -o /home/wryan/test.csv -s 5000 -c 0,1 -f 20 -z -x 2
    Program initialization
    -----------------------------------------------------
    Samples:            5000
    Desired SPS:        20k
    Reference voltage:  5.00
    Trigger voltage:    auto
    Trigger vector:     rising
    SPI Channel:        0
    SPI Speed:          8500000
    Output file:        /home/wryan/test.csv
    Display scale:      2.00
    -----------------------------------------------------
    setup event triggers
    trigger voltage=2.146
    taking samples...
    data capture begins...    << Button was pressed here
    trigger voltage=2.128
    taking samples...
    elapsed µs=249989
    saving results...
    end capture detected
    trigger voltage=2.113
    taking samples...
    data saved...
    actual sps=20000

