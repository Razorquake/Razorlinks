package com.razorquake.razorlinks.models;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;


@Entity
@Data
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    @Column(unique = true)
    private String username;
    private String password;
    private String role = "ROLE_USER";

    @OneToMany(mappedBy = "user")
    private List<UrlMapping> urlMappings;
}
