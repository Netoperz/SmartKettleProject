#include <ESP8266WiFi.h>
#include <ESP8266mDNS.h>
#include <Timer.h>

#define HEARTBEAT_INTERVAL 5000
#define PORT 9999



int ledPin = 5;
int readLDR = 0;
WiFiServer server(PORT);
WiFiClient client;
String response;

const char* ssid     = "ARDIC_GUEST";
const char* password = "W1cm2Ardc";

unsigned long readingFreq = 10000;

Timer myLDRTimer;
Timer heartbeatTimer;
int myHeartbeatTimerInt;
int myLDRTimerInt;
unsigned int discovery_ttl = 120;

String getMacAddress();
void connectToWifi();
void readLDRSensor();
void handleResponse();
void heartbeatGenerator();
String packetBuilder(String);


void update();

void setup() {
  Serial.begin(115200);

  pinMode(ledPin, OUTPUT);


  connectToWifi();


  server.begin();
  server.setNoDelay(true);

  myLDRTimerInt = myLDRTimer.every(readingFreq, readLDRSensor);
  myHeartbeatTimerInt = heartbeatTimer.every(HEARTBEAT_INTERVAL, heartbeatGenerator);
  while (true)
  {
    client = server.available();
    if (client.connected()) {
      Serial.println("Client Connected!");
      String macAdress = getMacAddress();
      client.write((uint8_t*) &macAdress[0], macAdress.length());
      break;

    }
  }


}

void loop() {



  if (client.connected()) {
    while (client.available()) {
      char c = client.read();
      response += c;
    }
    client.flush();
    if (!response.equals("")) {
      Serial.print("Data : ");
      Serial.print(response);
      Serial.println();
    }


    handleResponse();
    myLDRTimer.update();
    heartbeatTimer.update();



  } else {
    while (true)
    {
      client = server.available();
      if (client.connected()) {
        Serial.println("Client Connected!");
        String macAdress = getMacAddress();
        client.write((uint8_t*) &macAdress[0], macAdress.length());
        break;

      }
    }
  }

  delay(100);
}

void readLDRSensor() {
  String packet = "";
  int LDRPin = A0;

  readLDR = analogRead(LDRPin);



  String result = "";
  Serial.print("LDR=");
  Serial.print(readLDR);
  Serial.println();

  if (readLDR >= 750){
    Serial.print("Smart Kettle Boil");
    result = "BOILING";
  }
  else{
    Serial.print("Smart Kettle StandBy");
     result = "STANDBY";
  }

  packet = packetBuilder(result);
  client.write((uint8_t*)&packet[0], packet.length());
}



void connectToWifi() {
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(ssid);

  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    digitalWrite(ledPin, HIGH);
    delay(500);
    digitalWrite(ledPin, LOW);
    delay(500);
    Serial.print(".");
  }

  Serial.println("");
  Serial.println("WiFi connected");
  Serial.print("IP address: ");

  IPAddress localIP = WiFi.localIP();
  Serial.println(localIP);

  String name = "esp8266" + getMacAddress();
  Serial.println(name);
  if (!MDNS.begin(&name[0], localIP, discovery_ttl)) {
    Serial.println("Error setting up MDNS responder!");
    while (1) {
      delay(1000);
    }
  }
  Serial.println("mDNS responder started");

  MDNS.addService("esp8266", "tcp", PORT);
}

void handleResponse() {

  if (response.charAt(0) == 'R') {
    String config = response.substring(12, response.length());
    Serial.print("Config : ");
    Serial.print(config);
    Serial.println();
    readingFreq = (unsigned long) config.toFloat();
    Serial.println("Read Freq Changed!");
    update();
  } else {

    if (response.equals("LED_ON\n")) {
      Serial.println("Opening led...");
      digitalWrite(ledPin, HIGH);
      delay(100);
    }
    if (response.equals("LED_OFF\n")) {
      Serial.println("Closing led...");
      digitalWrite(ledPin, LOW);
      delay(100);

    }
  }

  response = "";
}

void update() {

  myLDRTimer.stop(myLDRTimerInt);

  myLDRTimerInt = myLDRTimer.every(readingFreq, readLDRSensor);
}



void heartbeatGenerator() {

  if (WiFi.status() != WL_CONNECTED) {
    connectToWifi();

  } else {

    String macAdress = getMacAddress();
    client.write((uint8_t*) &macAdress[0], macAdress.length());
    String heartbeat = "[___-^-____-^-____-^____-^-____] ";
    /////////////------------Heartbeat MSG-------//////////////
    client.write((uint8_t*)&heartbeat[0], heartbeat.length());

  }

}
String getMacAddress() {

  byte mac[6];

  WiFi.macAddress(mac);
  String cMac = "~";
  for (int i = 0; i < 6; ++i) {
    cMac += String(mac[i], HEX);
    if (i < 5)
      cMac += ":";

  }

  cMac.toUpperCase();
  cMac += "~";
  return cMac;
}

String packetBuilder(String ldr)
{ String packet = "";

  packet += "#";
  packet += "|Checksum|+";

  packet += "LDR:|" + ldr + "|" "|" "|";

  packet += "~";

  return packet;
}
