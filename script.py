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
    input("\nPlease insert the device and press ENTER\n")
    port = ESP32_detect()

print(f"ESP32 detected on port {port}!\n")

ser = serial.Serial(port=port, baudrate=115200, timeout=3)
time.sleep(2)

# --- Check Wi-Fi connection ---
ser.write(b"CheckConn\n")
time.sleep(0.5)
wifi_connected = False
while not wifi_connected:
    response = ser.readline().decode(errors="replace").strip()
    print(f"Initial response: {response}")

    if "WiFiConnected" in response:
        print("ESP32 is already connected to Wi-Fi.")
        wifi_connected = True
    elif "Wi-Fi connection failed" in response:
        print("ESP32 is not connected to Wi-Fi.")
        break

# --- Wi-Fi provisioning loop ---
if not wifi_connected:
    while True:
        wlan = input("\nPlease write your WLAN Name: ")
        password = input("\nPlease write your WLAN Password: ")
        ser.write(f'{wlan},{password}\r\n'.encode())
        time.sleep(1)

        response = ser.readline().decode(errors="replace").strip()
        print(response)
        while 'Connected to Wi-Fi' not in response:
            response = ser.readline().decode(errors="replace").strip()
            print(response)
            if 'Wi-Fi connection failed' in response:
                print('Failed to connect. Please try again.')
                break
        if 'Connected to Wi-Fi' in response:
            print("Connected successfully")
            break

# --- Command execution loop ---
try:
    while True:
        line = ser.readline().decode(errors="replace").strip()
        if line:
            print(line)
        if line.lower() in ['exit', 'quit']:
            print('\nExiting command mode.\n')
            break

        if "COMMAND" in line:
            idx = line.find("COMMAND:")
            cmd = line[idx + len("COMMAND: "):].strip()
            print(f"Executing command: {cmd}")
            try:
                result = subprocess.run(cmd, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True, timeout=10)
                output = result.stdout + result.stderr
                if not output.strip():
                    output = "Command executed, but no output\n"
            except subprocess.TimeoutExpired:
                output = "Error executing command: Command timed out.\n"
            except Exception as e:
                output = f"Error executing command: {str(e)}\n"

            output += "$"  # Termination signal
            for line in output.splitlines():
                ser.write((line[:250] + '\n').encode())
except KeyboardInterrupt:
    print('\nInterrupted by user. Exiting...')
finally:
    ser.close()

