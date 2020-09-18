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

public final class NotificationServer implements Notifications {

    private static final String NOTIFICATIONS_BUS = "org.freedesktop.Notifications";
    private static final String NOTIFICATIONS_PATH = "/org/freedesktop/Notifications";

    private static final List<String> capabilities = Arrays.asList(
            "body",
            "persistence",
            "sound",
            "icon-static"
    );

    private final Quad<String, String, String, String> serverInfo;

    private NotificationListener listener;

    /**
     * Create a new <code>NotificationServer</code> instance.
     * 
     * @param name    The product name of the server.
     * @param vendor  The vendor name. For example, "KDE," "GNOME,"
     *                "freedesktop.org," or "Microsoft."
     * @param version The server's version number.
     */
    public NotificationServer(String name, String vendor, String version) {
        this.serverInfo = new Quad<String, String, String, String>(name, vendor, version, "1.2");
    }

    public void setListener(NotificationListener listener) {
        this.listener = listener;
        this.listener.server = this;
    }

    void actionInvoked(int id, String action_key) {
        try {
            conn.sendSignal(new ActionInvoked(
                    NOTIFICATIONS_PATH,
                    new UInt32(id),
                    action_key));
        } catch (DBusException e) {/*ignore*/}
    }

    void notificationClosed(int id, int reason) {
        try {
            conn.sendSignal(new NotificationClosed(
                    NOTIFICATIONS_PATH,
                    new UInt32(id),
                    new UInt32(reason)));
        } catch (DBusException e) {/*ignore*/}
    }

    /*==============================================*/

    @Override
    public boolean isRemote() {
        return false;
    }

    @Override
    public UInt32 Notify(
            String app_name,
            UInt32 replaces_id,
            String app_icon,
            String summary,
            String body,
            List<String> actions,
            Map<String, Variant<?>> hints,
            int expire_timeout) {

        if (listener == null) return new UInt32(0);

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

        return new UInt32(listener.handleNotificaton(req));
    }

    @Override
    public void CloseNotification(UInt32 id) {
        if (listener != null)
            listener.closeNotificaton(id.intValue());
    }

    @Override
    public List<String> GetCapabilities() {
        return capabilities;
    }

    @Override
    public Quad<String, String, String, String> GetServerInformation() {
        return serverInfo;
    }

    /* =================================================== */

    private DBusConnection conn;

    public void connect() throws IOException {
        if (conn == null) {
            System.out.println("try to conn");
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
}
