package com.mindary.diary.services.impl;

import com.mindary.diary.models.CustomerEntity;
import com.mindary.diary.repositories.CustomerRepository;
import com.mindary.diary.services.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;
    @Override
    public String getPublicKey(UUID userId) {
        Optional<CustomerEntity> customerEntity = customerRepository.findById(userId);
        return customerEntity.map(CustomerEntity::getPublicKey).orElse(null);
    }
}
