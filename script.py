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

def wait_for_device():
    port = ESP32_detect()
    while not port:
        input("\nPlease insert the device and press ENTER\n")
        port = ESP32_detect()
    print(f"ESP32 detected on port {port}!\n")
    return port

def connect_to_serial(port):
    while True:
        try:
            ser = serial.Serial(port=port, baudrate=115200, timeout=3)
            time.sleep(2)
            return ser
        except serial.SerialException:
            print(f"Failed to open port {port}. Retrying...")
            time.sleep(2)
            port = wait_for_device()

def check_wifi_connection(ser):
    time.sleep(0.5)
    while True:
        try:
            ser.write(b"check_conn\n")
            response = ser.readline().decode(errors="replace").strip()
        except serial.SerialException:
            raise ConnectionError("Lost connection while checking Wi-Fi.")
        print(f"Initial response: {response}")
        if "WiFiConnected" in response:
            print("ESP32 is already connected to Wi-Fi.")
            return True
        elif "WiFiDisconnected" in response:
            return False

def provision_wifi(ser):
    while True:
        wlan = input("\nPlease write your WLAN Name: ")
        password = input("\nPlease write your WLAN Password: ")
        ser.write(f'{wlan},{password}\r\n'.encode())
        time.sleep(1)

        while True:
            try:
                response = ser.readline().decode(errors="replace").strip()
            except serial.SerialException:
                raise ConnectionError("Lost connection during Wi-Fi provisioning.")
            print(response)
            if 'Connected to Wi-Fi' in response:
                print("Connected successfully")
                return
            elif 'Wi-Fi connection failed' in response:
                print('Failed to connect. Please try again.')
                break

def main_loop(ser):
    while True:
        try:
            line = ser.readline().decode(errors="replace").strip()
        except serial.SerialException:
            raise ConnectionError("Lost connection during command loop.")
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
                output += "$"
                for line in output.splitlines():
                    ser.write((line[:250] + '\n').encode())
            except subprocess.TimeoutExpired:
                ser.write("Error executing command: Command timed out.\n").encode()
            except Exception as e:
                ser.write(f"Error executing command: {str(e)}\n").encode()
while True:
    try:
        port = wait_for_device()
        ser = connect_to_serial(port)

        wifi_connected = check_wifi_connection(ser)
        if not wifi_connected:
            provision_wifi(ser)

        main_loop(ser)

    except ConnectionError as e:
        print(f"\n{e}\nReinitializing connection...\n")
        try:
            ser.close()
        except:
            pass
        time.sleep(2)
    except KeyboardInterrupt:
        print("\nInterrupted by user. Exiting...")
        try:
            ser.close()
        except:
            pass
        break

