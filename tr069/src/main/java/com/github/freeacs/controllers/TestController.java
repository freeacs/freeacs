package com.github.freeacs.controllers;

import com.hazelcast.core.HazelcastInstance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ConcurrentMap;

@Slf4j
@RestController
public class TestController {

    @Value("${context-path}")
    private String contextPath;

    @Autowired
    private HazelcastInstance hazelcastInstance;

    private ConcurrentMap<String,String> retrieveMap() {
        return hazelcastInstance.getMap("map");
    }

    @PostMapping("${context-path}/test/put")
    public String put(@RequestParam(value = "key") String key, @RequestParam(value = "value") String value) {
        retrieveMap().put(key, value);
        return value;
    }

    @GetMapping("${context-path}/test/get")
    public String get(@RequestParam(value = "key") String key) {
        return retrieveMap().get(key);
    }
}
