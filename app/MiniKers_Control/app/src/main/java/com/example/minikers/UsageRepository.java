package com.example.minikers;


import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.sqlite.db.SupportSQLiteQuery;
import java.util.List;

class UsageRepository {

    private AllDao mAllDao;
    private final String TAG = "UsageRepository";


    UsageRepository(Application application) {
        UsageRoomDatabase db = UsageRoomDatabase.getDatabase(application);
        mAllDao = db.allDao();
    }

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    LiveData<List<Usage>> getAllRecords() {
        return mAllDao.getAllRecords();
    }




    LiveData<List<String>> getUsageStartTimesForDay(String day) {
        return mAllDao.getUsageStartTimesFromDay(day);
    }

    LiveData<List<String>> getUsageEndTimesFromDay(String day){
        return mAllDao.getUsageEndTimesFromDay(day);
    }

    LiveData<List<String>> getUsageTypeList(String day) {
        return mAllDao.getUsageTypesFromDay(day);
    }

    LiveData<List<Usage>> getDaysRecords(String date) {
        return mAllDao.getRecordsFromDay(date);
    }


    List<String> getUsageStartTimesForDayAsNormalList(String date){
        return mAllDao.getUsageStartTimesFromDayAsOrdinaryList(date);
    }

    List<String> getUsageEndTimesForDayAsNormalList(String date){
        return mAllDao.getUsageEndTimesFromDayAsOrdinaryList(date);
    }

    List<String> getUsageTypesFromDayAsOrdinaryList(String day){
        return mAllDao.getUsageTypesFromDayAsOrdinaryList(day);
    }


    List<Usage> getRecordsFromDayAsOrdinaryList(String day){
        return mAllDao.getRecordsFromDayAsOrdinaryList(day);
    }

    //For writing journaled entries to the main database file
    void checkpoint(SupportSQLiteQuery supportSQLiteQuery){
        mAllDao.checkpoint(supportSQLiteQuery);
    }


    List<Usage> getDeviceUsagesAsOrdinaryList(String device){
        return mAllDao.getDeviceUsagesAsOrdinaryList(device);
    }

    LiveData<List<Usage>> getDeviceUsages(String device){
        return mAllDao.getDeviceUsages(device);
    }

    List<Usage> getDeviceUsagesForDayAsOrdinaryList(String device, String day){
        return mAllDao.getDeviceUsagesForDayAsOrdinaryList(device, day);
    }


    LiveData<List<Usage>> getDeviceUsagesForDay(String device, String day){
        return mAllDao.getDeviceUsagesForDay(device, day);
    }

    List<Usage> getUsagesOfMacAddress(String macAddress){
        return mAllDao.getUsagesOfMacAddress(macAddress);
    }

    List<Usage> getUsagesOfMacAddressForDay(String macAddress, String day){
        return mAllDao.getUsagesOfMacAddressForDay(macAddress, day);
    }

    List<String> getUsageStartTimesFromDayForMacAddress(String macAddress, String day){
        return mAllDao.getUsageStartTimesFromDayForMacAddress(macAddress, day);
    }

    List<String> getUsageEndTimesFromDayForMacAddress(String macAddress, String day){
        return mAllDao.getUsageEndTimesFromDayForMacAddress(macAddress, day);
    }

    List<String> getUsageTypesFromDayForMacAddress(String macAddress, String day){
        return mAllDao.getUsageTypesFromDayForMacAddress(macAddress, day);
    }

    List<Double> getVoltagesFromDayForMacAddress(String macAddress, String day){
        return mAllDao.getVoltagesFromDayForMacAddress(macAddress, day);
    }

    List<String> getAllMacAddresses(){
        return mAllDao.getAllMacAddresses();
    }


    //Room throws exception if insert() is called on a UI thread, so call on non-UI thread
    void insert(Usage usage) {
        UsageRoomDatabase.databaseWriteExecutor.execute( () -> {
            mAllDao.insert(usage);
        });
    }
}