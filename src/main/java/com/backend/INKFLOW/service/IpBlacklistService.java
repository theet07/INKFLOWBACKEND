package com.backend.INKFLOW.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class IpBlacklistService {

    private final Map<String, Integer> suspectCount = new ConcurrentHashMap<>();
    private final Set<String> blacklist = ConcurrentHashMap.newKeySet();

    private static final int LIMITE_ANTES_DO_BAN = 5;

    public boolean isBanned(String ip) {
        return blacklist.contains(ip);
    }

    public void registrarTentativaSuspeita(String ip) {
        int count = suspectCount.merge(ip, 1, Integer::sum);
        if (count >= LIMITE_ANTES_DO_BAN) {
            blacklist.add(ip);
        }
    }
}
