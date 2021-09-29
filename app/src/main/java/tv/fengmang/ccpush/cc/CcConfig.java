package tv.fengmang.ccpush.cc;

public class CcConfig {
    public final static int ConnectTimeOutMs = 8000;

    public final static int HANDSHAKE_SUCCESS = 1;
    public final static int HANDSHAKE_FAILED = -1;

    public final static int REPORT_SUCCESS = 1;

    public static long heartbeatInterval = 180 * 1000;
}
