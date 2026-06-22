package facu.studer.services.implementation.support;

import facu.studer.entities.users.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class UserDetailsImpl implements UserDetails {

    private final Long id;
    private final String username;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(Long id, String username, String password, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.authorities = authorities;
    }

    // Puedes crear un método estático helper para construirlo fácilmente desde tu entidad User
    public static UserDetailsImpl build(User user) {
        // Si manejas roles/authorities, los mapeas acá. Si no, pasas una lista vacía.
        List<GrantedAuthority> authorities = List.of();

        return new UserDetailsImpl(
                user.getId(),
                user.getUsername(),
                user.getPassword(), // Puede ser null si usas JWT puramente sin estado
                authorities
        );
    }


    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
