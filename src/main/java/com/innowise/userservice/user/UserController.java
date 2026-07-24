package com.innowise.userservice.user;

import com.innowise.userservice.user.dto.CreateUserRequest;
import com.innowise.userservice.user.dto.UpdateUserRequest;
import com.innowise.userservice.user.dto.UserResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UserController {

  private static final int DEFAULT_PAGE_SIZE = 10;
  private static final int DEFAULT_PAGE_NUMBER = 0;
  private final UserService userService;

  @PostMapping
  public ResponseEntity<UserResponse> createUser(
          @RequestBody @Valid CreateUserRequest userDto
  ) {
    return ResponseEntity.status(HttpStatus.CREATED)
            .body(userService.create(userDto));
  }

  @GetMapping("/{id}")
  public ResponseEntity<UserResponse> getUserById(
          @PathVariable(name = "id") Long id
  ) {
    return ResponseEntity.status(HttpStatus.OK)
            .body(userService.getById(id));
  }

  @GetMapping
  public ResponseEntity<List<UserResponse>> getAllUsersBySpecification(
          @RequestParam(name = "name", required = false) String name,
          @RequestParam(name = "surname", required = false) String surname,
          @RequestParam(name = "pageSize", required = false) Integer pageSize,
          @RequestParam(name = "pageNumber", required = false) Integer pageNumber
  ) {
    pageSize = pageSize != null ? pageSize : DEFAULT_PAGE_SIZE;
    pageNumber = pageNumber != null ? pageNumber : DEFAULT_PAGE_NUMBER;

    Pageable pageable = Pageable.ofSize(pageSize).withPage(pageNumber);

    return ResponseEntity.status(HttpStatus.OK)
            .body(userService.getAllByFilter(name, surname, pageable));
  }

  @PatchMapping("/{id}/active")
  public ResponseEntity<UserResponse> updateUserActiveStatus(
          @PathVariable(name = "id") Long id,
          @RequestParam(name = "active") Boolean active
  ) {
    return ResponseEntity.status(HttpStatus.OK)
            .body(userService.setActiveStatus(id, active));
  }

  @PatchMapping("/{id}")
  public ResponseEntity<UserResponse> updateUser(
          @PathVariable(name = "id") Long id,
          @RequestBody @Valid UpdateUserRequest userDto
  ) {
    return ResponseEntity.status(HttpStatus.OK)
            .body(userService.update(id, userDto));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteUser(
          @PathVariable(name = "id") Long id
  ) {
    userService.delete(id);
    return ResponseEntity.noContent().build();
  }
}