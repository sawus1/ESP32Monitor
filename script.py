
import serial
import subprocess

# Replace with your actual device, e.g. /dev/ttyUSB0 or /dev/ttyACM0
SERIAL_PORT = '/dev/cu.usbmodem48CA435E48842'
BAUD_RATE = 9600

# Open the serial port
ser = serial.Serial(SERIAL_PORT, BAUD_RATE, timeout=1)

while True:
    if ser.in_waiting:
        command = ser.readline().decode().strip()
        print(f"Received command: {command}")
        
        if command == "get_systemdata":
            # Run a combination of useful Linux system commands
            commands = [
                "uname -a",
                "uptime",
                "df -h",
                "free -h"
            ]
            
            result = ""
            for cmd in commands:
                try:
                    output = subprocess.check_output(cmd, shell=True, text=True)
                    result += f"\n$ {cmd}\n{output}"
                except subprocess.CalledProcessError as e:
                    result += f"\n$ {cmd}\nError: {str(e)}\n"

            # Append end marker
            result += "###END###"
            # Send result to Arduino
            ser.write(result.encode())
