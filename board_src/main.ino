#include "Arduino.h"
#include "Adafruit_TinyUSB.h" // for Serial

#include "initialization.h"
#include "fram_processing.h"
#include "motor_control.h"
#include "bluetooth.h"
#include "energy_control.h"
#include "time_setting.h"

void setup() {

//  Serial.begin(115200);
  
  PIN_SETUP();

  //BLE
  BLE_INITIALIZATION();

  // RTC
  RTC_SETTING();

  //SLEEP MODE
  //Bluefruit.begin();          // Sleep functions need the softdevice to be active.

  //DIGIPOT 
  //Wire.begin(); // join i2c bus

//  //FRAM
//  digitalWrite(DIGIEN, LOW);
//  digitalWrite(RAMEN, LOW);
//  FRAM_INIT();
//  FRAM_CLEAR();
//  FRAM_DUMP();

}


void loop() {

//  DATA_LOGGING();
  
  delay(100);

  unsigned long time_interval = millis() - manual_previous_time; 
  
  if (bluetooth_connected == false){

   PMONITOR(start_measuring, time_interval);

   CHARGING();

   STORE_DATA();

   SYSTEM_ON_SLEEP();
   
   BLE_PERIODICAL_ADVERTISE();
   
  }   

  else{
    
    digitalWrite(LED, HIGH);
    
    BLE_MODE(); 

    PMONITOR(start_measuring, time_interval);
    
  }

   manual_previous_time = millis();

}
