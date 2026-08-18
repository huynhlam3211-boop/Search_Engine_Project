package com.vnsearch.auth;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface UserStore { 
    
    Optional<User> find(String username);
    List<User> findAll();

    void save(User user) throws IOException;

    boolean delete(String username) throws IOException;

    int count();
}
