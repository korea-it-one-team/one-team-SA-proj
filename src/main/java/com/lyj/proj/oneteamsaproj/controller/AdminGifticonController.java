package com.lyj.proj.oneteamsaproj.controller;

import com.lyj.proj.oneteamsaproj.service.AdmGifticonService;
import com.lyj.proj.oneteamsaproj.service.ExchangeService;
import com.lyj.proj.oneteamsaproj.service.GifticonService;
import com.lyj.proj.oneteamsaproj.vo.Exchange_Detail;
import com.lyj.proj.oneteamsaproj.vo.Exchange_History;
import com.lyj.proj.oneteamsaproj.vo.Gifticon;
import com.lyj.proj.oneteamsaproj.vo.Rq;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class AdminGifticonController {

    @Autowired
    private ExchangeService exchangeService;

    @Autowired
    private AdmGifticonService admgifticonService;  // Gifticon 서비스

    @Autowired
    private GifticonService gifticonService;

    private final String UPLOAD_DIR = "/uploads/";


    @PostMapping("adm/exchange/gifticon/{getGifticonId}/upload")
    @PatchMapping("")
    public ResponseEntity<Map<String, Object>> uploadStockAndImage(
            @PathVariable("getGifticonId") int gifticonId,
            @RequestParam("stock") int stock,
            @RequestParam("imageFile") List<MultipartFile> imageFiles) {


        try {
            // 파일 개수로 재고 수량 확인
            if (stock != imageFiles.size()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message","재고 수량이 업로드된 이미지 개수와 일치하지 않습니다."));
            }

            // 파일 저장 로직 (예: 로컬 저장, DB 연결 등)
            for (MultipartFile file : imageFiles) {
                // TODO: 파일 저장 처리 (경로 지정 또는 S3 등)
                admgifticonService.updateStockAndImage(gifticonId, file);
            }
            // 2. 재고 및 이미지 URL 업데이트
            return ResponseEntity.ok(Map.of("message","재고 및 이미지가 성공적으로 업로드되었습니다."));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message","업로드 중 오류가 발생했습니다."));
        }
    }

    // 교환 신청 목록 조회
    @RequestMapping("/adm/exchange/list")
    public String list(@RequestParam(required = false) String search,
                       @RequestParam(required = false) String status,
                       Model model) {

        List<Exchange_History> exchangeList = exchangeService.getExchangeList(search, status);

        model.addAttribute("exchangeList", exchangeList);
        return "adm/exchange/exchangeList";
    }

    @GetMapping("adm/exchange/detail")
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

    @PostMapping("adm/exchange/{id}/complete")
    public ResponseEntity<Void> completeExchange(@PathVariable int id) {
        // 교환 상태를 "COMPLETED"로 변경
        boolean updated = exchangeService.completeExchange(id);
        if (updated) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @RequestMapping("/adm/exchange/gifticonList")
    public String admGifticonList(Model model, @RequestParam(defaultValue = "1") int page) {

        int gifticonsCount = gifticonService.getAllGifticonCount();

        int itemsInAPage = 12;
        int pagesCount = gifticonsCount > 0 ? (int) Math.ceil(gifticonsCount / (double) itemsInAPage) : 1; // 최소 1 페이지

        List<Gifticon> gifticons = gifticonService.getForPrintGifticons(itemsInAPage, page);

        model.addAttribute("pagesCount", pagesCount);
        model.addAttribute("page", page);
        model.addAttribute("gifticons", gifticons);
        return "adm/article/admGifticonList"; // JSP 파일 이름
    }

}