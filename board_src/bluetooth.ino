#include "initialization.h"
#include "motor_control.h"
#include "energy_control.h"
#include "fram_processing.h"
#include "time_setting.h"


// callback invoked when central connects
void connect_callback(uint16_t conn_handle)
{
  // Get the reference to current connection
  BLEConnection* connection = Bluefruit.Connection(conn_handle);

  char central_name[32] = { 0 };
  connection->getPeerName(central_name, sizeof(central_name));

  bluetooth_connected = true;
  
}

void disconnect_callback(uint16_t conn_handle, uint8_t reason)
{
  (void) conn_handle;
  (void) reason;

  bluetooth_connected = false;
}

void startAdv(void)
{
  // Advertising packet
  Bluefruit.Advertising.addFlags(BLE_GAP_ADV_FLAGS_LE_ONLY_GENERAL_DISC_MODE);
  Bluefruit.Advertising.addTxPower();

  // Include bleuart 128-bit uuid
  Bluefruit.Advertising.addService(bleuart);

  // Secondary Scan Response packet (optional)
  // Since there is no room for 'Name' in Advertising packet
  Bluefruit.ScanResponse.addName();

  Bluefruit.Advertising.restartOnDisconnect(true);
  Bluefruit.Advertising.setInterval(32, 244);    // in unit of 0.625 ms
  Bluefruit.Advertising.setFastTimeout(30);      // number of seconds in fast mode
  //Bluefruit.Advertising.start(0);                // 0 = Don't stop advertising after n seconds
}

void BLE_INITIALIZATION() {
  Bluefruit.configPrphBandwidth(BANDWIDTH_MAX);
  Bluefruit.begin();
  // off Blue LED for lowest power consumption
  //Bluefruit.autoConnLed(false); //  Blue LED  PIN 19
  Bluefruit.setTxPower(0);    // Check bluefruit.h for supported values
  //Bluefruit.setName(getMcuUniqueID()); // useful testing with multiple central connections
  Bluefruit.setName("Drawer");
  Bluefruit.Periph.setConnectCallback(connect_callback);
  Bluefruit.Periph.setDisconnectCallback(disconnect_callback);

  // Configure and Start Device Information Service
  bledis.setManufacturer("Adafruit Industries");
  bledis.setModel("Bluefruit Feather52");
  bledis.begin();

  // Configure and Start BLE Uart Service
  bleuart.begin();

  // Configure CTS client
  //bleCTime.begin();

  // Start BLE Battery Service
  //blebas.begin();
  //blebas.write(100);

  // Set up and start advertising
  startAdv();
}

void BLE_MODE() {

  stall_detected = STALL_DETECTION();

  if (STALL_DETECTION() == true ){
    MOTOR_BRAKE();

    writing_automation = true;
    automation_end_time = rtc.now();
    FRAM_WRITE(writing_automation, 'A', VBAT, EBAT, automation_start_time, automation_end_time);
    writing_automation = false;
      
    start_measuring = false;
  }

  while (bleuart.available() )
  {
    uint8_t ch;
    ch = (uint8_t) bleuart.read();

//    Serial.write(ch);

    if (ch == 'o') { // run +

      start_measuring = true;

      automation_start_time = rtc.now();

      MOTOR_RUN(0, vel);
         
    }

    if (ch == 'r') { // run -

      start_measuring = true;

      automation_start_time = rtc.now();

      MOTOR_RUN(1, vel);
      
    }

    if (ch == 'f') { // stop

      MOTOR_BRAKE();

      writing_automation = true;
      automation_end_time = rtc.now();
      FRAM_WRITE(writing_automation, 'A', VBAT, EBAT, automation_start_time, automation_end_time);
      writing_automation = false;
      
      start_measuring = false;

      //FRAM_WRITE('A', EBAT, app_start_time, millis());
      //UPDATE_USAGE_TIMES();
    }

//    if (ch == 'r') { // read fram
//      //FRAM_TO_BLUETOOTH();
//    }

//    if (ch == 'c') { // erase memory
//      //FRAM_CLEAR();
//    }

  }
  

}

void BLE_PERIODICAL_ADVERTISE()
{
  if (SET_TIMER(BLE_TIMER, ble_previous_time) == true){
    Bluefruit.Advertising.start(0);
    ble_previous_time = millis();
  }
  else{
    Bluefruit.Advertising.stop();
    }
}
