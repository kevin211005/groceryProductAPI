package com.kevin.adsprovider.Service;

import com.kevin.adsprovider.Data.User;
import com.kevin.adsprovider.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    UserRepository userRepo;

    public Iterable<User> getUsers() {
        return userRepo.findAll();
    }

    public User addUser(User user) {
        try {
            return userRepo.save(user);
        } catch (Exception ex) {
            System.out.println(ex);
        }
        return user;
    }
    public boolean clearAll() {
        try {
            userRepo.deleteAll();
            return true;
        } catch (Exception ex) {
            System.out.println(ex);
            return false;
        }
    }

    public Optional<User> getUser(String id) {
        Optional<User> user = userRepo.findById(id);
        return user;
    }

    public Boolean userExist(String id) {
        return userRepo.existsById(id);
    }

    public Boolean deleteItems(String id) {
        try {
            Optional<User> user = userRepo.findById(id);
            if (user.isPresent()) {
                user.get().setDesiredItem(Arrays.asList());
                addUser(user.get());
            }
            return true;
        } catch (Exception ex) {
            System.out.println("Deleted items error \n" +  ex);
            return false;
        }
    }
}
