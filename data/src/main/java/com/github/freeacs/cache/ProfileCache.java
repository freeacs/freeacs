package com.github.freeacs.cache;

import com.github.freeacs.dao.Profile;
import com.github.freeacs.dao.ProfileDao;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import io.vavr.control.Option;
import org.springframework.stereotype.Component;

@Component
public class ProfileCache {
    private static final String KEY = "profiles";

    private final IMap<Long, Profile> cache;

    private final ProfileDao profileDao;

    public ProfileCache(ProfileDao profileDao, HazelcastInstance cache) {
        this.profileDao = profileDao;
        this.cache = cache.getMap(KEY);
    }

    public Option<Profile> getProfile(Long id) {
        if (cache.containsKey(id)) {
            return Option.of(cache.get(id));
        }
        Option<Profile> maybeProfile = profileDao.getProfile(id);
        maybeProfile.forEach(profile -> cache.put(id, profile));
        return maybeProfile;
    }
}
