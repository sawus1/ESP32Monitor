#include <Arduino.h>
#include <string>
#include "WiFiS3.h"

#include "/Users/oleksandrsavcenko/arduino_secrets.h"
#include "headers/request_handler.h"
char ssid[] = SECRET_SSID;
char pass[] = SECRET_PASS;

int led = LED_BUILTIN;
int status = WL_IDLE_STATUS;
WiFiServer server(80);

void printWifiStatus();

void setup() {
  Serial.begin(9600);
  pinMode(led, OUTPUT);

  if(WiFi.status() == WL_NO_MODULE){
    Serial.println("Communication with WiFi module failed!");
    while(true);
  }
  
  String fv = WiFi.firmwareVersion();
  if(fv < WIFI_FIRMWARE_LATEST_VERSION){
    Serial.println("Please upgrade the firmware");
  }
  
  while(status != WL_CONNECTED){
    Serial.print("Attempting to connect to Nework named: ");
    Serial.println(ssid);

    status = WiFi.begin(ssid, pass);
    delay(10000);
  }
  server.begin();
  printWifiStatus();
}


void loop() {
  WiFiClient client = server.available();
  
  if(client){
    Serial.println("new client");
    std::string currentLine = "";
    while(client.connected()){
      if(client.available()){
        char c = client.read();
        Serial.write(c);
        if (c == '\n') {
          if (currentLine.length() == 0) {
            // Headers ended
            break; // Exit the loop after headers
          }

          // Process the request line
          if (currentLine.find("GET") != std::string::npos) {
            size_t start = currentLine.find("GET") + 4; // Skip "GET "
            size_t end = currentLine.find("HTTP");
            std::string method = currentLine.substr(start, end - start - 1); // exclude trailing space
            client.println(handleGetRequest(method) + "\n");
          }

          currentLine = ""; // Reset for next header line
        }
        else if (c != '\r') {
          currentLine += c;
        }
      }
    }
    client.stop();
    Serial.println("client disconnected");
  }
}

void printWifiStatus(){
  Serial.print("SSID: ");
  Serial.println(WiFi.SSID());

  IPAddress ip = WiFi.localIP();
  Serial.print("IP Address: ");
  Serial.println(ip);
  
  long rssi = WiFi.RSSI();
  Serial.print("signal strength(RSSI): ");
  Serial.print(rssi);
  Serial.println(" dBm");
}