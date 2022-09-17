package com.example.minikers_receiver;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.sqlite.db.SupportSQLiteQuery;

import java.time.LocalTime;
import java.util.List;

//Holds UI data
public class UsageViewModel extends AndroidViewModel {

    private UsageRepository mRepository;

    private final LiveData<List<Usage>> mAllRecords;

    public UsageViewModel(Application application) {
        super(application);
        mRepository = new UsageRepository(application);
        mAllRecords = mRepository.getAllRecords();
    }

    LiveData<List<Usage>> getAllRecords() { return mAllRecords; }

    LiveData<List<Usage>> getRecordsForDay(String date) {
        return mRepository.getDaysRecords(date);
    }

    LiveData<List<String>> getUsageTypeListForDay(String date){
        return mRepository.getUsageTypeList(date);
    }

    LiveData<List<String>> getUsageStartTimesForDay(String date) {
        return mRepository.getUsageStartTimesForDay(date);
    }

    LiveData<List<String>> getUsageEndTimesFromDay(String day){
        return mRepository.getUsageEndTimesFromDay(day);
    }

    LiveData<List<String>> getUsageEndTimesForDay(String date) {
        return mRepository.getUsageEndTimesFromDay(date);
    }

    List<String> getUsageStartTimesForDayAsNormalList(String date) {
        return mRepository.getUsageStartTimesForDayAsNormalList(date);
    }

    List<String> getUsageEndTimesForDayAsNormalList(String date) {
        return mRepository.getUsageEndTimesForDayAsNormalList(date);
    }

    List<String> getUsageTypesFromDayAsOrdinaryList(String day){
        return mRepository.getUsageTypesFromDayAsOrdinaryList(day);
    }


    List<Usage> getRecordsFromDayAsOrdinaryList(String day){
        return mRepository.getRecordsFromDayAsOrdinaryList(day);
    }

    void checkpoint(SupportSQLiteQuery supportSQLiteQuery){
        mRepository.checkpoint(supportSQLiteQuery);
    }

    List<Usage> getDeviceUsagesAsOrdinaryList(String device){
        return mRepository.getDeviceUsagesAsOrdinaryList(device);
    }


    LiveData<List<Usage>> getDeviceUsages(String device){
        return mRepository.getDeviceUsages(device);
    }

    List<Usage> getDeviceUsagesForDayAsOrdinaryList(String device, String day){
        return mRepository.getDeviceUsagesForDayAsOrdinaryList(device, day);
    }


    LiveData<List<Usage>> getDeviceUsagesForDay(String device, String day){
        return mRepository.getDeviceUsagesForDay(device, day);
    }

    List<Usage> getDeviceUsagesUsingMacAddress(String macAddress){
        return mRepository.getUsagesOfMacAddress(macAddress);
    }

    List<Usage> getUsagesOfMacAddressForDay(String macAddress, String day){
        return mRepository.getUsagesOfMacAddressForDay(macAddress, day);
    }

    List<String> getUsageStartTimesFromDayForMacAddress(String macAddress, String day){
        return mRepository.getUsageStartTimesFromDayForMacAddress(macAddress, day);
    }

    List<String> getUsageEndTimesFromDayForMacAddress(String macAddress, String day){
        return mRepository.getUsageEndTimesFromDayForMacAddress(macAddress, day);
    }

    List<String> getUsageTypesFromDayForMacAddress(String macAddress, String day){
        return mRepository.getUsageTypesFromDayForMacAddress(macAddress, day);
    }

    List<Double> getVoltagesFromDayForMacAddress(String macAddress, String day){
        return mRepository.getVoltagesFromDayForMacAddress(macAddress, day);
    }

    List<String> getAllMacAddresses(){
        return mRepository.getAllMacAddresses();
    }

    //encapsulates the implementation of insert() from the UI.
    public void insert(Usage usage) {
        mRepository.insert(usage);
    }


}