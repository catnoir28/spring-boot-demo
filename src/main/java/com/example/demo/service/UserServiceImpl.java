package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.entity.Address;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @NotNull
    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public List<UserResponse> findAll() {
        return userRepository.findAll()
                .stream()
                .map(this::buildUserResponse)
                .collect(Collectors.toList());
    }

    @NotNull
    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public UserResponse findById(@NotNull Integer userId) {
        return userRepository.findById(userId)
                .map(this::buildUserResponse)
                .orElseThrow(() -> new EntityNotFoundException("User " + userId + " is not found"));
    }

    @NotNull
    @Override
    @Transactional
    public UserResponse createUser(@NotNull CreateUserRequest request) {
        User user = buildUserRequest(request);
        return buildUserResponse(userRepository.save(user));
    }

    @NotNull
    @Override
    @Transactional
    public UserResponse update(@NotNull Integer userId, @NotNull CreateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User " + userId + " is not found"));

        userUpdate(user, request);
        return buildUserResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void delete(@NotNull Integer userId) {
        userRepository.deleteById(userId);
    }

    // ====== builders ======

    @NotNull
    private UserResponse buildUserResponse(@NotNull User user) {
        Address address = user.getAddress();
        AddressResponse addressResponse = null;

        if (address != null) {
            addressResponse = new AddressResponse()
                    .setCity(address.getCity())
                    .setBuilding(address.getBuilding())
                    .setStreet(address.getStreet());
        }

        return new UserResponse()
                .setId(user.getId())
                .setLogin(user.getLogin())
                .setAge(user.getAge())
                .setFirstName(user.getFirstName())
                .setMiddleName(user.getMiddleName())
                .setLastName(user.getLastName())
                .setAddress(addressResponse);
    }

    @NotNull
    private User buildUserRequest(@NotNull CreateUserRequest request) {
        User user = new User()
                .setLogin(request.getLogin())
                .setAge(request.getAge())
                .setFirstName(request.getFirstName())
                .setMiddleName(request.getMiddleName())
                .setLastName(request.getLastName());

        CreateAddressRequest addressRequest = request.getAddress();
        if (addressRequest != null) {
            user.setAddress(new Address()
                    .setCity(addressRequest.getCity())
                    .setBuilding(addressRequest.getBuilding())
                    .setStreet(addressRequest.getStreet()));
        }

        return user;
    }

    private void userUpdate(@NotNull User user, @NotNull CreateUserRequest request) {
        ofNullable(request.getLogin()).ifPresent(user::setLogin);
        ofNullable(request.getFirstName()).ifPresent(user::setFirstName);
        ofNullable(request.getMiddleName()).ifPresent(user::setMiddleName);
        ofNullable(request.getLastName()).ifPresent(user::setLastName);
        ofNullable(request.getAge()).ifPresent(user::setAge);

        CreateAddressRequest addressRequest = request.getAddress();
        if (addressRequest != null) {
            if (user.getAddress() == null) {
                user.setAddress(new Address());
            }
            ofNullable(addressRequest.getBuilding()).ifPresent(user.getAddress()::setBuilding);
            ofNullable(addressRequest.getStreet()).ifPresent(user.getAddress()::setStreet);
            ofNullable(addressRequest.getCity()).ifPresent(user.getAddress()::setCity);
        }
    }
}