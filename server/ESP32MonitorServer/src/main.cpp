#include <WiFi.h>
#include <ArduinoJson.h>
#include "headers/request_handler.h"
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "freertos/semphr.h"

#define LED 2
#define STACK_SIZE 4096

WiFiServer server(5000);
String request;
WiFiClient client;
bool wifiConnected = false;
bool isMonitor = false;
xSemaphoreHandle xMutex;

void monitoring_task(void* pvParameters);


void setup(void)
{
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
  xMutex = xSemaphoreCreateMutex();
  xTaskCreatePinnedToCore(
    monitoring_task, 
    "monitoring_task", 
    STACK_SIZE, 
    NULL, 
    1, 
    NULL,
    tskNO_AFFINITY);
}

void loop()
{
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
          if(xSemaphoreTake(xMutex, portMAX_DELAY))
          {
            systemdata::system_info info = getSystemInfo();
            JsonDocument doc;
            info.serializeSystemInfo(doc);
            String response;
            serializeJson(doc, response);
            client.print(response);
            xSemaphoreGive(xMutex);
          }
          
        }
        if(request == "/startMonitor")
        {
          isMonitor = true;   
        }
        if(request == "/stopMonitor")
        {
          isMonitor = false;
        }
        if(request == "/disconnect")
        {
          client.stop();
        }
        if(request.indexOf("/getProcInfo") != -1)
        {
          String pid = request.substring(13);
          if(xSemaphoreTake(xMutex, portMAX_DELAY))
          {
            monitoringdata::ProcessInfo info = getProcessInfo(pid);
            JsonDocument doc;
            info.serializeProcessInfo(doc);
            String response;
            serializeJson(doc, response);
            client.print(response);
            xSemaphoreGive(xMutex);
          }
        }
        if(request.indexOf("/killProc") != -1)
        {
          String pid = request.substring(10);
          if(killProcess(pid))
          {
            if(xSemaphoreTake(xMutex, portMAX_DELAY))
            {
              client.println("killed");
              xSemaphoreGive(xMutex);
            }
          }
        }
        if(request == "/rebootSystem")
        {
          executeCommand("sudo reboot");
        }
        request = "";
      }
    }
  }
}

void monitoring_task(void* pvParameters)
{
  while(true)
  {
    if(isMonitor && client.connected()){
      if(xSemaphoreTake(xMutex, portMAX_DELAY))
      {
        monitoringdata::SystemLoadData data = getSystemLoadData();
        JsonDocument doc;
        data.serializeMonitoringData(doc);
        String response;
        serializeJson(doc,response);
        client.print(response);
        xSemaphoreGive(xMutex);
      }
    }
    vTaskDelay(5000 / portTICK_PERIOD_MS);
  }
}
