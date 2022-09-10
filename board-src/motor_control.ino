#include "initialization.h"

 bool STALL_DETECTION(){
  bool stall_detected;
  if (abs(IMOTOR) > STALL_CURRENT){
    stall_detected = true;
  }
  else{
    stall_detected = false;
  }

  return stall_detected;
}


void MOTOR_RUN(int dir, float vel) {

  //Uncomment hardware pwm in setup()
  digitalWrite(BEN, HIGH);
  digitalWrite(RECEN, HIGH); //Low on, high off

  digitalWrite(SLP, HIGH);

  pinMode(PWM1, OUTPUT);
  pinMode(PWM2, OUTPUT);

  if (dir == 0) {
    digitalWrite(PWM1, LOW);
    //HwPWM0.writePin(PWM1, maxValue * vel, false);
    digitalWrite(PWM2, HIGH);
  }
  else {
    digitalWrite(PWM1, HIGH);
    //HwPWM0.writePin(PWM2, maxValue * vel, false);
    digitalWrite(PWM2, LOW);
  }
}
void MOTOR_BRAKE() {

  digitalWrite(SLP, LOW);
  digitalWrite(PWM1, LOW);
  digitalWrite(PWM2, LOW);

  //digitalWrite(LED, LOW);
  // turn off all devices in automation mode
  digitalWrite(MONEN, HIGH); //low on high off
  digitalWrite(RAMEN, HIGH);
  // turn off all devices in manual mode
  digitalWrite(BEN, HIGH);
  digitalWrite(RECEN, HIGH);

  pinMode(PWM1, INPUT);
  pinMode(PWM2, INPUT);
}
