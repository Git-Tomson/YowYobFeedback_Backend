
package com.yowyob.feedback.service;

import com.yowyob.feedback.dto.request.PersonDTO;
import com.yowyob.feedback.dto.request.SubscriptionDTO;
import com.yowyob.feedback.dto.response.SubscriptionStatsDTO;
import org.springframework.data.domain.Page;
import reactor.core.publisher.Mono;

public interface SubscriptionService {
    
    // Suivre une personne
    Mono<SubscriptionDTO> subscribeToPerson(String followerId, String followedId);
    
    // Arrêter de suivre une personne
    Mono<Void> unsubscribeFromPerson(String followerId, String followedId);
    
    // Récupérer les personnes que je suis (following)
    Mono<Page<PersonDTO>> getFollowing(String userId, int page, int size);
    
    // Récupérer mes followers
    Mono<Page<PersonDTO>> getFollowers(String userId, int page, int size);
    
    // Récupérer les statistiques de subscription
    Mono<SubscriptionStatsDTO> getSubscriptionStats(String userId);
    
    // Vérifier si je suis une personne
    Mono<Boolean> isSubscribed(String followerId, String followedId);
}
