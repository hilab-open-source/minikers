#ifndef _ENERGY_CONTROL_H_
#define _ENERGY_CONTROL_H_

#include "Wire.h" // for I2C
#include "bluefruit.h" //sleep mode


const float GAIN = 5.0; // uA/mV 25
const float VBIAS = 1.65;
const float V_TO_MV = 1000.0;
const float A_TO_UA = 1000000.0;

const float SHUNT_R_MOTOR = 0.5; // 0.8; 0.7
const float SHUNT_R_BAT = 0.5;  //1.5;  0.7
const float ADC_TO_V = 3.6F / 1024.0F;
// 10-bit ADC with 3.6V input range

const float V_DIVIDER = 1.41;
const float VIN_DIVIDER = 1.41;

const float ROUT_BAT = 5000.0; //1000.0
const float ROUT_MOTOR = 5000.0; //1000.0

const float VIN_MIN_THRESHOLD = 0.7; // Efficiency higher than 85%
const float VIN_MAX_THRESHOLD = 1.7;  // Efficiency higher than 85%  According to VIN to select CAP, 1.6

const float MOTOR_RUNNING_THRESHOLD = 10;

#define SLEEPING_DELAY 5000

bool charging_to_bat = false;
bool manual_input = false;
bool motor_running = false;
bool start_measuring = false;

float VBAT = 0;
float VIN = 0;
float EBAT = 0;

float IMOTOR = 0;
float IBAT = 0;

float VIN_TO_SAVE = 0;

int manual_count = 0;

void PMONITOR(bool start_measuring, unsigned long t);
void CHARGING();
void DIGIPOT(int R_limit);
void SYSTEM_ON_SLEEP();
void gotoDeepSleep(unsigned long time_);
void manual_callback(void);
void manual_input_callback();


#endif
