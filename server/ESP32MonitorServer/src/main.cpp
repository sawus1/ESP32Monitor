#include <WiFi.h>
#include <ArduinoJson.h>
#include "headers/request_handler.h"
#define LED 2

WiFiServer server(5000);
String request;
WiFiClient client;
bool wifiConnected = false;

void setup() {
  Serial.begin(115200);
  pinMode(LED, OUTPUT);
  WiFi.mode(WIFI_STA);
  while(true)
  {
    if(Serial.available()){
      String info = Serial.readStringUntil('\n');
      info.trim();
      int comma = info.indexOf(',');
      String ssid = info.substring(0, comma);
      String password = info.substring(comma+1);
      WiFi.begin(ssid.c_str(), password.c_str());
      unsigned long startAttemptTime = millis();
      const unsigned long timeout = 10000;
      while(WiFi.status() != WL_CONNECTED && millis() - startAttemptTime < timeout){
        delay(500);
      }
      if(WiFi.status() == WL_CONNECTED)
      {
        Serial.println("Connected, Device IP: " + WiFi.localIP().toString());
        digitalWrite(LED, HIGH);
        break;
      }
      else
      {
        Serial.println("Connection Failed!");
        WiFi.disconnect();
      }
    }
  }
  server.begin();
}

void loop() {
  client = server.available();
  if(!client){
    return;
  }
  for(int i = 0; i < 4; i++){
    digitalWrite(LED, LOW);
    delay(100);
    digitalWrite(LED, HIGH);
    delay(100);
  }
  while(client.connected())
  {
    if(client.available())
    {
      char c = client.read();
      request += c;
      if(c == '\n')
      {
        request.trim();
        if(request == "/getSysInfo")
        {
          systemdata::system_info info = getSystemInfo();
          JsonDocument doc;
          info.serializeSystemInfo(doc);
          String response;
          serializeJson(doc, response);
          client.print(response);
        }
        if(request == "/getMonitoringData")
        {
          monitoringdata::SystemLoadData data = getSystemLoadData();
          JsonDocument doc;
          data.serializeMonitoringData(doc);
          String response;
          serializeJson(doc, response);
          client.print(response);
        }
        request = "";
      }
    }
  }
  
}
