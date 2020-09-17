package fd.notify;

public class NotificationHint<T> {

    public static final NotificationHint<Boolean> ACTION_ICONS  = new NotificationHint<>("action-icons");
    public static final NotificationHint<String>  CATEGORY      = new NotificationHint<>("category");
    public static final NotificationHint<String>  DESKTOP_ENTRY = new NotificationHint<>("desktop-entry");
    public static final NotificationHint<String>  IMAGE_PATH    = new NotificationHint<>("image-path");
    public static final NotificationHint<Boolean> RESIDENT      = new NotificationHint<>("resident");
    public static final NotificationHint<String>  SOUND_FILE    = new NotificationHint<>("sound-file");
    public static final NotificationHint<Byte>    URGENCY       = new NotificationHint<>("urgency");

    private final String name;

    public NotificationHint(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
