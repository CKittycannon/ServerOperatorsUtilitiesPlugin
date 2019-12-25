package dev.ckitty.mc.soup.ident;

import java.util.Base64;

public class LWCrypter {
    
    private Base64.Encoder encoder;
    private Base64.Decoder decoder;
    private int key;
    
    public LWCrypter(int key) {
        encoder = Base64.getEncoder();
        decoder = Base64.getDecoder();
        this.key = key;
    }
    
    public int getKey() {
        return key;
    }
    
    public String encrypt(String str) {
        byte[] bytes = str.getBytes();
        
        bytes = shift(bytes, bytes.length, key);
        
        bytes = encoder.encode(bytes);
        
        bytes = shift(bytes, -bytes.length, key);
        
        return encoder.encodeToString(bytes);
    }
    
    public String decrypt(String str) {
        byte[] bytes = str.getBytes();
        bytes = decoder.decode(bytes);
    
        bytes = unshift(bytes, -bytes.length, key);
    
        bytes = decoder.decode(bytes);
        
        bytes = unshift(bytes, bytes.length, key);
        
        return new String(bytes);
    }
    
    public byte[] shift(byte[] array, int n, int factor) {
        byte[] copy = new byte[array.length];
        
        for (int i = 0; i < array.length; i++) {
            if (i % 2 == 1)
                copy[i] = (byte) (array[i] + factor * n);
            else
                copy[i] = (byte) (array[i] - factor * n);
        }
        
        return copy;
    }
    
    public byte[] unshift(byte[] array, int n, int factor) {
        byte[] copy = new byte[array.length];
        
        for (int i = 0; i < array.length; i++) {
            if (i % 2 == 1)
                copy[i] = (byte) (array[i] - factor * n);
            else
                copy[i] = (byte) (array[i] + factor * n);
        }
        
        return copy;
    }
    
}
