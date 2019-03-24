package com.github.freeacs.cache;

import com.github.freeacs.dao.Profile;
import com.github.freeacs.dao.ProfileDao;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import io.vavr.control.Option;
import org.springframework.stereotype.Component;

@Component
public class ProfileCache {
    private static final String KEY_BY_ID = "profilesById";
    private static final String KEY_BY_NAME = "profilesByName";

    private final IMap<Long, Profile> idCache;
    private final IMap<String, Profile> nameCache;

    private final ProfileDao profileDao;

    public ProfileCache(ProfileDao profileDao, HazelcastInstance cache) {
        this.profileDao = profileDao;
        this.idCache = cache.getMap(KEY_BY_ID);
        this.nameCache = cache.getMap(KEY_BY_NAME);
    }

    public Option<Profile> getProfile(Long id) {
        if (idCache.containsKey(id)) {
            return Option.of(idCache.get(id));
        }
        Option<Profile> maybeProfile = profileDao.getProfile(id);
        maybeProfile.forEach(profile -> {
            idCache.put(id, profile);
            nameCache.put(profile.getName(), profile);
        });
        return maybeProfile;
    }

    public Long createProfile(Profile profile) {
        Long newId = profileDao.createProfile(profile);
        Profile withId = profile.withId(newId);
        idCache.put(newId, withId);
        nameCache.put(profile.getName(), withId);
        return newId;
    }
}
