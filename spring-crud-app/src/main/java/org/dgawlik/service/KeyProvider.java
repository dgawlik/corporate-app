package org.dgawlik.service;

import com.auth0.jwt.interfaces.RSAKeyProvider;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

@Service
public class KeyProvider
        implements RSAKeyProvider {

    @Value("classpath:/corp.der")
    Resource privateKF;

    @Value("classpath:/corppub.der")
    Resource publicKF;

    @SneakyThrows
    @Override
    public RSAPublicKey getPublicKeyById(String s) {

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(Files.readAllBytes(publicKF
                .getFile()
                .toPath()));
        return (RSAPublicKey) keyFactory.generatePublic(pubSpec);
    }

    @SneakyThrows
    @Override
    public RSAPrivateKey getPrivateKey() {

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(Files.readAllBytes(privateKF
                .getFile()
                .toPath()));
        return (RSAPrivateKey) keyFactory.generatePrivate(privSpec);

    }

    @Override
    public String getPrivateKeyId() {

        return "RS256";
    }
}
