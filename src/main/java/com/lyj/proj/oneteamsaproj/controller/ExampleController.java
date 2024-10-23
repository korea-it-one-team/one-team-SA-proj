package com.lyj.proj.oneteamsaproj.controller;

import com.lyj.proj.oneteamsaproj.service.ExchangeService;
import com.lyj.proj.oneteamsaproj.service.GifticonService;
import com.lyj.proj.oneteamsaproj.vo.Rq;
import jakarta.servlet.http.HttpServletRequest;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.exception.NurigoMessageNotReceivedException;
import net.nurigo.sdk.message.model.Balance;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.model.StorageType;
import net.nurigo.sdk.message.request.MessageListRequest;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.MessageListResponse;
import net.nurigo.sdk.message.response.MultipleDetailMessageSentResponse;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import io.github.cdimascio.dotenv.Dotenv;

@RestController
public class ExampleController {

    final DefaultMessageService messageService;

    private final GifticonService gifticonService;
    private final ExchangeService exchangeService;

    public ExampleController(GifticonService gifticonService, ExchangeService exchangeService) {
        Dotenv dotenv = Dotenv.configure().load(); // .env 파일 로드

        String apiKey = dotenv.get("API_KEY");
        String apiSecret = dotenv.get("API_SECRETKEY");

        if (apiKey == null || apiSecret == null) {
            throw new IllegalStateException("API_KEY or API_SECRETKEY is not set.");
        }

        // 반드시 계정 내 등록된 유효한 API 키, API Secret Key를 입력해주셔야 합니다!
        this.messageService = NurigoApp.INSTANCE.initialize(apiKey, apiSecret, "https://api.coolsms.co.kr");
        this.gifticonService = gifticonService;
        this.exchangeService = exchangeService;
    }

    /**
     * 메시지 조회 예제
     */
    @GetMapping("/get-message-list")
    public MessageListResponse getMessageList() {
        // 검색 조건이 있는 경우에 MessagListRequest를 초기화 하여 getMessageList 함수에 파라미터로 넣어서 검색할 수 있습니다!.
        // 수신번호와 발신번호는 반드시 -,* 등의 특수문자를 제거한 01012345678 형식으로 입력해주셔야 합니다!
        MessageListRequest request = new MessageListRequest();

        // 검색할 건 수, 값 미지정 시 20건 조회, 최대 500건 까지 설정 가능
        // request.setLimit(1);

        // 조회 후 다음 페이지로 넘어가려면 조회 당시 마지막의 messageId를 입력해주셔야 합니다!
        // request.setStartKey("메시지 ID");

        // request.setTo("검색할 수신번호");
        // request.setFrom("검색할 발신번호");

        // 메시지 상태 검색, PENDING은 대기 건, SENDING은 발송 중,COMPLETE는 발송완료, FAILED는 발송에 실패한 모든 건입니다.
        /*
        request.setStatus(MessageStatusType.PENDING);
        request.setStatus(MessageStatusType.SENDING);
        request.setStatus(MessageStatusType.COMPLETE);
        request.setStatus(MessageStatusType.FAILED);
        */

        // request.setMessageId("검색할 메시지 ID");

        // 검색할 메시지 목록
        /*
        ArrayList<String> messageIds = new ArrayList<>();
        messageIds.add("검색할 메시지 ID");
        request.setMessageIds(messageIds);
         */

        // 조회 할 메시지 유형 검색, 유형에 대한 값은 아래 내용을 참고해주세요!
        // SMS: 단문
        // LMS: 장문
        // MMS: 사진문자
        // ATA: 알림톡
        // CTA: 친구톡
        // CTI: 이미지 친구톡
        // NSA: 네이버 스마트알림
        // RCS_SMS: RCS 단문
        // RCS_LMS: RCS 장문
        // RCS_MMS: RCS 사진문자
        // RCS_TPL: RCS 템플릿문자
        // request.setType("조회 할 메시지 유형");

        MessageListResponse response = this.messageService.getMessageList(request);
        System.out.println(response);

        return response;
    }

    /**
     * 단일 메시지 발송 예제
     */
    @PostMapping("/send-one")
    public SingleMessageSentResponse sendOne(int id) {
        Message message = new Message();
        // 발신번호 및 수신번호는 반드시 01012345678 형태로 입력되어야 합니다.
        message.setFrom("01012341234");
        message.setTo("수신번호 입력");
        message.setText("한글 45자, 영자 90자 이하 입력되면 자동으로 SMS타입의 메시지가 추가됩니다.");

        SingleMessageSentResponse response = this.messageService.sendOne(new SingleMessageSendingRequest(message));
        System.out.println(response);

        return response;
    }

