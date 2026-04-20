package com.backend.INKFLOW.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class FotoService {

    private final Cloudinary cloudinary;

    public FotoService(
            @Value("${cloudinary.cloud-name}") String cloudName,
            @Value("${cloudinary.api-key}") String apiKey,
            @Value("${cloudinary.api-secret}") String apiSecret) {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret
        ));
    }

    public String upload(MultipartFile file, String publicId) throws IOException {
        Map result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "public_id", publicId,
                "folder", "inkflow/clientes",
                "overwrite", true
        ));
        return (String) result.get("secure_url");
    }

    public void delete(String publicId) throws IOException {
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }

    public String extractPublicId(String url) {
        // ex: https://res.cloudinary.com/xxx/image/upload/v123/inkflow/clientes/cliente_1.jpg
        // retorna: inkflow/clientes/cliente_1
        int uploadIndex = url.indexOf("/upload/");
        if (uploadIndex == -1) return null;
        String path = url.substring(uploadIndex + 8);
        // remove versão (v123/)
        if (path.startsWith("v") && path.contains("/")) {
            path = path.substring(path.indexOf("/") + 1);
        }
        // remove extensão
        int dotIndex = path.lastIndexOf(".");
        if (dotIndex != -1) path = path.substring(0, dotIndex);
        return path;
    }
}
