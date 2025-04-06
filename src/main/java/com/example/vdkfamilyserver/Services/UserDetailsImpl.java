package com.example.vdkfamilyserver.Services;

import com.example.vdkfamilyserver.Models.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "password")
public class UserDetailsImpl implements UserDetails {
    private Long id;
    private String phoneNumber;
    private String password;
    private boolean active;

    @Override
    public String getUsername() {
        return phoneNumber; // Возвращаем номер телефона как username
    }

    public UserDetailsImpl(User user) {
        this.id = user.getId();
        this.phoneNumber = user.getPhoneNumber();
        this.password = user.getPassword();
        this.active = user.isActive();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    // Остальные методы интерфейса UserDetails
    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { return active; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDetailsImpl that = (UserDetailsImpl) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(phoneNumber, that.phoneNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, phoneNumber);
    }
}