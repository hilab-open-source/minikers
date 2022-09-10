
#include "initialization.h"
#include "motor_control.h"
#include "time_setting.h"



void manual_input_callback(){

  if (bluetooth_connected == false){

  digitalWrite(RECEN, LOW);

  manual_input = true;
  start_measuring = true; 

  // prepare data for writing to fram
  VIN_TO_SAVE = analogRead(PIN_UMOTOR) * VIN_DIVIDER * ADC_TO_V ;
  manual_start_time = rtc.now();

  //manual_previous_time = millis();
  
//  
//  while( start_measuring == true ){
//
////    DATA_LOGGING();
//
//    unsigned long time_interval = millis() - manual_previous_time; 
//
//    PMONITOR(start_measuring, time_interval);
//  
//    CHARGING();
//  
//    STORE_DATA();
//  
//    SYSTEM_ON_SLEEP();
//
//    manual_previous_time = millis();
//
//    delay(100);
//    
//  }
   
//  manual_count = manual_count + 1;   

  }
  
  else{
    digitalWrite(RECEN, HIGH);
  }

    
}

void PMONITOR(bool start_measuring,  unsigned long t) {

  if (start_measuring == true){
    
    digitalWrite(MONEN, LOW); 
   
    float VIN_ADC = analogRead(PIN_UMOTOR);
    VIN = VIN_ADC * VIN_DIVIDER * ADC_TO_V ;
  
    float VBAT_ADC = analogRead(PIN_UBAT);
    VBAT = VBAT_ADC * V_DIVIDER * ADC_TO_V;
  
    float IMOTOR_ADC = analogRead(PIN_IMOTOR);
    IMOTOR = (IMOTOR_ADC * ADC_TO_V - VBIAS) * A_TO_UA / ROUT_MOTOR / GAIN / SHUNT_R_MOTOR; //mA

    float PMotor = VIN * 0.001 * IMOTOR  ;
   
    
    if ( abs(IMOTOR) > MOTOR_RUNNING_THRESHOLD )
    {  
      motor_running = true;
    }
    else{
      motor_running = false;
    }

  
    float IBAT_ADC = analogRead(PIN_IBAT);
    IBAT = (IBAT_ADC * ADC_TO_V - VBIAS) * A_TO_UA / ROUT_BAT / GAIN / SHUNT_R_BAT; //mA

    float PBAT;


    if (charging_to_bat == true && IBAT > 0){
      PBAT = 0;
    }
    else{
        PBAT = VBAT * 0.001 * IBAT  ; // V * mA
    }
    
    EBAT = EBAT + PBAT * 0.001 * t; // J
  
    }

    else {
      digitalWrite(MONEN, HIGH); 
      
      EBAT = 0;
      IBAT = 0;
      IMOTOR = 0;

      motor_running = false;
           
    }

}


void CHARGING() {

  if (VIN > VIN_MAX_THRESHOLD ) {
    digitalWrite(BEN, LOW);
    charging_to_bat = true;

    // prepare data for writing to fram  
    charging_start_time = rtc.now();    
  }

  else if (charging_to_bat == true && VIN > VIN_MIN_THRESHOLD) {
    digitalWrite(BEN, LOW);
  }

  else { // VIN_MAX_THRESHOLD > VIN > VIN_MIN_THRESHOLD, charging_to_bat = false;

    digitalWrite(BEN, HIGH);

    if (manual_input == false){

       // prepare data for writing to fram  
      if (charging_to_bat == true){
        charging_end_time = rtc.now();
        writing_charging = true;
      }
      
      charging_to_bat = false;
      
      start_measuring = false;
      
    }
    
  }

    if ( manual_input == true ){
      
      digitalWrite(RECEN, LOW);
      
      if(motor_running == false){ // One manual input finished  
        manual_input = false; 
        pinMode(INTERRUPT_PIN, INPUT_PULLDOWN);

        // prepare data for writing to fram 
        
        manual_end_time = rtc.now();
 
        writing_manual = true;
       
      }
   }

    else{
            
      digitalWrite(RECEN, HIGH);
         
    }

    
}



void DIGIPOT(int R_limit) {

  digitalWrite(DIGIEN, LOW);

  Wire.beginTransmission(44); // transmit to device #44 (0x2c)
  // device address is specified in datasheet
  Wire.write(byte(0x00));            // sends instruction byte
  Wire.write(R_limit);             // sends potentiometer value byte
  Wire.endTransmission();     // stop transmitting

  delay(500);
}

void SYSTEM_ON_SLEEP()
{

  //LED that indicates the device is working
  if (SET_TIMER(LED_TIMER, led_previous_time) == true){
      digitalWrite(LED, HIGH);
      delay(50);
      digitalWrite(LED, LOW);
      led_previous_time = millis();
   }
   else{
      digitalWrite(LED, LOW);
   }

  // turn off all devices //low on high off

  digitalWrite(SLP, LOW);
//  digitalWrite(MONEN, HIGH); 
  digitalWrite(RAMEN, HIGH);  //---------------Attention: FRAM should be controlled by DIGIEN
//  digitalWrite(BEN, HIGH);
//  digitalWrite(RECEN, HIGH); 
  digitalWrite(DIGIEN, HIGH);
    
  //sd_app_evt_wait(); //System on sleep mode
  
}

void gotoDeepSleep(unsigned long time_)
{
  // shutdown when time reaches SLEEPING_DELAY ms
  if ((time_ > SLEEPING_DELAY))
  {
    Serial.println("Yes");
    // to reduce power consumption when sleeping, turn off all power hungry devices
    digitalWrite(LED, LOW);
    // turn off all devices in automation mode
    digitalWrite(SLP, LOW);
    digitalWrite(MONEN, HIGH); //low on high off
    digitalWrite(RAMEN, HIGH);
    // turn off all devices in manual mode
    digitalWrite(BEN, HIGH);
    digitalWrite(RECEN, HIGH);

    // setup wake-up pins.
    pinMode(WAKE_LOW_PIN,  INPUT_PULLUP_SENSE);    // WAKE_LOW_PIN is pulled up and wakes up the feather when externally connected to ground.
    //pinMode(WAKE_HIGH_PIN, INPUT_PULLDOWN_SENSE);  // WAKE_HIGH_PIN is pulled down and wakes up the feather when externally connected to 3.3v.

    //nRF5x_lowPower.enableWakeupByInterrupt(7, CHANGE);

    // power down nrf52.
    sd_power_system_off();  // this function puts the whole nRF52 to deep sleep (no Bluetooth). 600uA
    //If no sense pins are setup (or other hardware interrupts), the nrf52 will not wake up.
    //sd_power_mode_set(NRF_POWER_MODE_LOWPWR); // 600uA
    //sd_app_evt_wait(); //System on sleep mode
    //NVIC_ClearPendingIRQ(SWI2_IRQn);
    //__WFE();

    //suspendLoop();

  }
}
