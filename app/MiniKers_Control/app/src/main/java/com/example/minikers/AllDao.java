package com.example.minikers;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

import java.util.List;


//Defines functions, which are wrappers for SQL queries, to retrieve records from database
@Dao
interface AllDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Usage usage);

    @Query("DELETE FROM usage_table")
    void deleteAll();

    @Query("SELECT * FROM usage_table")
    LiveData<List<Usage>> getAllRecords();  //LiveData makes sure the list returned is the most recent one?

    @Query("SELECT * FROM usage_table WHERE day = :day ORDER BY startTime ASC")
    LiveData<List<Usage>> getRecordsFromDay(String day);

    @Query("SELECT mode FROM usage_table WHERE day = :day ORDER BY startTime ASC")
    LiveData<List<String>> getUsageTypesFromDay(String day);

    @Query("SELECT startTime FROM usage_table WHERE day = :day ORDER BY startTime ASC")
    LiveData<List<String>> getUsageStartTimesFromDay(String day);

    @Query("SELECT endTime FROM usage_table WHERE day = :day ORDER BY startTime ASC")
    LiveData<List<String>> getUsageEndTimesFromDay(String day);

    //Below, functions that return non-LiveData lists are for use in SingleDayData, where the data only needs to be loaded once at the beginning
    @Query("SELECT startTime FROM usage_table WHERE day = :day ORDER BY startTime ASC")
    List<String> getUsageStartTimesFromDayAsOrdinaryList(String day);

    @Query("SELECT endTime FROM usage_table WHERE day = :day ORDER BY startTime ASC")
    List<String> getUsageEndTimesFromDayAsOrdinaryList(String day);

    @Query("SELECT mode FROM usage_table WHERE day = :day ORDER BY startTime ASC")
    List<String> getUsageTypesFromDayAsOrdinaryList(String day);

    @Query("SELECT * FROM usage_table WHERE day = :day ORDER BY startTime ASC")
    List<Usage> getRecordsFromDayAsOrdinaryList(String day);

    @Query("Select * from usage_table WHERE deviceName = :device ORDER BY startTime ASC")
    List<Usage> getDeviceUsagesAsOrdinaryList(String device);

    @Query("Select * from usage_table WHERE deviceName = :device ORDER BY startTime ASC")
    LiveData<List<Usage>> getDeviceUsages(String device);

    @Query("Select * from usage_table WHERE deviceName = :device AND day = :day ORDER BY startTime ASC")
    List<Usage> getDeviceUsagesForDayAsOrdinaryList(String device, String day);

    @Query("Select * from usage_table WHERE deviceName = :device AND day = :day ORDER BY startTime ASC")
    LiveData<List<Usage>> getDeviceUsagesForDay(String device, String day);

    @Query("Select * from usage_table WHERE macAddress = :macAddress ORDER BY startTime ASC")
    List<Usage> getUsagesOfMacAddress(String macAddress); //The same as getDeviceUsages, but uses macAddress instead of device name to identify.

    @Query("Select * from usage_table WHERE macAddress = :macAddress AND day = :day ORDER BY startTime ASC")
    List<Usage> getUsagesOfMacAddressForDay(String macAddress, String day);

    @Query("SELECT startTime FROM usage_table WHERE macAddress = :macAddress AND day = :day ORDER BY startTime ASC")
    List<String> getUsageStartTimesFromDayForMacAddress(String macAddress, String day);

    @Query("SELECT endTime FROM usage_table WHERE macAddress = :macAddress AND day = :day ORDER BY startTime ASC")
    List<String> getUsageEndTimesFromDayForMacAddress(String macAddress, String day);

    @Query("SELECT mode FROM usage_table WHERE macAddress = :macAddress AND day = :day ORDER BY startTime ASC")
    List<String> getUsageTypesFromDayForMacAddress(String macAddress, String day);

    @Query("SELECT voltage FROM usage_table WHERE macAddress = :macAddress AND day = :day ORDER BY startTime ASC")
    List<Double> getVoltagesFromDayForMacAddress(String macAddress, String day);

    @Query("SELECT DISTINCT macAddress from usage_table")
    List<String> getAllMacAddresses();

    @RawQuery
    int checkpoint(SupportSQLiteQuery supportSQLiteQuery); //Used to write changes to main database file

}
