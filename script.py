
import serial
import subprocess
import threading
import time

SERIAL_PORT = '/dev/cu.usbmodem48CA435E48842'
BAUD_RATE = 9600

ser = serial.Serial(SERIAL_PORT, BAUD_RATE, timeout=1)

monitoring_active = False
isWriting = False

def safe_serial_write(data):
    try:
        isWriting = True
        ser.write(data.encode())
        isWriting = False
    except Exception as e:
        print(f"Serial write error: {e}")

def stream_monitoring_data():
    global monitoring_active
    while monitoring_active:
        try:
            output = subprocess.check_output(["top", "-b", "-n", "1"], text=True)
            trimmed = output[:4000] + "\n###MONITOR_END###"
            while isWriting:
                pass
            safe_serial_write(trimmed)
        except Exception as e:
            while isWriting:
                pass
            safe_serial_write(f"Monitor error: {e}\n###MONITOR_END###")
        time.sleep(2)  # Repeat every 2 seconds

def handle_command(command):
    global monitoring_active
    print(f"Received command: {command}")

    if command == "get_systemdata":
        # Run in a new thread to avoid blocking the main loop
        def send_systemdata():
            commands = [
                "uname",
                "uname -n",
                "uname -v",
                "uname -r",
                "uname -m"
            ]
            result = ""
            for cmd in commands:
                try:
                    output = subprocess.check_output(cmd, shell=True, text=True)
                    result += f"{output.strip()}$"
                except subprocess.CalledProcessError as e:
                    result += f"\n$ {cmd}\nError: {str(e)}\n"
            result += "###SYS_END###"
            while isWriting:
                pass
            safe_serial_write(result)

        threading.Thread(target=send_systemdata, daemon=True).start()

    elif command == "get_monitoring_data":
        if not monitoring_active:
            monitoring_active = True
            threading.Thread(target=stream_monitoring_data, daemon=True).start()
            while isWriting:
                pass
            safe_serial_write("###MONITOR_START###")

    elif command == "stop_monitoring":
        monitoring_active = False
        while isWriting:
            pass
        safe_serial_write("###MONITOR_STOPPED###")

# Main loop
while True:
    try:
        if ser.in_waiting:
            line = ser.readline().decode().strip()
            handle_command(line)
    except Exception as e:
        print(f"Error in main loop: {e}")

