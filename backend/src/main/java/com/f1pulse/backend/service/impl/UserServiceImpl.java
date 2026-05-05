package com.f1pulse.backend.service.impl;

import com.f1pulse.backend.dto.UserResponse;
import com.f1pulse.backend.dto.UserSummaryResponse;
import com.f1pulse.backend.dto.FavoriteDriverRequest;
import com.f1pulse.backend.exception.UserNotFoundException;
import com.f1pulse.backend.model.User;
import com.f1pulse.backend.repository.UserRepository;
import com.f1pulse.backend.service.UserService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@ConditionalOnProperty(name = "security.enabled", havingValue = "true", matchIfMissing = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserResponse getCurrentUser(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return new UserResponse(
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getFavoriteDriver()
        );
    }

   @Override
public List<UserSummaryResponse> getAllUsers(int page, int size) {

    Pageable pageable = PageRequest.of(page, size);

    Page<User> userPage = userRepository.findAll(pageable);

    return userPage.getContent()
            .stream()
            .map(user -> new UserSummaryResponse(
                    user.getEmail(),
                    user.getRole()
            ))
            .toList();
}

    @Override
    public UserResponse updateFavoriteDriver(String email, FavoriteDriverRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setFavoriteDriver(request.getFavoriteDriver());
        userRepository.save(user);

        return new UserResponse(
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getFavoriteDriver()
        );
    }
}
