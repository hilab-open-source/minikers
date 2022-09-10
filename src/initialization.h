#ifndef _INITISLIZAION_H_
#define _INITISLIZAION_H_

#include "energy_control.h"

#define PIN_UMOTOR A0
#define PIN_UBAT A1
#define PIN_IMOTOR A2
#define PIN_IBAT A3

#define LED 17
#define RECEN 7
#define BEN 14
#define MONEN 13
#define RAMEN 19
#define DIGIEN 12
#define BUT 23

#define SLP 11
#define PWM1 15
#define PWM2 16

#define WAKE_LOW_PIN  PIN_A4
//#define WAKE_HIGH_PIN PIN_A5

#define INTERRUPT_PIN  PIN_A5

#define SDA 25
#define SCL 26

void DATA_LOGGING();
void PIN_SETUP();
void manual_input_callback();


#endif
