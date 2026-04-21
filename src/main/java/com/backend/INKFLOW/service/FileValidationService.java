package com.backend.INKFLOW.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;

@Service
public class FileValidationService {

    private static final byte[] MAGIC_JPEG = {(byte)0xFF, (byte)0xD8, (byte)0xFF};
    private static final byte[] MAGIC_PNG  = {(byte)0x89, 0x50, 0x4E, 0x47};
    private static final byte[] MAGIC_WEBP = {0x52, 0x49, 0x46, 0x46}; // "RIFF"

    private static final long MAX_SIZE = 5 * 1024 * 1024; // 5MB

    public void validar(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Arquivo vazio.");

        if (file.getSize() > MAX_SIZE)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Arquivo excede 5MB.");

        byte[] header = new byte[8];
        try (InputStream is = file.getInputStream()) {
            is.read(header);
        }

        if (!startsWith(header, MAGIC_JPEG) &&
            !startsWith(header, MAGIC_PNG)  &&
            !startsWith(header, MAGIC_WEBP)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Tipo de arquivo nao permitido. Use JPEG, PNG ou WEBP.");
        }
    }

    private boolean startsWith(byte[] data, byte[] prefix) {
        if (data.length < prefix.length) return false;
        for (int i = 0; i < prefix.length; i++)
            if (data[i] != prefix[i]) return false;
        return true;
    }
}
