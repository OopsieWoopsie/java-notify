package fd.notify;

import java.util.List;
import java.util.Map;

public class NotificationRequest {

    private String app_name;
    private int replaces_id;
    private String app_icon;
    private String summary;
    private String body;
    private List<String> actions;
    private Map<String, Object> hints;
    private int expire_timeout;

    public NotificationRequest(
            String app_name,
            int replaces_id,
            String app_icon,
            String summary,
            String body,
            List<String> actions,
            Map<String, Object> hints,
            int expire_timeout) {

        this.app_name = app_name;
        this.replaces_id = replaces_id;
        this.app_icon = app_icon;
        this.summary = summary;
        this.body = body;
        this.actions = actions;
        this.hints = hints;
        this.expire_timeout = expire_timeout;
    }

    public String getAppName() {
        return app_name;
    }

    public int getReplacesId() {
        return replaces_id;
    }

    public String getAppIcon() {
        return app_icon;
    }

    public String getSummary() {
        return summary;
    }

    public String getBody() {
        return body;
    }

    public List<String> getActions() {
        return actions;
    }

    public Map<String, Object> getHints() {
        return hints;
    }

    public int getExpireTimeout() {
        return expire_timeout;
    }
}
