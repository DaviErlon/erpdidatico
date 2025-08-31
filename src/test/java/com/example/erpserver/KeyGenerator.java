package com.example.erpserver;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Base64;

public class KeyGenerator {
	public static void main(String[] args) {
		Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
		String base64Key = Base64.getEncoder().encodeToString(key.getEncoded());
		System.out.println("Sua chave fixa em Base64: " + base64Key);
	}
}
