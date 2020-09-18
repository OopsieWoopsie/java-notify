package fd.notify;

import java.io.IOException;

public class ListenerImpl extends NotificationListener {

    public static void main(String[] args) throws IOException {
        NotificationServer server = new NotificationServer("ConsoleNotifications", "Sheidy", "1.0");
        server.setListener(new ListenerImpl());
        server.connect();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> server.disconnect()));
    }

    private int notificationNumber = 0;

    @Override
    public int handleNotificaton(NotificationRequest req) {
        System.out.println(req.toString());

        int toReturn = req.getReplacesId();

        if (toReturn == 0) {
            toReturn = ++notificationNumber;
        }

        try {
            return toReturn;
        } finally {
            // this server is not interactive,
            // this is to avoid the sender
            // to wait for an user interaction
            closeNotificaton(toReturn);
        }
    }

    @Override
    public void closeNotificaton(int id) {
        // emit NotificationClosed signal
        notificationClosed(id, ClosedExpired);
    }
}
