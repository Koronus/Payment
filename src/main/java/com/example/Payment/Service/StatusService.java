package com.example.Payment.Service;

import com.example.Payment.Repository.StatusRepository;
import com.example.Payment.Tables.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StatusService {

    @Autowired
    private StatusRepository statusRepository;

    public List<Status> getAllStatus(){return statusRepository.findAll();}


}
