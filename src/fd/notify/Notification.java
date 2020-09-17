package fd.notify;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.freedesktop.Notifications;
import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.UInt32;
import org.freedesktop.dbus.Variant;
import org.freedesktop.dbus.exceptions.DBusException;

public final class Notification {

    public static final byte UrgencyLow = 0;
    public static final byte UrgencyNormal = 1;
    public static final byte UrgencyCritical = 2;

    private static final String NOTIFICATIONS_BUS = "org.freedesktop.Notifications";
    private static final String NOTIFICATIONS_PATH = "/org/freedesktop/Notifications";

    private final Map<String, Variant<?>> hints;
    private String summary;
    private String body;
    private String application;

    public static void main(String[] args) throws IOException {
        Notification notification = new Notification("Hello!", "I just wanted to say hello :)");
        notification.setUrgency(UrgencyCritical);
        notification.send();
    }

    public Notification(String summary) {
        this(summary, "");
    }

    public Notification(String summary, String body) {
        this("", summary, body);
    }

    public Notification(String application, String summary, String body) {
        this.application = application;
        this.summary = summary;
        this.body = body;
        this.hints = new HashMap<String, Variant<?>>();
        this.setUrgency(UrgencyLow);
    }

    public int send() throws IOException {
        return send(0);
    }

    public int send(int replaces_id) throws IOException {
        try {
            DBusConnection conn = DBusConnection.getConnection(DBusConnection.SESSION);

            Notifications notify = (Notifications) conn.getRemoteObject(
                    NOTIFICATIONS_BUS,
                    NOTIFICATIONS_PATH,
                    Notifications.class);

            UInt32 id = notify.Notify(
                    application,
                    new UInt32(replaces_id),
                    "", // app_icon, not implemented yet
                    summary,
                    body,
                    new LinkedList<String>(), // actions, not implemented yet
                    hints,
                    -1); // timeout, not implemented yet

            try {
                return id.intValue(); // return the id of the created notification
            } finally {
                conn.disconnect(); // closes the DBus connection
            }
        } catch (DBusException e) {
            throw new IOException(e);
        }
    }

    /* setters */

    public <T> void setHint(NotificationHint<T> hint, T value) {
        hints.put(hint.getName(), new Variant<T>(value));
    }

    public void setUrgency(byte level) {
        setHint(NotificationHint.URGENCY, level);
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    /* getters */

    public String getSummary() {
        return summary;
    }

    public String getBody() {
        return body;
    }

    public String getApplication() {
        return application;
    }
}
