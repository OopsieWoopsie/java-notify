package fd.notify;

public abstract class NotificationListener {

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

    NotificationServer server;

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
        server.actionInvoked(id, action_key);
    }

    /**
     * A completed notification is one that has timed out, or has been dismissed by
     * the user.
     * 
     * @param id     The ID of the notification that was closed.
     * @param reason The reason the notification was closed.
     */
    public final void notificationClosed(int id, int reason) {
        server.notificationClosed(id, reason);
    }
}
