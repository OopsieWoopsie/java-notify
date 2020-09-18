package fd.notify;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.freedesktop.Notifications;
import org.freedesktop.Quad;
import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.UInt32;
import org.freedesktop.dbus.Variant;
import org.freedesktop.dbus.exceptions.DBusException;

public abstract class NotificationServer implements Notifications, AutoCloseable {

    private static final String NOTIFICATIONS_BUS = "org.freedesktop.Notifications";
    private static final String NOTIFICATIONS_PATH = "/org/freedesktop/Notifications";

    /**
     * The notification expired.
     */
    public static final int ClosedExpired = 1;
    /**
     * The notification was dismissed by the user.
     */
    public static final int ClosedDismissed = 2;
    /**
     * The notification was closed by a call to CloseNotification.
     */
    public static final int ClosedManual = 3;
    /**
     * Undefined/reserved reasons.
     */
    public static final int ClosedOther = 4;

    private static final List<String> capabilities = Arrays.asList(
            "body",
            "persistence",
            "sound",
            "icon-static"
    );

    private final Quad<String, String, String, String> serverInfo;

    public NotificationServer(String name, String vendor, String version) {
        this.serverInfo = new Quad<String, String, String, String>(name, vendor, version, "1.2");
    }

    public abstract int handleNotificaton(NotificationRequest req);

    public abstract void closeNotificaton(int id);

    public final void actionInvoked(int id, String action_key) {
        try {
            conn.sendSignal(new ActionInvoked(
                    NOTIFICATIONS_PATH,
                    new UInt32(id),
                    action_key));
        } catch (DBusException e) {/*ignore*/}
    }

    public final void notificationClosed(int id, int reason) {
        try {
            conn.sendSignal(new NotificationClosed(
                    NOTIFICATIONS_PATH,
                    new UInt32(id),
                    new UInt32(reason)));
        } catch (DBusException e) {/*ignore*/}
    }

    /*==============================================*/

    @Override
    public final boolean isRemote() {
        return false;
    }

    @Override
    public final UInt32 Notify(
            String app_name,
            UInt32 replaces_id,
            String app_icon,
            String summary,
            String body,
            List<String> actions,
            Map<String, Variant<?>> hints,
            int expire_timeout) {

        Map<String, Object> newHints = new HashMap<String, Object>();

        for (Entry<String, Variant<?>> hint : hints.entrySet()) {
            newHints.put(hint.getKey(), hint.getValue().getValue());
        }

        NotificationRequest req = new NotificationRequest(
                app_name,
                replaces_id.intValue(),
                app_icon,
                summary,
                body,
                actions,
                newHints,
                expire_timeout);

        return new UInt32(handleNotificaton(req));
    }

    @Override
    public final void CloseNotification(UInt32 id) {
        closeNotificaton(id.intValue());
    }

    @Override
    public final List<String> GetCapabilities() {
        return capabilities;
    }

    @Override
    public final Quad<String, String, String, String> GetServerInformation() {
        return serverInfo;
    }

    /* =================================================== */

    private DBusConnection conn;

    public void connect() throws IOException {
        if (conn == null) {
            try {
                conn = DBusConnection.getConnection(DBusConnection.SESSION);
                conn.requestBusName(NOTIFICATIONS_BUS);
                conn.exportObject(NOTIFICATIONS_PATH, this);
            } catch (DBusException e) {
                throw new IOException(e);
            }
        } else {
            throw new IllegalStateException("Already connected");
        }
    }

    public void disconnect() {
        if (conn != null) {
            conn.disconnect();
            conn = null;
        }
    }

    @Override
    public void close() {
        disconnect();
    }
}
