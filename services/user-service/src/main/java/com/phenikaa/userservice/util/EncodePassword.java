package com.phenikaa.userservice.util;

import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

public class EncodePassword {

    public static void main(String[] args) {
        String raw = args != null && args.length > 0 ? args[0] : "123456PKA@";
        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        String encoded = encoder.encode(raw);
        System.out.println(encoded);
    }
}


