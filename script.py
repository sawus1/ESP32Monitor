import serial
import serial.tools.list_ports
import time
import subprocess

def ESP32_detect():
    ports = serial.tools.list_ports.comports()
    for port in ports:
        desc = port.description.lower()
        manuf = (port.manufacturer or "").lower()
        
        if "ch340" in desc or "cp210" in desc or "silicon labs" in manuf or "ftdi" in desc:
            return port.device

    return None

port = ESP32_detect()
while not port:
    print("\nPlease insert the device and press ENTER\n")
    input()
    port = ESP32_detect()

print(f"ESP32 detected on port {port}!\n")

ser = serial.Serial(port=port, baudrate=115200, timeout=None)
time.sleep(2)


while(True):
    wlan = input("\nPlease write your WLAN Name: ")
    password = input("\nPlease write your WLAN Password: ")

    ser.write(f'{wlan},{password}\n'.encode())
    time.sleep(1)

    response = ser.readline().decode().strip()
    print(response)
    if 'Connected' in response:
        print('WiFi Connected. Now entering command mode...')
        break
    else:
        print('Failed to connect. Please try again.')
        continue

try:
    while True:
       cmd = ser.readline().decode().strip()
       if cmd.lower() in ['exit', 'quit']:
           print('\nExiting command mode. \n')
           break

       print(f"Received command: {cmd}")
       try:
           result = subprocess.run(cmd, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True, timeout=10)
           output = result.stdout + result.stderr + '$'
           if not output:
               output = "Command executed, but no output\n"
       except subprocess.TimeoutExpired:
            output = "Command timed out.\n"
       except Exception as e:
            output = f"Error executing command: {str(e)}\n"

       for line in output.splitlines():
            ser.write((line[:250] + '\n').encode())

except KeyboardInterrupt:
    print('\nInterrupted by user. Exiting...')
finally:
    ser.close()






    
