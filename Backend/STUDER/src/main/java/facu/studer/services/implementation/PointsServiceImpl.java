package facu.studer.services.implementation;

import facu.studer.entities.users.User;
import facu.studer.repositories.UserRepository;
import facu.studer.services.PointsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PointsServiceImpl implements PointsService {

    private final UserRepository userRepository;

    public PointsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void addPoints(User user, long amount) {
        user.setPoints(user.getPoints() + amount);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deductPoints(User user, long amount) {
        user.setPoints(Math.max(0, user.getPoints() - amount));
        userRepository.save(user);
    }
}
