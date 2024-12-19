package com.sti.accounting.securityLayer.core;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class Argon2Cipher {
    private static final Logger log = LoggerFactory.getLogger(Argon2Cipher.class);

    @Value("${app.cipher.salt.length}")
    private int saltLength; // salt length in bytes


    @Value("${app.cipher.hash.length}")
    private int hashLength; // hash length in bytes


    @Value("${app.cipher.parallelism}")
    private int parallelism ; // currently not supported by Spring Security


    @Value("${app.cipher.memory}")
    private int memory;
    // memory costs, parameter k

    @Value("${app.cipher.iterations}")
    private int iterations;


    public Argon2Cipher() {
    }


    public String encrypt(String password) {
        try {
            Argon2PasswordEncoder encoder = getInstance();
            return encoder.encode(password);
        } catch (Exception e) {
            log.error("Error encrypting password", e);
            throw new RuntimeException(e);
        }
    }

    public boolean matches(String password, String hash) {
        try {
            Argon2PasswordEncoder encoder = getInstance();
            return encoder.matches(password, hash);
        } catch (Exception e) {
            log.error("Error matching password", e);
            throw new RuntimeException(e);
        }
    }


    private Argon2PasswordEncoder getInstance() {
        return new Argon2PasswordEncoder(saltLength, hashLength, parallelism, memory, iterations);
    }
}
