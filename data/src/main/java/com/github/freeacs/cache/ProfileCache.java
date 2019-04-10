package com.github.freeacs.cache;

import com.github.freeacs.dao.Profile;
import com.github.freeacs.dao.ProfileDao;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import io.vavr.collection.List;
import io.vavr.control.Option;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class ProfileCache {
    private static final String KEY_BY_ID = "profilesById";

    private final IMap<Long, Profile> idCache;

    private final ProfileDao profileDao;

    public ProfileCache(ProfileDao profileDao, @Qualifier("hazelcastInstance") HazelcastInstance cache) {
        this.profileDao = profileDao;
        this.idCache = cache.getMap(KEY_BY_ID);
    }

    public Option<Profile> getProfileById(Long id) {
        Profile profileFromCache = idCache.get(id);
        if (profileFromCache != null) {
            return Option.of(profileFromCache);
        }
        return profileDao.getProfileById(id)
            .map(profileFromDb -> {
               idCache.put(id, profileFromDb);
               return profileFromDb;
            });
    }

    public Long createProfile(Profile profile) {
        Long newId = profileDao.createProfile(profile);
        Profile withId = profile.withId(newId);
        idCache.put(newId, withId);
        return newId;
    }

    public List<Profile> getProfiles(Long unitTypeId) {
        List<Profile> profiles = profileDao.getProfiles(unitTypeId);
        profiles.forEach(profile -> {
            if (!idCache.containsKey(profile.getId())) {
                idCache.put(profile.getId(), profile);
            }
        });
        return profiles;
    }
}
