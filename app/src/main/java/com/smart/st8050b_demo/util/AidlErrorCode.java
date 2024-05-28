package com.smart.st8050b_demo.util;

import com.smart.st8050b_demo.R;
import com.smart.st8050b_demo.St8050bApplication;

/**
 * Created by Administrator on 2018/3/1.
 */

public class AidlErrorCode {

    /**
     * EMV模块错误码
     *
     * @author Administrator
     */
    public static final class EMV {
        /**
         * 和EMV内核通讯出错
         */
        public static final int EMV_KERNEL_COMMUNICATE_ERROR = -0x04;
        /**
         * EMV内核执行出错
         */
        public static final int EMV_KERNEL_EXCUTE_ERROR = -0x05;
        /**
         * EMV内核参数错误
         */
        public static final int EMV_ILLIGAL_PARAMETER_ERROR = -0x07;
        /**
         * 读取EMV内核参数错
         */
        public static final int EMV_READ_KERNEL_DATA_FAIL = -0x08;
        /**
         * 未知的EMV内核事件
         */
        public static final int EMV_KERNEL_UNKNOWN_EVENTID_ERROR = -0x06;
        /**
         * 磁条卡设备异常
         */
        public static final int ERROR_CHECK_MAGCARD_ERROR = -0x03;
        /**
         * IC卡设备异常
         */
        public static final int ERROR_CHECK_ICCARD_ERROR = -0x01;
        /**
         * RF卡设备异常
         */
        public static final int ERROR_CHECK_RFCARD_ERROR = -0x02;
        /**
         * 如果是带磁条的IC卡，拒绝刷磁条
         */
        public static final int ERROR_CHECK_REFUSE_MAGCARD_FOR_ICCARD = -0x10;
        /**
         * 工作秘钥TDK不存在
         */
        public static final int ERROR_CHECK_TDK_NOT_EXIST = -0x11;
        /**
         * 检卡参数错误
         */
        public static final int ERROR_CHECK_PARAMS_ERROR = -0x12;
        /**
         * IC卡公钥为空
         */
        public static final int EMV_PUBLICKEY_NULL = -0x01;
        /**
         * AID参数为空
         */
        public static final int EMV_AID_NULL = -0x02;
        /**
         * AID参数、IC卡公钥都为空
         */
        public static final int EMV_PUBLICKEY_AID_NULL = -0x03;
        /**
         * AID参数、IC卡公钥都存在
         */
        public static final int EMV_PUBLICKEY_AID_EXIST = 0x00;

        public static String getErrCodeMsg(int errcode)
        {
            String msg = "";
            switch (errcode)
            {
                case ERROR_CHECK_REFUSE_MAGCARD_FOR_ICCARD:
                    msg = St8050bApplication.getContext().getString(R.string.ERROR_CHECK_REFUSE_MAGCARD_FOR_ICCARD);
                    break;
            }
            return msg;
        }
    }

    /**
     * 打印机设备错误码
     *
     * @author Administrator
     */
    public static final class Printer {
        /**
         * 缺纸
         */
        public static final int ERROR_PRINT_NOPAPER = 0x01;
        /*** 高温 */
        public static final int ERROR_PRINT_HOT = 0x02;
        /*** 未知错误 */
        public static final int ERROR_PRINT_UNKNOWN = 0x03;
        /**
         * 设备未打开
         */
        public static final int ERROR_DEV_NOT_OPEN = 0x04;
        /**
         * 设备忙
         */
        public static final int ERROR_DEV_IS_BUSY = 0x05;
        /**
         * 打印位图宽度溢出
         */
        public static final int ERROR_PRINT_BITMAP_WIDTH_OVERFLOW = 0x06;
        /**
         * 打印位图错误
         */
        public static final int ERROR_PRINT_BITMAP_OTHER = 0x07;
        /**
         * 打印条码错误
         */
        public static final int ERROR_PRINT_BARCODE_OTHER = 0x08;
        /**
         * 参数错误
         */
        public static final int ERROR_PRINT_ILLIGALARGUMENT = 0x09;
        /*** 打印文本错误 */
        public static final int ERROR_PRINT_TEXT_OTHER = 0x0A;
        /*** mac校验错误(当要求对打印数据进行防串改校验时) */
        public static final int ERROR_PRINT_DATA_MAC = 0x0B;
    }

    /**
     * 磁条卡设备错误码
     *
     * @author Administrator
     */
    public static final class MagCard {
        public static final int DEVICE_IS_BUSY = 0x00;
        public static final int TRACK_DATA_ERR = 0x01;
        public static final int OTHER_ERROR = 0x02;
    }

    /**
     * rf卡设备错误码
     *
     * @author Administrator
     */
    public static final class RFCard {
        /**
         * 设备繁忙
         */
        public static final int ERROR_IS_BUSY = 0;
        /**
         * 设备未打开
         */
        public static final int ERROR_NOT_OPEN = 1;
        /**
         * 密钥类型错误
         */
        public static final int ERROR_KEY_TYPE = -1;
        /**
         * 密钥长度错误
         */
        public static final int ERROR_KEY_LENTH = -2;
        /**
         * 卡片复位信息错误
         */
        public static final int ERROR_RESET_DATA = -3;
        /**
         * 未知错误
         */
        public static final int ERROR_UNKNOWN = -4;
        /**
         * 认证失败
         */
        public static final int ERROR_AUTH_FAIL = -5;
        /**
         * 读数据失败
         */
        public static final int ERROR_READ_FAIL = -6;
        /**
         * 写数据失败
         */
        public static final int ERROR_WRITE_FAIL = -7;
        /**
         * 选择块失败
         */
        public static final int ERROR_SELECT_FAIL = -8;
        /**
         * 加值失败
         */
        public static final int ERROR_ADD_FAIL = -9;
        /**
         * 减值失败
         */
        public static final int ERROR_REDUCE_FAIL = -10;
        /**
         * 传值失败
         */
        public static final int ERROR_DELIVERY_FAIL = -11;
    }

