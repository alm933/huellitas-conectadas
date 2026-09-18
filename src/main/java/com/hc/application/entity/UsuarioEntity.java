package com.hc.application.entity;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.hc.application.enums.UsuarioRol;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToMany;
@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50, message = "El nombre no puede superar los 50 caracteres")
    @Column(length = 50, nullable = false)
    private String name;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 100, message = "Los apellidos no pueden superar los 100 caracteres")
    @Column(name = "last_name", length = 100, nullable = false)
    private String lastName;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    @Column(unique = true, nullable = false, length = 150)
    private String email;

 
    @Column(name = "password", nullable = false, length = 100)
    private String pass;

    @NotNull(message = "El rol es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UsuarioRol role;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        if (role == null) {
            return List.of();
        }

        return List.of(
                new SimpleGrantedAuthority(role.name())
        );
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public String getPassword() {
        return this.pass;
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
        return activo != null && activo;
    }

    @PrePersist
    protected void alCrear() {

        if (activo == null) {
            activo = true;
        }

        if (role == null) {
            role = UsuarioRol.ADOPTANTE;
        }
    }

    @OneToMany
    (
        mappedBy = "user",
        cascade = CascadeType.REMOVE
    )
    
    private List<TokenEntity> tokens;


    

}