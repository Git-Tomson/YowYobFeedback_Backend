package com.yowyob.feedback.repository;

import com.yowyob.feedback.entity.Subscription;
import org.springframework.data.domain.Pageable;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface SubscriptionRepository extends ReactiveCrudRepository<Subscription, UUID> {
    
    @Modifying
    @Query("DELETE FROM subscription WHERE followed_id = :followedId AND follower_id = :followerId")
    Mono<Void> deleteByFollowedIdAndFollowerId(UUID followedId, UUID followerId);
    
    // Trouver une subscription spAccifique
    Mono<Subscription> findByFollowedIdAndFollowerId(UUID followedId, UUID followerId);
    
    // VAcrifier si une subscription existe
    Mono<Boolean> existsByFollowedIdAndFollowerId(UUID followedId, UUID followerId);
    
    // Compter le nombre de personnes que je suis (following)
    Mono<Long> countByFollowerId(UUID followerId);
    
    // Compter le nombre de personnes qui me suivent (followers)
    Mono<Long> countByFollowedId(UUID followedId);
    
    // RAccupAcrer toutes les personnes que je suis (paginated)
    Flux<Subscription> findByFollowerId(UUID followerId, Pageable pageable);
    
    // RAccupAcrer mes followers (simplifiAc)
    Flux<Subscription> findByFollowedId(UUID followedId, Pageable pageable);
}
