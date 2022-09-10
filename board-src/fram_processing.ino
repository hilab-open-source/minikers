#include "initialization.h"
#include "bluetooth.h"


void FRAM_INIT() {
  if (fram.begin()) {  // you can stick the new i2c addr in here, e.g. begin(0x51);
    Serial.println("Found I2C FRAM");
  } else {
    Serial.println("I2C FRAM not identified");
    while (1);
  }

}

void FRAM_CLEAR() {

  for (uint32_t addr = 0; addr < sizeInBytes; addr ++)
  {
    fram.write(addr, 0x00);
  }

  Serial.println("Finish clearing");

}

void FRAM_WRITE_END(){
  
  writing_manual = false;
  writing_charging = false;
  
}

void FRAM_WRITE(bool writing, char mode_, float data_1, float data_2, DateTime start_time, DateTime end_time) {

  if (writing == true){

    digitalWrite(RAMEN, LOW); 
    digitalWrite(DIGIEN, LOW);

    fram.begin();

    uint8_t buffer[4];
    float count_modes;
    
    fram.read(0x0, buffer, sizeof(float)); 
    memcpy(&count_modes, buffer, sizeof(float));
    

    //count the address to start
    uint16_t addr = count_modes * BYTE_PER_PACKET + 4;
  
    uint16_t num_mode = fram.writeObject(addr, mode_); // 1 byte
    addr = addr + num_mode;
  
    uint16_t num_data_1 = fram.writeObject(addr, data_1); // 4 bytes
    addr = addr + num_data_1;

    uint16_t num_data_2 = fram.writeObject(addr, data_2); // 4 bytes
    addr = addr + num_data_2;
  
    uint16_t num_start_time_month = fram.writeObject(addr, start_time.month()); // 1 byte
    addr = addr + num_start_time_month;
  
    uint16_t num_start_time_day = fram.writeObject(addr, start_time.day()); // 1 byte
    addr = addr + num_start_time_day;
  
    uint16_t num_start_time_hour = fram.writeObject(addr, start_time.hour());  // 1 byte
    addr = addr + num_start_time_hour;
  
    uint16_t num_start_time_minute = fram.writeObject(addr, start_time.minute()); // 1 byte
    addr = addr + num_start_time_minute;
  
    uint16_t num_start_time_second = fram.writeObject(addr, start_time.second()); // 1 byte
    addr = addr + num_start_time_second;
  
    uint16_t num_end_time_hour = fram.writeObject(addr, end_time.hour()); // 1 byte
    addr = addr + num_end_time_hour;
  
    uint16_t num_end_time_minute = fram.writeObject(addr, end_time.minute()); // 1 byte
    addr = addr + num_end_time_minute;
  
    uint16_t num_end_time_second = fram.writeObject(addr, end_time.second()); // 1 byte
    addr = addr + num_end_time_second;
  
//    UPDATE_PACKET_NUM();

    fram.writeObject(0x0, count_modes + 1);
    
    fram.end();

//    Serial.println("_____");

  }


  else{

     digitalWrite(RAMEN, HIGH); 
     digitalWrite(DIGIEN, HIGH);

  }
  
}


void STORE_DATA(){
   
   FRAM_WRITE(writing_manual, 'M', VBAT, VIN_TO_SAVE, manual_start_time, manual_end_time);  //manual data
   FRAM_WRITE(writing_charging, 'C', VBAT, EBAT, charging_start_time, charging_end_time);  //charging data
   FRAM_WRITE_END(); 
}



void FRAM_DUMP() {
  // dump the entire 32K of memory!
  uint8_t value;
  for (uint16_t a = 0; a < 32768; a++) {
    value = fram.read(a);
    if ((a % 32) == 0) {
      Serial.print("\n 0x"); Serial.print(a, HEX); Serial.print(": ");
    }
    Serial.print("0x");
    if (value < 0x1)
      Serial.print('0');
    Serial.print(value, HEX); Serial.print(" ");
  }
}

void UPDATE_PACKET_NUM() { //usage time is stored first byte, 0x00
  uint8_t count_packets = fram.read(0x0);
  fram.write(0x0, count_packets + 1);
}


void FRAM_TO_BLUETOOTH() { //send fram via bluetooth one byte by one byte
  uint8_t num_modes = fram.read(0x0);
  for (uint16_t addr = 1; addr < BYTE_PER_PACKET * num_modes + 1; addr ++) {
    bleuart.write(fram.read(addr)); //send fram via bluetooth one byte by one byte
    delay(20);
  }
}