    /**
     * pinpad设备错误码
     *
     * @author Administrator
     */
    public static final class Pinpad {
        /**
         * 无此设备
         */
        public static final int ERROR_NODEV = -1;
        /**
         * pinblock输入次数错误
         */
        public static final int ERROR_INPUTTIMES = -2;
        /**
         * pinblock类型错误
         */
        public static final int ERROR_KEYTYPE = -3;
        /**
         * 操作超时
         */
        public static final int ERROR_TIMEOUT = -4;
        /**
         * 未知错误
         */
        public static final int ERROR_UNKNOWN = -5;
        /**
         * 获取MAC错误
         */
        public static final int ERROR_MAC = -6;
        /**
         * 获取加密错误
         */
        public static final int ERROR_ENCRYPT = -7;
    }

    /**
     * 串口设备
     *
     * @author Administrator
     */
    public static final class SerialPort {
        /**
         * 无此设备
         */
        public static final int ERROR_NODEV = -1;
        /**
         * 设备忙
         */
        public static final int DEVICE_IS_BUSY = -2;
        /**
         * 设备未打开
         */
        public static final int DEVICE_NOT_OPEN = -3;
        /**
         * 读数据出错
         */
        public static final int READ_ERROR = -4;
    }

    /**
     * 系统设备错误码
     *
     * @author Administrator
     */
    public static final class System {
        public static final int INSTALL_SUCCESS = 0x00;
        public static final int INSTALL_FAILED_ALREADY_EXISTS = -1;
        public static final int INSTALL_FAILED_INVALID_APK = -2;
        public static final int INSTALL_FAILED_INVALID_URI = -3;
        public static final int INSTALL_FAILED_INSUFFICIENT_STORAGE = -4;
        public static final int INSTALL_FAILED_DUPLICATE_PACKAGE = -5;
        public static final int INSTALL_FAILED_NO_SHARED_USER = -6;
        public static final int INSTALL_FAILED_UPDATE_INCOMPATIBLE = -7;
        public static final int INSTALL_FAILED_SHARED_USER_INCOMPATIBLE = -8;
        public static final int INSTALL_FAILED_MISSING_SHARED_LIBRARY = -9;
        public static final int INSTALL_FAILED_REPLACE_COULDNT_DELETE = -10;
        public static final int INSTALL_FAILED_DEXOPT = -11;
        public static final int INSTALL_FAILED_OLDER_SDK = -12;
        public static final int INSTALL_FAILED_CONFLICTING_PROVIDER = -13;
        public static final int INSTALL_FAILED_NEWER_SDK = -14;
        public static final int INSTALL_FAILED_TEST_ONLY = -15;
        public static final int INSTALL_FAILED_CPU_ABI_INCOMPATIBLE = -16;
        public static final int INSTALL_FAILED_MISSING_FEATURE = -17;
        public static final int INSTALL_FAILED_CONTAINER_ERROR = -18;
        public static final int INSTALL_FAILED_INVALID_INSTALL_LOCATION = -19;
        public static final int INSTALL_FAILED_MEDIA_UNAVAILABLE = -20;
        public static final int INSTALL_FAILED_VERIFICATION_TIMEOUT = -21;
        public static final int INSTALL_FAILED_VERIFICATION_FAILURE = -22;
        public static final int INSTALL_FAILED_PACKAGE_CHANGED = -23;
        public static final int INSTALL_FAILED_UID_CHANGED = -24;
        public static final int INSTALL_FAILED_VERSION_DOWNGRADE = -25;
        public static final int INSTALL_PARSE_FAILED_NOT_APK = -100;
        public static final int INSTALL_PARSE_FAILED_BAD_MANIFEST = -101;
        public static final int INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION = -102;
        public static final int INSTALL_PARSE_FAILED_NO_CERTIFICATES = -103;
        public static final int INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES = -104;
        public static final int INSTALL_PARSE_FAILED_CERTIFICATE_ENCODING = -105;
        public static final int INSTALL_PARSE_FAILED_BAD_PACKAGE_NAME = -106;
        public static final int INSTALL_PARSE_FAILED_BAD_SHARED_USER_ID = -107;
        public static final int INSTALL_PARSE_FAILED_MANIFEST_MALFORMED = -108;
        public static final int INSTALL_PARSE_FAILED_MANIFEST_EMPTY = -109;
        public static final int INSTALL_FAILED_INTERNAL_ERROR = -110;
    }

    public static final class CPUCard {
        public static final int OPEN_SUCCEED = 0;
        public static final int CARD_NOT_SUPPORTED = 1;
        public static final int OPEN_FAILED = 2;
    }

    public static final class ScannerError {
        /**
         * 已经初始化
         */
        public static final int ERROR_ALREADY_INIT = 109001;
        /**
         * 初始化扫描模组失败
         */
        public static final int ERROR_INIT_SCANNER_ENGINE = 109002;
        /**
         * 初始化解码库失败
         */
        public static final int ERROR_INIT_DECODER = 109003;
        /**
         * 解码失败
         */
        public static final int ERROR_DECODE_FAILED = 109004;
        /**
         * License认证失败
         */
        public static final int ERROR_AUTH_LICENSE = 109005;
        /**
         * 打摄像头失败
         */
        public static final int ERROR_OPEN_CAMERA = 109006;
        /**
         * 传递参数错误
         */
        public static final int ERROR_INPUT_PARAMS = 109007;
    }
}
