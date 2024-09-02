package com.example.firebase_crud.service.impl;

import com.example.firebase_crud.dto.UserDTO;
import com.example.firebase_crud.entity.Users;
import com.example.firebase_crud.repo.UserRepo;
import com.example.firebase_crud.service.UserService;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import com.google.firebase.cloud.StorageClient;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;


@Service
@Transactional
public class UserServiceIMPL implements UserService {
    @Autowired
    UserRepo userRepo;

    @Autowired
    ModelMapper modelMapper;


    private File convertToFile(MultipartFile multipartFile, String fileName) {
        File tempFile = new File(fileName);
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(multipartFile.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tempFile;
    }

    @Override
    public String uploadFile(File file, String fileName) {
        try {
            String bucketName = "bucket name";
            BlobId blobId = BlobId.of(bucketName, fileName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("media").build();

            InputStream inputStream = UserServiceIMPL.class.getClassLoader().getResourceAsStream("jason file path");
            if (inputStream == null) {
                throw new IOException("Failed to find the credentials file in classpath.");
            }

            GoogleCredentials credentials = GoogleCredentials.fromStream(inputStream);
            Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
            storage.create(blobInfo, Files.readAllBytes(file.toPath()));

            String DOWNLOAD_URL = String.format("https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media", bucketName, URLEncoder.encode(fileName, StandardCharsets.UTF_8));
            return DOWNLOAD_URL;
        } catch (IOException e) {
            e.printStackTrace();
            return "An error occurred while uploading the file: " + e.getMessage();
        } catch (Exception e) {
            e.printStackTrace();
            return "An unexpected error occurred: " + e.getMessage();
        }
    }


    @Override
    public Users saveUser(UserDTO dto, MultipartFile multipartFile) {
        try {

            Users map = modelMapper.map(dto, Users.class);
            Users save = userRepo.save(map);

            String fileName = save.getUserEmail();

            File file = this.convertToFile(multipartFile, fileName);
            String URL = this.uploadFile(file, fileName);
            file.delete();
            save.setImageUrl(URL);
            return save;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("An error occurred while uploading the file: " + e.getMessage());
        }
    }

    @Override
    public Users updateUser(UserDTO dto, MultipartFile multipartFile) {
        Users byEmail = userRepo.findByEmail(dto.getUserEmail());
        if (byEmail != null) {
            byte[] imageBytes = getImageFromUrl(byEmail.getImageUrl());
            if (imageBytes != null) {
                if(deleteFile(byEmail.getUserEmail())){
                    Users map = modelMapper.map(dto, Users.class);
                    Users save = userRepo.save(map);

                    String fileName = save.getUserEmail();

                    File file = this.convertToFile(multipartFile, fileName);
                    String URL = this.uploadFile(file, fileName);
                    file.delete();
                    save.setImageUrl(URL);
                    return save;
                };
                return byEmail;
            } else {
                throw new RuntimeException("Failed to fetch the image.");
            }
        }
        throw new RuntimeException("User not found");
    }

    @Override
    public Users deleteUser(String email) {
        Users byEmail = userRepo.findByEmail(email);
        if(byEmail != null){

            deleteFile(byEmail.getUserEmail());
            userRepo.delete(byEmail);
            return byEmail;
        }
        throw new RuntimeException("User not found");
    }


    @Override
    public boolean deleteFile(String userEmail) {
        try {

            String bucketName = "bucket name";

            String imageUrl = userEmail;
            String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);

            if (fileName.contains("?")) {
                fileName = fileName.substring(0, fileName.indexOf("?"));
            }

            fileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8.toString());

            InputStream inputStream = UserServiceIMPL.class.getClassLoader().getResourceAsStream("json file path");
            if (inputStream == null) {
                throw new IOException("Failed to find the credentials file in classpath.");
            }
            GoogleCredentials credentials = GoogleCredentials.fromStream(inputStream);
            Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();

            BlobId blobId = BlobId.of(bucketName, fileName);

            boolean deleted = storage.delete(blobId);

            return deleted;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("An error occurred while deleting the file: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("An unexpected error occurred: " + e.getMessage());
        }
    }

    @Override
    public byte[] getImageFromUrl(String imageUrl) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoInput(true);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (InputStream inputStream = connection.getInputStream()) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                }
            } else {
                throw new RuntimeException("Failed to fetch image, response code: " + responseCode);
            }

            return outputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("An error occurred while fetching the image: " + e.getMessage());
        }
    }
}
