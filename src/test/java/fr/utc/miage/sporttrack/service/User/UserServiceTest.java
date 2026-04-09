package fr.utc.miage.sporttrack.service.User;

import fr.utc.miage.sporttrack.entity.User.Admin;
import fr.utc.miage.sporttrack.entity.User.Athlete;
import fr.utc.miage.sporttrack.repository.User.AdminRepository;
import fr.utc.miage.sporttrack.repository.User.AthleteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private AthleteRepository athleteRepository;

    @Mock
    private AdminRepository adminRepository;

    @InjectMocks
    private UserService userService;

    private Athlete mockAthlete;
    private Admin mockAdmin;

    @BeforeEach
    void setUp() {
        mockAthlete = new Athlete();
        mockAthlete.setEmail("athlete@test.com");
        mockAthlete.setPassword("hashedpassword");

        mockAdmin = new Admin();
        mockAdmin.setEmail("admin@test.com");
        mockAdmin.setPassword("hashedadminpassword");
    }

    @Test
    void loadUserByUsername_AthleteFound_ReturnsUserDetailsWithUserRole() {
        // Arrange
        when(athleteRepository.findByEmail("athlete@test.com")).thenReturn(Optional.of(mockAthlete));

        // Act
        UserDetails userDetails = userService.loadUserByUsername("athlete@test.com");

        // Assert
        assertNotNull(userDetails);
        assertEquals("athlete@test.com", userDetails.getUsername());
        assertEquals("hashedpassword", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));

        verify(athleteRepository, times(1)).findByEmail("athlete@test.com");
        verify(adminRepository, never()).findByEmail(anyString());
    }

    @Test
    void loadUserByUsername_AdminFound_ReturnsUserDetailsWithAdminRole() {
        // Arrange
        when(athleteRepository.findByEmail("admin@test.com")).thenReturn(Optional.empty());
        when(adminRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(mockAdmin));

        // Act
        UserDetails userDetails = userService.loadUserByUsername("admin@test.com");

        // Assert
        assertNotNull(userDetails);
        assertEquals("admin@test.com", userDetails.getUsername());
        assertEquals("hashedadminpassword", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));

        verify(athleteRepository, times(1)).findByEmail("admin@test.com");
        verify(adminRepository, times(1)).findByEmail("admin@test.com");
    }

    @Test
    void loadUserByUsername_UserNotFound_ThrowsException() {
        // Arrange
        when(athleteRepository.findByEmail("notfound@test.com")).thenReturn(Optional.empty());
        when(adminRepository.findByEmail("notfound@test.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername("notfound@test.com"));

        verify(athleteRepository, times(1)).findByEmail("notfound@test.com");
        verify(adminRepository, times(1)).findByEmail("notfound@test.com");
    }
}
