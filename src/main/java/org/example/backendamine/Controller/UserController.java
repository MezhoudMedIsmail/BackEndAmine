package org.example.backendamine.Controller;

import lombok.RequiredArgsConstructor;
import org.example.backendamine.Entities.FileEntity;
import org.example.backendamine.Entities.Response.UploadFileResponse;
import org.example.backendamine.Entities.Response.UserRequest;
import org.example.backendamine.Entities.Response.UserResponse;
import org.example.backendamine.Entities.TypeDepartement;
import org.example.backendamine.Entities.User;
import org.example.backendamine.Service.FileService;
import org.example.backendamine.Service.UserService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userClientService;
    private final FileService dbFileStorageService;

    @GetMapping
    public ResponseEntity<List<UserResponse>> getUser() {
        return new ResponseEntity<List<UserResponse>>(userClientService.getUser(), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody UserRequest userRequest) {
        UserResponse userResponse = userClientService.createUser(userRequest);
        return ResponseEntity.ok(userResponse);
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable long id) {
        User user = userClientService.getUserById(id);
        UserResponse us =UserResponse.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .region(user.getRegion())
                .departement(user.getDepartement().name())
                .matricule(user.getMatricule())
                .phone(user.getPhone())
                .build();
        return new ResponseEntity<UserResponse>(us, HttpStatus.OK);
    }
    @GetMapping(path = "/matricule/{matricule}")
    public ResponseEntity<UserResponse> getUserByMatricule(@PathVariable long matricule) {
        User user = userClientService.getUserByMatricule(matricule);
        UserResponse us =UserResponse.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .region(user.getRegion())
                .departement(user.getDepartement().name())
                .matricule(user.getMatricule())
                .phone(user.getPhone())
                .build();
        return new ResponseEntity<UserResponse>(us, HttpStatus.OK);
    }
    @PutMapping(path = "/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable long id, @RequestBody UserRequest user) {
        User u = new User();
        u.setFirstName(user.getFirstName());
        u.setLastName(user.getLastName());
        u.setEmail(user.getEmail());
        u.setRegion(user.getRegion());
        u.setDepartement(user.getDepartement());
        u.setMatricule(user.getMatricule());
        u.setPhone(user.getPhone());
        return new ResponseEntity<UserResponse>(userClientService.updateUser(id, u), HttpStatus.ACCEPTED);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userClientService.deleteUser(id);
        return ResponseEntity.ok().build(); // Returns an HTTP 200 OK with no body.
    }
    @PostMapping("/uploadFile/{userId}")
    public UploadFileResponse uploadFile(@RequestParam("file") MultipartFile file, @PathVariable long userId) {
        FileEntity dbFile = dbFileStorageService.storeFile(file, userId);

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath().path("/downloadFile/")
                .path(String.valueOf(dbFile.getId())).toUriString();

        return new UploadFileResponse(dbFile.getFileName(), fileDownloadUri, file.getContentType(), file.getSize());
    }
    @GetMapping("/downloadFile/{userId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable long userId) {
        // Load file from database
        FileEntity dbFile = dbFileStorageService.getFile(userId);

        return ResponseEntity.ok().contentType(MediaType.parseMediaType(dbFile.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + dbFile.getFileName() + "\"")
                .body(new ByteArrayResource(dbFile.getData()));
    }
}
