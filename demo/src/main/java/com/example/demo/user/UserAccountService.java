package com.example.demo.user;

import com.example.demo.registration.token.ConfirmationToken;
import com.example.demo.registration.token.ConfirmationTokenService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
@Service
public class UserAccountService implements UserDetailsService {

    private final UserAccountRepository userAccountRepository;
    private final static String USER_NOT_FOUND_MESSAGE = "User not found";
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final ConfirmationTokenService confirmationTokenService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userAccountRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND_MESSAGE));
    }

    public List<UserAccount> findAll() {
        return userAccountRepository.findAll();
    }

    public String signUpUser(UserAccount userAccount) {
        boolean userExists = userAccountRepository.findByEmail(userAccount.getEmail()).isPresent();

        if(userExists) {
            throw new IllegalStateException("email already taken.");
        }

        String encodedPassword = bCryptPasswordEncoder.encode(userAccount.getPassword());
        userAccount.setPassword(encodedPassword);
        userAccountRepository.save(userAccount);

        String token = UUID.randomUUID().toString();
        ConfirmationToken confirmationToken = new ConfirmationToken(
                token,
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(15),
                userAccount
        );
        confirmationTokenService.saveConfirmationToken(
                confirmationToken);

        return token;
    }
    public int enableAppUser(String email) {
        return userAccountRepository.enableAppUser(email);
    }


    public int countDecksByUserId(Long userId) {
        return userAccountRepository.countDecksByUserId(userId);
    }

    public Long getUserIdByEmail(String email) {
        try {
            Optional<UserAccount> userAccountOptional = userAccountRepository.findByEmail(email);
            if (userAccountOptional.isPresent()) {
                return userAccountOptional.get().getId();
            }
            // Handle case when user with given email is not found
            return null;
        } catch (Exception e) {
            // Handle any exceptions
            e.getMessage();
            return null;
        }
    }
}
