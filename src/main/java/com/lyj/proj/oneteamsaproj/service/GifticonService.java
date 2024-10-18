package com.lyj.proj.oneteamsaproj.service;

import com.lyj.proj.oneteamsaproj.repository.ExchangeRepository;
import com.lyj.proj.oneteamsaproj.repository.GifticonRepository;
import com.lyj.proj.oneteamsaproj.vo.Gifticon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class GifticonService {

    @Autowired
    private GifticonRepository gifticonRepository;

    public List<Gifticon> getAllGifticons() {
        return gifticonRepository.findAll();
    }

    public int getAllGifticonCount(String searchKeyword) {
        return gifticonRepository.getGifticonCount(searchKeyword);
    }


    public List<Gifticon> getForPrintGifticons(int itemsInAPage, int page, String searchKeyword) {
        int limitFrom = (page - 1) * itemsInAPage;
        int limitTake = itemsInAPage;

        if (limitFrom < 0) {
            // limitFrom이 -1일 경우 모든 결과를 반환하는 로직
            return gifticonRepository.findAll();
        } else {
            return gifticonRepository.getForPrintGifticons(limitFrom, limitTake, searchKeyword);
        }
    }


    public String getGifticonUrl(int id) {
        int gifticonId = gifticonRepository.getGifticonid(id);
        String gifticon_Stock = gifticonRepository.getGifticonUrl(gifticonId);
        gifticonRepository.useGifticon(gifticon_Stock);
        return gifticon_Stock;
    }
}
