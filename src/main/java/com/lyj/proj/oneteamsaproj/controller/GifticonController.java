package com.lyj.proj.oneteamsaproj.controller;


import com.lyj.proj.oneteamsaproj.service.ExchangeService;
import com.lyj.proj.oneteamsaproj.service.GifticonService;
import com.lyj.proj.oneteamsaproj.vo.Gifticon;
import com.lyj.proj.oneteamsaproj.vo.Rq;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
public class GifticonController {

    @Autowired
    private GifticonService gifticonService;

    @Autowired
    private ExchangeService exchangeService;

    @RequestMapping("usr/gifticonsList")
    public String getGifticonList(Model model, @RequestParam(defaultValue = "1") int page,
                                  @RequestParam(defaultValue = "") String searchKeyword) {

        int gifticonsCount = gifticonService.getAllGifticonCount(searchKeyword);

        int itemsInAPage = 12;
        int pagesCount = gifticonsCount > 0 ? (int) Math.ceil(gifticonsCount / (double) itemsInAPage) : 1; // 최소 1 페이지

        List<Gifticon> gifticons = gifticonService.getForPrintGifticons(itemsInAPage, page, searchKeyword);

        model.addAttribute("pagesCount", pagesCount);
        model.addAttribute("page", page);
        model.addAttribute("gifticons", gifticons);
        return "/usr/article/gifticonList"; // JSP 파일 이름
    }

    @PostMapping("usr/gifticons/{id}/application")
    public ResponseEntity<Map<String, Object>> getGifticonApplication(HttpServletRequest req, @PathVariable int id) {
        Rq rq = (Rq) req.getAttribute("rq");
        int loginedMemberId = rq.getLoginedMemberId();

        if (loginedMemberId <= 0) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "로그인이 필요합니다."));
        }

        boolean application_Point = exchangeService.getGifticonPoint(id,rq.getLoginedMember().getPoints(), rq.getLoginedMemberId());

        if (!application_Point) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "보유한 Point가 부족합니다."));
        }

        boolean application = exchangeService.gifticon_Application(id, loginedMemberId);

        if (application) {
            return ResponseEntity.ok(Map.of("message", "교환 신청이 완료되었습니다.", "id", id));
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "교환 신청에 실패했습니다."));
    }
}