#include <Arduino.h>
#include <string>
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
        String fullResponse = "";
        Serial.println("\nget_systemdata"); // Ask PC to send info

        // Read until END marker
        String line = "";
        while (true) {
            while (Serial.available()) {
                char c = Serial.read();
                line += c;

                if (line.endsWith("###END###")) {
                    fullResponse = line;
                    fullResponse.replace("###END###", ""); // Remove the end marker
                    msg = fullResponse;
                    goto done;
                }
            }
        }
    done:;
    }
    else return "";
    response += content_type + '\n' + "Content-Length: " + msg.length() + '\n' + msg + "\n\n"; 
    return response;
}