package com.yowyob.feedback.service.impl;

import com.yowyob.feedback.exception.AlreadySubscribedException;
import com.yowyob.feedback.exception.PersonNotFoundException;
import com.yowyob.feedback.entity.AppUser;
import com.yowyob.feedback.entity.Person;
import com.yowyob.feedback.entity.Subscription;
import com.yowyob.feedback.dto.request.PersonDTO;
import com.yowyob.feedback.dto.request.SubscriptionDTO;
import com.yowyob.feedback.dto.response.SubscriptionStatsDTO;
import com.yowyob.feedback.repository.AppUserRepository;
import com.yowyob.feedback.repository.PersonRepository;
import com.yowyob.feedback.repository.SubscriptionRepository;
import com.yowyob.feedback.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {
    
    private final SubscriptionRepository subscriptionRepository;
    private final PersonRepository personRepository;
    private final AppUserRepository appUserRepository;
    
    @Override
    @Transactional
    public Mono<SubscriptionDTO> subscribeToPerson(String followerIdStr, String followedIdStr) {
        UUID followerId = UUID.fromString(followerIdStr);
        UUID followedId = UUID.fromString(followedIdStr);

        if (followerId.equals(followedId)) {
            return Mono.error(new IllegalArgumentException("Vous ne pouvez pas vous suivre vous-même"));
        }
        
        return personRepository.findById(followerId)
                .switchIfEmpty(Mono.error(new PersonNotFoundException("Personne qui suit non trouvée")))
                .then(personRepository.findById(followedId))
                .switchIfEmpty(Mono.error(new PersonNotFoundException("Personne à suivre non trouvée")))
                .then(subscriptionRepository.existsByFollowedIdAndFollowerId(followedId, followerId))
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new AlreadySubscribedException("Vous êtes déjà abonné à cette personne"));
                    }
                    Subscription subscription = Subscription.builder()
                        .followedId(followedId)
                        .followerId(followerId)
                        .followDateTime(OffsetDateTime.now())
                        .build();
                    return subscriptionRepository.save(subscription);
                })
                .flatMap(this::convertToSubscriptionDTO);
    }
    
    @Override
    @Transactional
    public Mono<Void> unsubscribeFromPerson(String followerIdStr, String followedIdStr) {
        UUID followerId = UUID.fromString(followerIdStr);
        UUID followedId = UUID.fromString(followedIdStr);

        if (followerId.equals(followedId)) {
            return Mono.error(new IllegalArgumentException("Vous ne pouvez pas vous désabonner de vous-même"));
        }
        
        return subscriptionRepository.findByFollowedIdAndFollowerId(followedId, followerId)
                .switchIfEmpty(Mono.error(new PersonNotFoundException("Abonnement non trouvé")))
                .flatMap(sub -> subscriptionRepository.deleteByFollowedIdAndFollowerId(followedId, followerId));
    }
    
    @Override
    @Transactional(readOnly = true)
    public Mono<Page<PersonDTO>> getFollowing(String userIdStr, int page, int size) {
        UUID userId = UUID.fromString(userIdStr);

        return personRepository.findById(userId)
                .switchIfEmpty(Mono.error(new PersonNotFoundException("Personne non trouvée")))
                .then(Mono.defer(() -> {
                    Pageable pageable = PageRequest.of(page, size, Sort.by("followDateTime").descending());
                    
                    return subscriptionRepository.findByFollowerId(userId, pageable)
                            .flatMap(sub -> personRepository.findById(sub.getFollowedId())
                                .flatMap(this::convertToPersonDTO))
                            .collectList()
                            .zipWith(subscriptionRepository.countByFollowerId(userId).defaultIfEmpty(0L))
                            .map(tuple -> new PageImpl<>(tuple.getT1(), pageable, tuple.getT2()));
                }));
    }
    
    @Override
    @Transactional(readOnly = true)
    public Mono<Page<PersonDTO>> getFollowers(String userIdStr, int page, int size) {
        UUID userId = UUID.fromString(userIdStr);

        return personRepository.findById(userId)
                .switchIfEmpty(Mono.error(new PersonNotFoundException("Personne non trouvée")))
                .then(Mono.defer(() -> {
                    Pageable pageable = PageRequest.of(page, size, Sort.by("followDateTime").descending());
                    
                    return subscriptionRepository.findByFollowedId(userId, pageable)
                            .flatMap(sub -> personRepository.findById(sub.getFollowerId())
                                .flatMap(this::convertToPersonDTO))
                            .collectList()
                            .zipWith(subscriptionRepository.countByFollowedId(userId).defaultIfEmpty(0L))
                            .map(tuple -> new PageImpl<>(tuple.getT1(), pageable, tuple.getT2()));
                }));
    }
    
    @Override
    @Transactional(readOnly = true)
    public Mono<SubscriptionStatsDTO> getSubscriptionStats(String userIdStr) {
        UUID userId = UUID.fromString(userIdStr);
        
        return personRepository.findById(userId)
                .switchIfEmpty(Mono.error(new PersonNotFoundException("Personne non trouvée")))
                .flatMap(person -> Mono.zip(
                        subscriptionRepository.countByFollowerId(userId).defaultIfEmpty(0L),
                        subscriptionRepository.countByFollowedId(userId).defaultIfEmpty(0L)
                ))
                .map(tuple -> {
                    SubscriptionStatsDTO stats = new SubscriptionStatsDTO();
                    stats.setFollowingCount(tuple.getT1());
                    stats.setFollowersCount(tuple.getT2());
                    return stats;
                });
    }
    
    @Override
    @Transactional(readOnly = true)
    public Mono<Boolean> isSubscribed(String followerIdStr, String followedIdStr) {
        UUID followerId = UUID.fromString(followerIdStr);
        UUID followedId = UUID.fromString(followedIdStr);

        if (followerId.equals(followedId)) {
            return Mono.just(false);
        }
        
        return personRepository.findById(followerId)
                .switchIfEmpty(Mono.error(new PersonNotFoundException("Personne qui suit non trouvée")))
                .then(personRepository.findById(followedId))
                .switchIfEmpty(Mono.error(new PersonNotFoundException("Personne à vérifier non trouvée")))
                .then(subscriptionRepository.existsByFollowedIdAndFollowerId(followedId, followerId));
    }
    
    // Méthodes de conversion réactives
    private Mono<SubscriptionDTO> convertToSubscriptionDTO(Subscription subscription) {
        return Mono.zip(
                personRepository.findById(subscription.getFollowedId()).flatMap(this::convertToPersonDTO),
                personRepository.findById(subscription.getFollowerId()).flatMap(this::convertToPersonDTO)
        ).map(tuple -> {
            SubscriptionDTO dto = new SubscriptionDTO();
            dto.setFollowedId(subscription.getFollowedId());
            dto.setFollowerId(subscription.getFollowerId());
            dto.setFollowed(tuple.getT1());
            dto.setFollower(tuple.getT2());
            dto.setFollowDateTime(subscription.getFollowDateTime().toLocalDateTime());
            return dto;
        });
    }
    
    private Mono<PersonDTO> convertToPersonDTO(Person person) {
        if (person == null) return Mono.empty();
        
        return appUserRepository.findById(person.getPerson_id())
                .flatMap(appUser -> {
                     return Mono.zip(
                            subscriptionRepository.countByFollowerId(person.getPerson_id()).defaultIfEmpty(0L),
                            subscriptionRepository.countByFollowedId(person.getPerson_id()).defaultIfEmpty(0L)
                     ).map(statsTuple -> {
                          PersonDTO dto = new PersonDTO();
                          dto.setUserId(person.getPerson_id());
                          dto.setOccupation(person.getOccupation());
                          
                          // Populate from AppUser
                          dto.setFirstName(appUser.getUser_firstname());
                          dto.setLastName(appUser.getUser_lastname());
                          dto.setEmail(appUser.getEmail());
                          dto.setContact(appUser.getContact());
                          dto.setProfileImage(appUser.getUser_logo());
                          dto.setDomain(appUser.getDomain());
                          dto.setDescription(appUser.getDescription());
                          dto.setCertified(appUser.getCertified());
                          dto.setUserType(appUser.getUser_type() != null ? appUser.getUser_type().name() : null);
                          dto.setRegistrationDateTime(appUser.getRegistration_date_time() != null ? appUser.getRegistration_date_time().toLocalDateTime() : null);
                          
                          SubscriptionStatsDTO stats = new SubscriptionStatsDTO();
                          stats.setFollowingCount(statsTuple.getT1());
                          stats.setFollowersCount(statsTuple.getT2());
                          dto.setSubscriptionStats(stats);
                          
                          return dto;
                     });
                });
    }
}
