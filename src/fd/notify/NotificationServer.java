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

public abstract class NotificationServer implements Notifications {

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

    /**
     * Handle a <code>NotificationRequest</code>.<br>
     * <br>
     * If <code>req.replacesId()</code> is 0, the return value is a <code>int</code>
     * that represent the notification. It is unique, and will not be reused unless
     * a <code>Integer.MAX_VALUE</code> number of notifications have been generated.
     * An acceptable implementation may just use an incrementing counter for the ID.
     * The returned ID is always greater than zero. Servers must make sure not to
     * return zero as an ID.<br>
     * <br>
     * If <code>req.replacesId()</code> is not 0, the returned value is the same
     * value as <code>req.replacesId()</code>.
     * 
     * @param req the <code>NotificationRequest</code> to handle.
     * @return the id of the notification.
     */
    public abstract int handleNotificaton(NotificationRequest req);

    /**
     * Cause a notification to be forcefully closed and removed from the user's
     * view. It can be used, for example, in the event that what the notification
     * pertains to is no longer relevant, or to cancel a notification with no
     * expiration time.
     * 
     * @param id the id of the notification to be closed.
     */
    public abstract void closeNotificaton(int id);

    /**
     * This signal is emitted when one of the following occurs: <br>
     * &bull; The user performs some global "invoking" action upon a notification. For
     * instance, clicking somewhere on the notification itself. <br>
     * &bull; The user invokes a specific action as specified in the original Notify
     * request. For example, clicking on an action button.
     * 
     * @param id         The ID of the notification emitting the ActionInvoked
     *                   signal.
     * @param action_key The key of the action invoked. These match the keys sent
     *                   over in the list of actions.
     */
    public final void actionInvoked(int id, String action_key) {
        try {
            conn.sendSignal(new ActionInvoked(
                    NOTIFICATIONS_PATH,
                    new UInt32(id),
                    action_key));
        } catch (DBusException e) {/*ignore*/}
    }

    /**
     * A completed notification is one that has timed out, or has been dismissed by
     * the user.
     * 
     * @param id     The ID of the notification that was closed.
     * @param reason The reason the notification was closed.
     */
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

    public final void connect() throws IOException {
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

    public final void disconnect() {
        if (conn != null) {
            conn.disconnect();
            conn = null;
        }
    }
}
