package com.lyj.proj.oneteamsaproj.controller;

import com.lyj.proj.oneteamsaproj.Util.Ut;
import com.lyj.proj.oneteamsaproj.service.ExchangeService;
import com.lyj.proj.oneteamsaproj.service.GifticonService;
import com.lyj.proj.oneteamsaproj.vo.Exchange_History;
import com.lyj.proj.oneteamsaproj.vo.Gifticon;
import com.lyj.proj.oneteamsaproj.vo.Rq;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class GifticonController {

    @Autowired
    private GifticonService gifticonService;

    @Autowired
    private ExchangeService exchangeService;

    @RequestMapping("/gifticons")
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

    @RequestMapping("/doGifticon")
    @ResponseBody
    public String doGifticon(HttpServletRequest req){
        Rq rq = (Rq) req.getAttribute("rq");
        return Ut.jsReplace("", "", "../gifticons");
    }

    // 교환 신청 목록 조회
    @RequestMapping("/list")
    public String list(@RequestParam(required = false) String search,
                       @RequestParam(required = false) String status,
                       Model model) {
        List<Exchange_History> exchangeList = exchangeService.getExchangeList(search, status);

        System.out.println("dsfsdf : " + exchangeList);
        model.addAttribute("exchangeList", exchangeList);
        return "/usr/gifticon/exchangeList";
    }

}
