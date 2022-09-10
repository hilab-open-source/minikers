


void RTC_SETTING (){
  
  if (! rtc.begin()) {
    Serial.println("Couldn't find RTC");
    Serial.flush();
    abort();
  }

  //
  rtc.adjust(DateTime(F(__DATE__), F(__TIME__)));

  
}


void APPCTS_TO_RTC(BLEClientCts bleCTime)
{
  
   bleCTime.getCurrentTime(); 
   // This line sets the RTC with the date/time from the Current Time Service:
   rtc.adjust(DateTime(bleCTime.Time.year, bleCTime.Time.month, bleCTime.Time.day, bleCTime.Time.hour, bleCTime.Time.minute, bleCTime.Time.second));

    
    
}

void ELAPSE_TIME_FROM_RST()
{
  uint32_t elapsedSeconds = nrf_rtc_counter_get(NRF_RTCZ) >> 0x3;  
}


boolean SET_TIMER(unsigned long timer_interval, unsigned long previous_time)
{
  if (millis() - previous_time > timer_interval){
    return true;
  }
  else{
    return false;
  }
  
}
