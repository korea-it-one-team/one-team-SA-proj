package com.lyj.proj.oneteamsaproj.controller;

import com.lyj.proj.oneteamsaproj.Util.Ut;
import com.lyj.proj.oneteamsaproj.service.ExchangeService;
import com.lyj.proj.oneteamsaproj.service.GifticonService;
import com.lyj.proj.oneteamsaproj.vo.Exchange_History;
import com.lyj.proj.oneteamsaproj.vo.Gifticon;
import com.lyj.proj.oneteamsaproj.vo.Rq;
import com.lyj.proj.oneteamsaproj.vo.Exchange_Detail;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        return Ut.jsReplace("", "", "../exchange/gifticons");
    }

    // 교환 신청 목록 조회
    @RequestMapping("/exchange/list")
    public String list(@RequestParam(required = false) String search,
                       @RequestParam(required = false) String status,
                       Model model) {

        List<Exchange_History> exchangeList = exchangeService.getExchangeList(search, status);

        System.out.println("dsfsdfzzzz : " + exchangeList);
        model.addAttribute("exchangeList", exchangeList);
        return "/usr/gifticon/exchangeList";
    }

    @GetMapping("/exchange/detatil")
    public ResponseEntity<Map<String, Object>> getExchangeDetails(HttpServletRequest req, int id) {
        // 교환 상세 정보 가져오기 (DB에서 조회)
        Rq rq = (Rq) req.getAttribute("rq");

        List<Exchange_Detail> exchange = exchangeService.findById(id, rq.getLoginedMemberId());
        if (exchange == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("gifticon_Name", exchange.get(0).getGifticon_Name());
        response.put("member_name", exchange.get(0).getMember_Name());
        response.put("phone", exchange.get(0).getMember_Phone());
        response.put("exchange_Status", exchange.get(0).getExchange_Status());
        response.put("id", exchange.get(0).getExchange_Id());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/exchange/{id}/complete")
    public ResponseEntity<Void> completeExchange(@PathVariable int id) {
        // 교환 상태를 "COMPLETED"로 변경
        boolean updated = exchangeService.completeExchange(id);
        System.out.println("dsknfknx : " + updated);
        if (updated) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }


    @PostMapping("/gifticons/{id}/application")
    public ResponseEntity<Map<String, Object>> getGifticonApplication(HttpServletRequest req, @PathVariable int id) {
        Rq rq = (Rq) req.getAttribute("rq");
        int loginedMemberId = rq.getLoginedMemberId();

        if (loginedMemberId <= 0) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "로그인이 필요합니다."));
        }

        boolean application = exchangeService.gifticon_Application(id, loginedMemberId);
        if (application) {
            return ResponseEntity.ok(Map.of("message", "교환 신청이 완료되었습니다.", "id", id));
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "교환 신청에 실패했습니다."));
    }
}