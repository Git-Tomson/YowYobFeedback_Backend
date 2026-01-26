
package com.yowyob.feedback.controller;

import com.yowyob.feedback.dto.request.PersonDTO;
import com.yowyob.feedback.dto.request.SubscriptionDTO;
import com.yowyob.feedback.dto.response.SubscriptionStatsDTO;
import com.yowyob.feedback.service.SubscriptionService;
import com.yowyob.feedback.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class SubscriptionController {
    
    private final SubscriptionService subscriptionService;
    
    /**
     * POST /api/subscribe/{followedId}
     * S'abonner à une personne (la suivre)
     * 
     * @param followedId L'ID de la personne à suivre
     * @return SubscriptionDTO créée
     */
    @PostMapping("/subscribe/{followedId}")
    public Mono<ResponseEntity<SubscriptionDTO>> subscribe(
            @PathVariable String followedId) {
        
        return SecurityUtil.getAuthenticatedUserId()
                .flatMap(followerId -> subscriptionService.subscribeToPerson(followerId.toString(), followedId)
                        .map(subscription -> ResponseEntity.status(HttpStatus.CREATED).body(subscription)));
    }
    
    /**
     * DELETE /api/unsubscribe/{followedId}
     * Se désabonner d'une personne (arrêter de la suivre)
     * 
     * @param followedId L'ID de la personne à ne plus suivre
     * @return 204 No Content
     */
    @DeleteMapping("/unsubscribe/{followedId}")
    public Mono<ResponseEntity<Void>> unsubscribe(
            @PathVariable String followedId) {
        
        return SecurityUtil.getAuthenticatedUserId()
                .flatMap(followerId -> subscriptionService.unsubscribeFromPerson(followerId.toString(), followedId)
                        .then(Mono.just(ResponseEntity.noContent().build())));
    }
    
    /**
     * GET /api/following
     * Récupérer la liste des personnes que je suis
     * 
     * @param page Numéro de page (défaut: 0)
     * @param size Taille de la page (défaut: 20)
     * @return Page de PersonDTO
     */
    @GetMapping("/following")
    public Mono<ResponseEntity<Page<PersonDTO>>> getFollowing(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        return SecurityUtil.getAuthenticatedUserId()
                .flatMap(userId -> subscriptionService.getFollowing(userId.toString(), page, size)
                        .map(ResponseEntity::ok));
    }
    
    /**
     * GET /api/followers
     * Récupérer la liste de mes followers
     * 
     * @param page Numéro de page (défaut: 0)
     * @param size Taille de la page (défaut: 20)
     * @return Page de PersonDTO
     */
    @GetMapping("/followers")
    public Mono<ResponseEntity<Page<PersonDTO>>> getFollowers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        return SecurityUtil.getAuthenticatedUserId()
                .flatMap(userId -> subscriptionService.getFollowers(userId.toString(), page, size)
                        .map(ResponseEntity::ok));
    }
    
    /**
     * GET /api/subscription/stats
     * Récupérer les statistiques d'abonnement de l'utilisateur connecté
     * 
     * @return SubscriptionStatsDTO avec le nombre de personnes suivies et de followers
     */
    @GetMapping("/subscription/stats")
    public Mono<ResponseEntity<SubscriptionStatsDTO>> getSubscriptionStats() {
        
        return SecurityUtil.getAuthenticatedUserId()
                .flatMap(userId -> subscriptionService.getSubscriptionStats(userId.toString())
                        .map(ResponseEntity::ok));
    }
    
    /**
     * GET /api/subscription/check/{followedId}
     * Vérifier si je suis une personne
     * 
     * @param followedId L'ID de la personne à vérifier
     * @return boolean true si abonné, false sinon
     */
    @GetMapping("/subscription/check/{followedId}")
    public Mono<ResponseEntity<Boolean>> checkSubscription(
            @PathVariable String followedId) {
        
        return SecurityUtil.getAuthenticatedUserId()
                .flatMap(followerId -> subscriptionService.isSubscribed(followerId.toString(), followedId)
                        .map(ResponseEntity::ok));
    }
}
