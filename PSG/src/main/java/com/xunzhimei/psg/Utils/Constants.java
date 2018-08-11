package com.xunzhimei.psg.Utils;

/**
 * Author: Created by 周寒 on 2018/8/5
 * Email : vihanmy@google.com
 * Notes :App中的一些常量
 */
public class Constants
{
    public static String getSPFILENAME()
    {
        return SPFILENAME;
    }

    //APP配置SPname
    private static String SPFILENAME = "spfilename";

    //Sp中用于保存匹配的蓝牙设备信息
    private static String MATCHDEVICE_NAME = "matchdevice_name";
    private static String MATCHDEVICE_MAC = "matchdevice_mac";

    //蓝牙收发UUID
    //收
    private static String RECEI_BLE_DATA_SERVICE_UUID = "0000ffe0-0000-1000-8000-00805f9b34fb";
    private static String RECEI_BLE_DATA_CHAREC_UUID = "0000ffe1-0000-1000-8000-00805f9b34fb";

    //发
    private static String SEND_BLE_DATA_SERVICE_UUID = "00001802-0000-1000-8000-00805f9b34fb";
    private static String SEND_BLE_DATA_CHAREC_UUID = "00002a06-0000-1000-8000-00805f9b34fb";


    //---------------------------------getter方法------------------------------

    public static String getReceiBleDataCharecUuid()
    {
        return RECEI_BLE_DATA_CHAREC_UUID;
    }
    public static String getSendBleDataServiceUuid()
    {
        return SEND_BLE_DATA_SERVICE_UUID;
    }
    public static String getSendBleDataCharecUuid()
    {
        return SEND_BLE_DATA_CHAREC_UUID;
    }
    public static String getMATCHDEVICE_NAME()
    {
        return MATCHDEVICE_NAME;
    }
    public static String getMATCHDEVICE_MAC()
    {
        return MATCHDEVICE_MAC;
    }


}
