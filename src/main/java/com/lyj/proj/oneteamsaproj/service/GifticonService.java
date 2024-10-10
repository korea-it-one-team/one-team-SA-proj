package com.lyj.proj.oneteamsaproj.service;

import com.lyj.proj.oneteamsaproj.repository.GifticonRepository;
import com.lyj.proj.oneteamsaproj.vo.Gifticon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GifticonService {

    @Autowired
    private GifticonRepository gifticonRepository;

    public List<Gifticon> getAllGifticons() {
        return gifticonRepository.findAll();
    }
}
