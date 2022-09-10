#ifndef _TIME_SETTING_H_
#define _TIME_SETTING_H_

#include "RTClib.h"
#include "bluetooth.h"
#include "RTClib.h"

#define LED_TIMER 10000
#define BLE_TIMER 5000

#define DELAY_TIME 100

RTC_nRF52 rtc;

DateTime manual_start_time;
DateTime manual_end_time;

DateTime charging_start_time;
DateTime charging_end_time;

DateTime automation_start_time;
DateTime automation_end_time;

unsigned long led_previous_time = millis();
unsigned long ble_previous_time = millis();

unsigned long manual_previous_time = millis();
unsigned long ble_connected_previous_time = millis();

void RTC_SETTING ();
void APPCTS_TO_RTC(BLEClientCts bleCTime);
void ELAPSE_TIME_FROM_RST();
boolean SET_TIMER(unsigned long timer_interval, unsigned long previous_time);


#endif