    /**
     * MMS 발송 예제
     * 단일 발송, 여러 건 발송 상관없이 이용 가능
     */
    @PostMapping("adm/exchange/{id}/application")
    public SingleMessageSentResponse sendMmsByResourcePath(HttpServletRequest req, @PathVariable int id) throws IOException {
        Rq rq = (Rq) req.getAttribute("rq");

        // 1. 수신자 전화번호 가져오기 및 형식 정리
        String memberPhone = exchangeService.getPhoneNum(id);
        String normalizedPhone = memberPhone.replaceAll("-", ""); // 하이픈 제거

        // 2. 기프티콘 이미지 URL 생성
        String gifticonUrl = "https://storage.googleapis.com/" + gifticonService.getGifticonUrl(id);
        URL url = new URL(gifticonUrl);

        // 3. URL로부터 이미지 다운로드 및 임시 파일 생성
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setDoInput(true);

        try (InputStream inputStream = connection.getInputStream()) {

            // URL에서 파일 이름과 확장자 추출
            String fileName = new File(url.getPath()).getName();
            String fileExtension = "";

            // 파일 이름에서 확장자 추출
            int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
                fileExtension = fileName.substring(dotIndex); // 확장자 추출
            } else {
                fileExtension = ".jpg"; // 기본 확장자 설정
            }

            File tempFile = File.createTempFile("downloaded-", ".jpg");

            try (OutputStream outputStream = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

            // 4. 이미지 파일 업로드 후 Image ID 획득
            String imageId = this.messageService.uploadFile(tempFile, StorageType.MMS, null);
            tempFile.deleteOnExit();  // 임시 파일 삭제 예약

            // 5. 메시지 생성 및 설정
            Message message = new Message();
            message.setFrom("01064480039"); // 발신 번호 설정
            message.setTo(normalizedPhone); // 수신 번호 설정 (동적으로 처리)
            message.setText("KICKNALYSIS에서 신청하신 상품 교환권입니다.");
            message.setImageId(imageId); // 업로드된 이미지 ID 설정

            // 6. 메시지 전송
            return this.messageService.sendOne(new SingleMessageSendingRequest(message));

        } catch (IOException e) {
            e.printStackTrace(); // 로깅 또는 예외 처리 필요
            throw new RuntimeException("이미지를 가져오거나 메시지 전송 중 오류가 발생했습니다.", e);
        } finally {
            connection.disconnect(); // 연결 해제
        }
    }

    /**
     * 여러 메시지 발송 예제
     * 한 번 실행으로 최대 10,000건 까지의 메시지가 발송 가능합니다.
     */
    @PostMapping("/send-many")
    public MultipleDetailMessageSentResponse sendMany() {
        ArrayList<Message> messageList = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            Message message = new Message();
            // 발신번호 및 수신번호는 반드시 01012345678 형태로 입력되어야 합니다.
            message.setFrom("발신번호 입력");
            message.setTo("수신번호 입력");
            message.setText("한글 45자, 영자 90자 이하 입력되면 자동으로 SMS타입의 메시지가 추가됩니다." + i);

            // 메시지 건건 마다 사용자가 원하는 커스텀 값(특정 주문/결제 건의 ID를 넣는등)을 map 형태로 기입하여 전송 후 확인해볼 수 있습니다!
            /*HashMap<String, String> map = new HashMap<>();

            map.put("키 입력", "값 입력");
            message.setCustomFields(map);

            messageList.add(message);*/
        }

        try {
            // send 메소드로 단일 Message 객체를 넣어도 동작합니다!
            // 세 번째 파라미터인 showMessageList 값을 true로 설정할 경우 MultipleDetailMessageSentResponse에서 MessageList를 리턴하게 됩니다!
            MultipleDetailMessageSentResponse response = this.messageService.send(messageList, false, true);

            // 중복 수신번호를 허용하고 싶으실 경우 위 코드 대신 아래코드로 대체해 사용해보세요!
            //MultipleDetailMessageSentResponse response = this.messageService.send(messageList, true);

            System.out.println(response);

            return response;
        } catch (NurigoMessageNotReceivedException exception) {
            System.out.println(exception.getFailedMessageList());
            System.out.println(exception.getMessage());
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
        }
        return null;
    }


    @PostMapping("/send-scheduled-messages")
    public MultipleDetailMessageSentResponse sendScheduledMessages() {
        ArrayList<Message> messageList = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            Message message = new Message();
            // 발신번호 및 수신번호는 반드시 01012345678 형태로 입력되어야 합니다.
            message.setFrom("발신번호 입력");
            message.setTo("수신번호 입력");
            message.setText("한글 45자, 영자 90자 이하 입력되면 자동으로 SMS타입의 메시지가 추가됩니다." + i);

            messageList.add(message);
        }

        try {
            // 과거 시간으로 예약 발송을 진행할 경우 즉시 발송처리 됩니다.
            LocalDateTime localDateTime = LocalDateTime.parse("2022-11-26 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            ZoneOffset zoneOffset = ZoneId.systemDefault().getRules().getOffset(localDateTime);
            Instant instant = localDateTime.toInstant(zoneOffset);

            // 단일 발송도 지원하여 ArrayList<Message> 객체가 아닌 Message 단일 객체만 넣어도 동작합니다!
            MultipleDetailMessageSentResponse response = this.messageService.send(messageList, instant);

            // 중복 수신번호를 허용하고 싶으실 경우 위 코드 대신 아래코드로 대체해 사용해보세요!
            //MultipleDetailMessageSentResponse response = this.messageService.send(messageList, instant, true);

            System.out.println(response);

            return response;
        } catch (NurigoMessageNotReceivedException exception) {
            System.out.println(exception.getFailedMessageList());
            System.out.println(exception.getMessage());
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
        }
        return null;
    }

    /**
     * 잔액 조회 예제
     */
    @GetMapping("/get-balance")
    public Balance getBalance() {
        Balance balance = this.messageService.getBalance();
        System.out.println(balance);

        return balance;
    }
}
