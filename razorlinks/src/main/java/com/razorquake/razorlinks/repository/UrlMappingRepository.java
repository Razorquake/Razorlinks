package com.razorquake.razorlinks.repository;

import com.razorquake.razorlinks.models.UrlMapping;
import com.razorquake.razorlinks.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UrlMappingRepository extends JpaRepository<UrlMapping, Long>, JpaSpecificationExecutor<UrlMapping> {

    @Modifying
    @Query("UPDATE UrlMapping u SET u.clickCount = u.clickCount + 1 WHERE u.id = :id")
    void incrementClickCount(@Param("id") Long id);
    UrlMapping findByShortUrl(String shortUrl);
    List<UrlMapping> findByUser(User user);
}
