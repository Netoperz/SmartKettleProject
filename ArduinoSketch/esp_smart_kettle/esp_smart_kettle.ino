#include <ESP8266WiFi.h>
#include <ESP8266mDNS.h>
#include <Timer.h>
#include <HX711.h>

#define HEARTBEAT_INTERVAL 5000
#define LDR_INTERVAL 5000
#define WEIGHT_INTERVAL 5000
#define WATER_INTERVAL 5000
#define TEA_INTERVAL 5000

#define PORT 9999

HX711 cell(16, 4);

int rled = 13;                       //RGB led pin tanımlaması yapılıyor
int bled = 14;                //2
int gled = 12;                //4
int ledPin = 5;                       //pin tanımlanması yapılıyor
int readLDR = 0;
int State = 0 ;
int relayPin = 2;                     //röle pin tanımlanıyor    14
long val = 0;
long wal = 0;                       //değişkenler tanımlanıyor
long water = 0;
long waterGLO = 0;
double waterlitre = 0;
double tealitre = 0;
long tea = 0;
long teaGLO = 0;
int sayac = 0;

WiFiServer server(PORT);
WiFiClient client;

String response;                    //yollanacak veriler tanımlanıyor
String packet = "";
String result = "";
String weight = "";
String Water = "";
String Tea = "";
String RelayButton = "open";
String KettleRelay = "";

const char* ssid     = "YOUR SSID";          //bağlanılacak ağ adı ve şifresi girilmeli
const char* password = "YOUR PASSWORD";

unsigned long readingLDRFreq = 5000;                //refresh zamanı
unsigned long readingWeightFreq = 5000;
unsigned long readingWaterFreq = 5000;
unsigned long readingTeaFreq = 5000;

Timer myWeightTimer;                         //dönüglerin update edilmesi için gerekli timerler tanımlanıyor
Timer myLDRTimer;
Timer myWaterTimer;
Timer myTeaTimer;
Timer heartbeatTimer;
int myTeaTimerInt;
int myWaterTimerInt;
int myWeightTimerInt;
int myHeartbeatTimerInt;
int myLDRTimerInt;

unsigned int discovery_ttl = 1200;                  //wifi server bağlantı süresi

String getMacAddress();

void connectToWifi();                          //setup ve loop döngüleri haricinde sensörlerin farklı zamanlarda update edilmesi için gerekli döngüler oluşturuluyor
void readLDRSensor();
void handleResponse();
void heartbeatGenerator();
void scaleSensor();
void waterSensor();
void teaSensor();

String packetBuilder(String, String, String, String, String);

void update();

