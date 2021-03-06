package org.innovateuk.ifs.notifications.resource;

import org.innovateuk.ifs.user.domain.User;

/**
 * Represents a User as the source of a given Notification
 */
public class UserNotificationSource implements NotificationSource {

    private User user;

    public UserNotificationSource(User user) {
        this.user = user;
    }

    @Override
    public String getName() {
        return user.getName();
    }

    @Override
    public String getEmailAddress() {
        return user.getEmail();
    }
}
