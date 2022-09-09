

void PIN_SETUP(){
  pinMode(LED, OUTPUT);
  pinMode(RECEN, OUTPUT);
  pinMode(BEN, OUTPUT);
  pinMode(MONEN, OUTPUT);
  pinMode(RAMEN, OUTPUT);
  pinMode(DIGIEN, OUTPUT);

  //AUTOMATION
  pinMode(SLP, OUTPUT);
  pinMode(PWM1, INPUT);
  pinMode(PWM2, INPUT);

  pinMode(INTERRUPT_PIN, INPUT_PULLDOWN);
  attachInterrupt(INTERRUPT_PIN, manual_input_callback, RISING);
  
  //PWM
  //HwPWM0.addPin( PWM1 );
  //HwPWM0.addPin( PWM2 )
  //HwPWM0.begin();
  //HwPWM0.setResolution(15);
  //HwPWM0.setClockDiv(PWM_PRESCALER_PRESCALER_DIV_1); // freq = 16Mhz

}

void DATA_LOGGING(){

  Serial.print("manual_input = ");
  Serial.print(manual_input);
  Serial.print(",");
  Serial.print("motor running = ");
  Serial.print(motor_running);
  Serial.print(",");
//  Serial.print("manual count = ");
//  Serial.print(manual_count);
//  Serial.print(",");
  Serial.print("start Measuring = ");
  Serial.print(start_measuring);
  Serial.print(",");
  Serial.print("battery current = ");
  Serial.print(IBAT);
  Serial.print(",");
  Serial.print("motor current = ");
  Serial.print(IMOTOR);
  Serial.print(",");
  Serial.print("supercap volatge = ");
  Serial.print(VIN);
//  Serial.print(",");
//  Serial.print("battery volatge = ");
//  Serial.print(VBAT);
//  
  Serial.print(",");
  Serial.print("charging to battery = ");
  Serial.print(charging_to_bat);
  Serial.print(",");
  Serial.print(" EBAT = ");
  Serial.print(EBAT);
//  
//  Serial.print(",");
//  Serial.print("writing_charging = ");
//  Serial.print(writing_charging);
//
  Serial.print(",");
  Serial.print("writing_manual = ");
  Serial.print(writing_manual);
//
//  Serial.print(",");
//  Serial.print("writing_automation = ");
//  Serial.println(writing_automation);

    Serial.print(",");
    Serial.print("manual_count = ");
    Serial.println(manual_count);

//  Serial.print(",");
//  Serial.print(" stall_detected = ");
//  Serial.println(stall_detected);

  
 
}
