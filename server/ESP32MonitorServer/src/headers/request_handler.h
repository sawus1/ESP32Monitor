#include <Arduino.h>
#include <ArduinoJson.h>
#include <string>
#include "systemdata.h"

std::string sendCommandToSerial(String command);
String handleGetRequest(std::string route){
    
    String response = "HTTP/1.1 200 OK\nContent-Type: ";
    String msg;
    String content_type;
    if(route == "/L"){
        digitalWrite(LED_BUILTIN, LOW);
        msg = "OFF";
        content_type = "text/plain";
    }
    else if(route == "/H"){
        digitalWrite(LED_BUILTIN, HIGH);
        msg = "ON";
        content_type = "text/plain";
    }
    else if(route == "/systemdata") {
        content_type = "application/json";
        std::string stdresp = sendCommandToSerial("get_systemdata");
        std::string coms[5];
        auto pos = stdresp.find("$");
        int i = 0;
        while(pos != std::string::npos){
            coms[i] = stdresp.substr(0, pos);
            stdresp.erase(0, pos + 1);
            pos = stdresp.find("$");
            i++;
        }
        systemdata::sysinfo.systemName = coms[0].c_str();
        systemdata::sysinfo.networkHostname = coms[1].c_str();
        systemdata::sysinfo.kernelVersion = coms[2].c_str();
        systemdata::sysinfo.kernelRelease = coms[3].c_str();
        systemdata::sysinfo.hardwareArchitecture = coms[4].c_str();

        JsonDocument doc;
        doc["system_name"] = systemdata::sysinfo.systemName;
        doc["network_hostname"] = systemdata::sysinfo.networkHostname;
        doc["kernel_version"] = systemdata::sysinfo.kernelVersion;
        doc["kernel_release"] = systemdata::sysinfo.kernelRelease;
        doc["hardware_architecture"] = systemdata::sysinfo.hardwareArchitecture;

        serializeJson(doc, msg);
    }
    else if(route == "/monitoringData"){
        content_type = "application/json";
        //std::string response = sendCommandToSerial("get_monitoring_data")
    }
    else return "";
    response += content_type + '\n' + "Content-Length: " + msg.length() + '\n' + msg + "\n\n"; 
    return response;
}

std::string sendCommandToSerial(String command){
    String fullResponse = "";
    Serial.println('\n' + command); // Ask PC to send info
    std::string stdresp;
    // Read until END marker
    String line = "";
    while (true) {
        while (Serial.available()) {
            char c = Serial.read();
            line += c;
            if (line.endsWith("###END###")) {
                fullResponse = line;
                fullResponse.replace("###END###", ""); // Remove the end marker
                goto done;
            }
        }
    }
    done:;
    return fullResponse.c_str();
}