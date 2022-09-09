#ifndef _BLUETOOTH_H_
#define _BLUETOOTH_H_ 

#include "Adafruit_LittleFS.h" //BLuetooth
#include "InternalFileSystem.h" //BLuetooth
#include "bluefruit.h" 

// BLE Service
BLEDis  bledis;  // device information
BLEUart bleuart; // uart over ble
BLEBas  blebas;  // battery

BLEClientCts  bleCTime; // BLE Client Current Time Service

boolean bluetooth_connected = false;

void connect_callback(uint16_t conn_handle);
void disconnect_callback(uint16_t conn_handle, uint8_t reason);
void startAdv(void);
void BLE_INITIALIZATION();
void BLE_MODE(); 
void BLE_PERIODICAL_ADVERTISE();


#endif
