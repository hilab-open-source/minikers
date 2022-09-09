#ifndef _FRAM_PROCESSING_H_
#define _FRAM_PROCESSING_H_ 

#include "Adafruit_FRAM_I2C.h" //for FRAM
#include "time_setting.h"

#define sizeInBytes 32768
#define BYTE_PER_PACKET 17

Adafruit_FRAM_I2C  fram = Adafruit_FRAM_I2C();

bool writing_manual = false;
bool writing_charging = false;
bool writing_automation = false;

void FRAM_INIT();
void FRAM_CLEAR();
void FRAM_DUMP();
void UPDATE_PACKET_NUM();
void FRAM_TO_BLUETOOTH(); 
void STORE_DATA();
void FRAM_WRITE(bool writing, char mode_, float delta_vin, DateTime start_time, DateTime end_time);


#endif
