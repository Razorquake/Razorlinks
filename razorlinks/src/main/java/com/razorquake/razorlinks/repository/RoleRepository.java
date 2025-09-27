package com.razorquake.razorlinks.repository;

import com.razorquake.razorlinks.models.AppRole;
import com.razorquake.razorlinks.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRoleName(AppRole appRole);
}
