#ifndef _MOTOR_CONTORL_H_
#define _MOTOR_CONTORL_H_

const int maxValue = bit(15) - 1;

int dir = 0;
int vel = 1.0;

bool stall_detected;
float STALL_CURRENT = 130;

void MOTOR_RUN(int dir, float vel);
void MOTOR_BRAKE();
bool STALL_DETECTION();

#endif
