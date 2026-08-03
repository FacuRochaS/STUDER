package facu.studer.services;

import facu.studer.entities.users.User;

public interface PointsService {
    void addPoints(User user, long amount);
    void deductPoints(User user, long amount);
}