void setup() {

  Serial.begin(115200);             // seri haberleşme portu ayarlanıyor

  pinMode(ledPin, OUTPUT);
  pinMode(relayPin, OUTPUT);

  connectToWifi();              // wifi bağlanılıyor



  server.begin();             //server başlatılıyor
  server.setNoDelay(true);

  myLDRTimerInt = myLDRTimer.every(LDR_INTERVAL, readLDRSensor);             //timerler yeni döngülere atanıyor
  myHeartbeatTimerInt = heartbeatTimer.every(HEARTBEAT_INTERVAL, heartbeatGenerator);
  myWeightTimerInt = myWeightTimer.every(WEIGHT_INTERVAL, scaleSensor);
  myWaterTimerInt = myWaterTimer.every(WATER_INTERVAL, waterSensor);
  myTeaTimerInt = myTeaTimer.every(TEA_INTERVAL, teaSensor);



  pinMode(rled, OUTPUT);
  pinMode(bled, OUTPUT);
  pinMode(gled, OUTPUT);



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


    myLDRTimer.update();
    myWeightTimer.update();
    myWaterTimer.update();
    myTeaTimer.update();
    heartbeatTimer.update();
    handleResponse();                          //timerlar güncelleniyor

    val = cell.read();                        //loop döngüsünde ağırlık ölçümü yapılıyor
    wal = ((val - 4475040) / 52846.0f * 113);

    if (wal < 800)
    {
      State = 1;
    }

    Serial.print("weight");
    Serial.println(wal);

    switch (State) {

      case 1:
        {
          digitalWrite(rled, 0);
          digitalWrite(gled, 0);
          digitalWrite(bled, 255);
          tea = 0;
          teaGLO = 0;
          water = 0;
          waterGLO = 0;
          waterlitre = 0;
          tealitre = 0;
          sayac = 0;

          if (wal > 800)
          {
            State = 2;
          }

        }
        break;

      case 2:
        {
          tea = 0;
          teaGLO = 0;
          digitalWrite(rled, 255);
          digitalWrite(gled, 0);
          digitalWrite(bled, 255);
          water = wal - 920;
          Serial.print("Water = ");
          Serial.println(water);
          waterlitre = (waterGLO * 1.82) / 1570;

          if (sayac < 5)
          {
            waterGLO = water;
            sayac++;
          }
          else
          { Serial.print("gecis");
            Serial.println(waterGLO - water);
            if ((waterGLO - water) < -10)
            {
              State = 3;
              sayac = 0;
            }
            else
            {
              waterGLO = water;
              sayac++;
              if (waterlitre < 0.5)
              {
                State = 4;     // state 4.2 durumunu ifade eder
                sayac = 0;
              }
              else
              {
                State = 2;
              }
            }
          }
        }
        break;

      case 3:
        {
          digitalWrite(rled, 255);
          digitalWrite(gled, 255);
          digitalWrite(bled, 0);
          tea = wal - waterGLO - 1450;

          if (sayac < 15)
          {
            sayac++;
          }
          else
          {
            if ((tea - 20) < 0)
            {
              State = 2;
              sayac = 0;
            }
            else
            {
              teaGLO = tea;
              water = wal - tea - 1450;
              if (waterlitre < 0.5)
              {
                State = 5; //State 4.3 ifade ediyor
              }
              else
              {
                if (waterGLO != water)
                {
                  waterGLO = water;
                  State = 3;
                }
                State = 3;
              }
            }
          }

        }
        break;

      case 4:
        {
          digitalWrite(rled, 0);
          digitalWrite(gled, 255);
          digitalWrite(bled, 0);
          water = wal - 920;
          waterlitre = (waterGLO * 1.82) / 1570;
          tea = 0;
          teaGLO = 0;

          if (sayac < 5)
          {
            waterGLO = water;
            sayac++;
          }
          else
          {
            if ((waterGLO - water) < -10)
            {
              State = 5;
            }
            else
            {
              waterGLO = water;
              sayac++;
              if (waterlitre > 0.5)
              {
                State = 2;
                sayac = 0;
              }
              else if (waterlitre < 0.2)
              {
                State = 6;
                sayac = 0;
                // state 4.2 durumunu ifade eder
              }
              else if (waterlitre < 0.5)
              {
                State = 4;
              }
            }
          }
        }
        break;

      case 5:
        {
          digitalWrite(rled, 0);
          digitalWrite(gled, 255);
          digitalWrite(bled, 0);
          tea = wal - waterGLO - 1450;

          if ((tea - 20) < 0)
          {
            State = 4;
          }
          else
          {
            teaGLO = tea;
            water = wal - tea - 1450;
            if (waterlitre < 0.2)
            {
              State = 7;
            }
            else if (waterlitre < 0.5)
            {
              if ((waterGLO - 20) > water)
              {
                waterGLO = water;
                State = 5;
              }
              State = 5; //State 4.3 ifade ediyor
            }
          }
        }
        break;

      case 6:
        {
          digitalWrite(rled, 0);
          digitalWrite(gled, 255);
          digitalWrite(bled, 255);
          digitalWrite(relayPin, HIGH);
          water = wal - 920;
          waterlitre = (waterGLO * 1.82) / 1570;
          tea = 0;
          teaGLO = 0;

          if (sayac < 3)
          {
            waterGLO = water;
            sayac++;
          }
          else
          {
            if ((waterGLO - water) < -20)
            {
              State = 7;
            }
            else
            {
              waterGLO = water;
              sayac++;
              if (waterlitre > 0.5)
              {
                State = 2;
                sayac = 0;
              }
              else if (waterlitre < 0.2)
              {
                State = 6;
                sayac = 0;
                // state 4.2 durumunu ifade eder
              }
              else if (waterlitre < 0.5)
              {
                State = 4;
              }
            }
          }
        }
        break;

      case 7:
        {
          digitalWrite(rled, 0);
          digitalWrite(gled, 255);
          digitalWrite(bled, 255);
          tea = wal - waterGLO - 1450;

          if (sayac < 15)
          {
            sayac++;
          }
          else
          {
            if ((tea - 20) < 0)
            {
              State = 6;
              sayac = 0;
            }
            else
            {
              teaGLO = tea;
              water = wal - tea - 1450;
              if (waterlitre < 0.2)
              {
                State = 7 ; //State 4.3 ifade ediyor
              }
              else
              {
                if (waterGLO != water)
                {
                  waterGLO = water;
                  State = 7;
                }
                State = 7;
              }
            }
          }
        }
        break;
    }

    if (RelayButton == "open")
    {
      if (waterlitre < 0.2)
      {
        digitalWrite(relayPin , HIGH);   //röle kapanıyor
        KettleRelay = "close";
        Serial.println("su az oldugundan kettle calismiyor");
      }
      else
      {
        KettleRelay = "open";
        digitalWrite(relayPin , LOW);    //röle açılıyor
        Serial.println("kettle calisiyor");
      }
    }
    else
    {
      KettleRelay = "close";
      digitalWrite(relayPin, HIGH);
      Serial.println("kettle calismiyor");
    }
  }
  else {
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
void readLDRSensor() {      //Ldr sensöründen veriler okunuyor ve kettleın state durumu belirleniyor

  int LDRPin = A0;

  readLDR = analogRead(LDRPin);

  Serial.println(readLDR);

  if (KettleRelay == "open") {
    if (readLDR > 800)
    {
      result = "STANDBY";
    }
    else
    {
      result = "BOILING";
    }
  }
  else
  {
    result = "SMART KETTLE DISABLE";
  }


  Serial.print("LDR =");
  Serial.println(result);



}
void scaleSensor() {           // ağırlık ölçümü yapılıyor ve yapılan ölçüm grama dönüştürülüyor


  weight = String(wal, DEC);          // state ve ağırlık bilgileri paketleniyor

}
void teaSensor() {


  tealitre = (teaGLO * 1.82) / 1570;

  Tea = String(tealitre, DEC);
  Tea = Tea.substring(0, 4);



  Serial.print("Tea LITRE =");
  Serial.println(Tea);


}

void waterSensor() {

  waterlitre = (waterGLO * 1.82) / 1570;


  Water = String(waterlitre, DEC);
  Water = Water.substring(0, 4);


  Serial.print("Water LITRE =");
  Serial.println(Water);



  packet = packetBuilder(result, weight, Water, Tea, KettleRelay);
  client.write((uint8_t*)&packet[0], packet.length());  // paket yollanıyor


}
void connectToWifi() {               // wifi bağlantısı yapılıyor

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

  MDNS.addService("esp8266kettleservice", "tcp", PORT);
}

void handleResponse() {                // bağlantının olup olmadığını cihazın çalışıp çalışmadğını kontrol eden sinyal

  if (response.charAt(0) == 'L') {
    String config = response.substring(12, response.length());
    Serial.print("LDR Config : ");
    Serial.print(config);
    Serial.println();
    readingLDRFreq = (unsigned long) config.toFloat();
    Serial.println("LDR Read Freq Changed!");
    update();
  } else if (response.charAt(0) == 'W') {

    String config = response.substring(12, response.length());
    Serial.print(" Weight Config : ");
    Serial.print(config);
    Serial.println();
    readingWeightFreq = (unsigned long) config.toFloat();
    Serial.println("Weight Read Freq Changed!");
    update();

  } else if (response.charAt(0) == 'A') {

    String config = response.substring(12, response.length());
    Serial.print(" Water Config : ");
    Serial.print(config);
    Serial.println();
    readingWaterFreq = (unsigned long) config.toFloat();
    Serial.println("Water Read Freq Changed!");
    update();

  } else if (response.charAt(0) == 'T') {

    String config = response.substring(12, response.length());
    Serial.print(" Tea Config : ");
    Serial.print(config);
    Serial.println();
    readingTeaFreq = (unsigned long) config.toFloat();
    Serial.println("Tea Read Freq Changed!");
    update();

  } else if (response.equals("open\n")) {

    Serial.println("RelayButton = open");
    RelayButton = "open";
    update();

  }
  else if (response.equals("close\n")) {

    Serial.println("RelayButton = close");
    RelayButton = "close";
    update();

  }

  else {

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

void update() {          //timerlar güncelleniyor

  myLDRTimer.stop(myLDRTimerInt);
  myLDRTimerInt = myLDRTimer.every(readingLDRFreq, readLDRSensor);

  myWeightTimer.stop(myWeightTimerInt);
  myWeightTimerInt = myWeightTimer.every(readingWeightFreq, scaleSensor);

  myWaterTimer.stop(myWaterTimerInt);
  myWaterTimerInt = myWaterTimer.every(readingWaterFreq, waterSensor);

  myTeaTimer.stop(myTeaTimerInt);
  myTeaTimerInt = myTeaTimer.every(readingTeaFreq, teaSensor);



}

void heartbeatGenerator() {     // bağlantının olup olmadığını cihazın çalışıp çalışmadğını kontrol eden sinyal bilgisi

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
String getMacAddress() {      //mac adresi okunuyor

  byte mac[6];

  WiFi.macAddress(mac);
  String cMac = "{";
  for (int i = 0; i < 6; ++i) {
    cMac += String(mac[i], HEX);
    if (i < 5)
      cMac += ":";

  }

  cMac.toUpperCase();
  cMac += "}";
  return cMac;
}

String packetBuilder(String ldr, String weight, String Water, String Tea, String KettleRelay)              // paket oluşturuluyor
{ String packet = "";

  packet += "#";
  packet += "|Checksum|+";

  packet += "SENSORS:|" + ldr + "|" + weight + "|" + Water + "|" + Tea + "|" + KettleRelay + "|"  ;

  packet += "~";

  return packet;
}
