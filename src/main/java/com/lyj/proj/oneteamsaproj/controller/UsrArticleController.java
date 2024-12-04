package com.lyj.proj.oneteamsaproj.controller;


import com.lyj.proj.oneteamsaproj.openCV.FlaskProcessingController;
import com.lyj.proj.oneteamsaproj.service.*;
import com.lyj.proj.oneteamsaproj.utils.RqUtil;
import com.lyj.proj.oneteamsaproj.utils.Ut;
import com.lyj.proj.oneteamsaproj.vo.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartRequest;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Controller
public class UsrArticleController {

    @Autowired
    private FlaskProcessingController flaskProcessingController;

    @Autowired
    private RqUtil rq;

    // 서비스의 생성자가 없는데도 사용할수 있다.
    @Autowired
    private ArticleService articleService;

    @Autowired
    private GenFileService genFileService;

    @Autowired
    private BoardService boardService;

    @Autowired
    private ReactionPointService reactionPointService;

    @Autowired
    private ReplyService replyService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private LoginService loginService;


    // 액션 메서드, 컨트롤 메서드
    @RequestMapping("/usr/article/detail")
    public String showDetail(HttpServletRequest req, Model model, int id) {

        RqUtil rq = (RqUtil) req.getAttribute("rq");

        Article article = articleService.getForPrintArticle(loginService.getLoginedMemberId(), id);

//        // -1 싫어요, 0 표현 x, 1 좋아요
//        model.addAttribute("usersReaction", usersReaction);

        ResultData usersReactionRd = reactionPointService.usersReaction(loginService.getLoginedMemberId(), "article", id);

        if (usersReactionRd.isSuccess()) {

            model.addAttribute("userCanMakeReaction", usersReactionRd.isSuccess());
        }

        List<Reply> replies = replyService.getForPrintReplies(loginService.getLoginedMemberId(), "article", id);

        // 이미지 파일 여러개 첨부했을 때
        List<GenFile> files = genFileService.getFilesByRelTypeCodeAndRelId("article", id);

        int videoFileCount = genFileService.getFileCountByType2CodeAndRelId("video", id);

        int repliesCount = replies.size();

        model.addAttribute("article", article);

        model.addAttribute("replies", replies);
        model.addAttribute("repliesCount", repliesCount);

        model.addAttribute("files", files);
        model.addAttribute("videoFileCount", videoFileCount);

        model.addAttribute("isAlreadyAddGoodRp",

                reactionPointService.isAlreadyAddGoodRp(loginService.getLoginedMemberId(), id, "article"));

        model.addAttribute("isAlreadyAddBadRp",

                reactionPointService.isAlreadyAddBadRp(loginService.getLoginedMemberId(), id, "article"));

        model.addAttribute("loginedMemberId", loginService.getLoginedMemberId());

        return "usr/article/detail";
    }

    @RequestMapping("/usr/article/doIncreaseHitCountRd")
    @ResponseBody
    public ResultData doIncreaseHitCount(int id) {

        ResultData increaseHitCountRd = articleService.increaseHitCount(id);

        if (increaseHitCountRd.isFail()) {
            return increaseHitCountRd;
        }

        ResultData rd = ResultData.newData(increaseHitCountRd, "hitCount", articleService.getArticleHitCount(id));

        rd.setData2("조회수가 증가된 게시글의 id", id);

        return rd;
    }

    @RequestMapping("/usr/article/modify")
    public String showModify(HttpServletRequest req, Model model, int id) {

        RqUtil rq = (RqUtil) req.getAttribute("rq");

        Article article = articleService.getForPrintArticle(loginService.getLoginedMemberId(), id);

        if (article == null) {
            return Ut.jsHistoryBack("F-1", Ut.f("%d번 게시글은 없습니다", id));
        }

        GenFile existingFile = genFileService.getGenFileByRelId("article", id);
        if (existingFile != null) {
            model.addAttribute("file", existingFile);
        }

        model.addAttribute("article", article);

        return "usr/article/modify";
    }

    // 로그인 체크 -> 유무 체크 -> 권한 체크 -> 수정
    @RequestMapping("/usr/article/doModify")
    @ResponseBody
    public String doModify(HttpServletRequest req, int id, String title, String body, String boardId,
                           @RequestParam("imageUrls") String imageUrls,
                           MultipartRequest multipartRequest) {

        RqUtil rq = (RqUtil) req.getAttribute("rq");

        Article article = articleService.getArticleById(id);

        if (article == null) {
            return Ut.jsHistoryBack("F-1", Ut.f("%d번 게시글은 없습니다.", id));
        }

        // -3- 권한 체크
        ResultData userCanModifyRd = articleService.userCanModify(loginService.getLoginedMemberId(), article);

        if (userCanModifyRd.isFail()) {
            return Ut.jsHistoryBack(userCanModifyRd.getResultCode(), userCanModifyRd.getMsg());
        }
        if (userCanModifyRd.isSuccess()) {
            articleService.modifyArticle(id, title, body, boardId);
        }

        article = articleService.getArticleById(id);

        // 이미지 URL들을 처리 (쉼표로 구분된 URL들)
        String[] images = imageUrls.split(",");

        if (imageUrls.length() >= 2) {
            try {
                // images 배열을 순차적으로 처리
                for (String imageUrl : images) {
                    if (imageUrl.split("/").length >= 4) {
                        if (!imageUrl.substring(16, 17).equals(boardId)) {
                            imageService.moveImage(imageUrl, boardId, id);
                        }
                        continue;
                    }
                    // 각 이미지 URL을 처리 (예: DB에 저장하거나, 다른 서비스에 저장)
                    imageService.saveImage(imageUrl, id, boardId);  // 이미지 업로드
                }
            } catch (IOException e) {
                return Ut.jsHistoryBack("F-4", "이미지 업로드 중 오류 발생.");
            }
        }

        // 파일 처리
        Map<String, List<MultipartFile>> fileMap = multipartRequest.getMultiFileMap();

        for (String fileInputName : fileMap.keySet()) {
            List<MultipartFile> multipartFiles = fileMap.get(fileInputName);

            for (MultipartFile multipartFile : multipartFiles) {
                if (!multipartFile.isEmpty()) {
                    // 기존 파일이 있다면 업데이트, 없으면 새로 추가
                    genFileService.updateOrSave(multipartFile, id);
                }
            }
        }

        return Ut.jsReplace(userCanModifyRd.getResultCode(), userCanModifyRd.getMsg(), "../article/detail?id=" + id);
    }

    @RequestMapping("/usr/article/doDelete")
    @ResponseBody
    public String doDelete(HttpServletRequest req, int id) throws IOException {

        RqUtil rq = (RqUtil) req.getAttribute("rq");

        // id가 있는지부터 알아야 함.
        Article article = articleService.getArticleById(id);

        if (article == null) {
            return Ut.jsHistoryBack("F-1", Ut.f("%d번 게시글은 없습니다", id));
        }

        ResultData userCanDeleteRd = articleService.userCanDelete(loginService.getLoginedMemberId(), article);

        if (userCanDeleteRd.isFail()) {
            return Ut.jsHistoryBack(userCanDeleteRd.getResultCode(), userCanDeleteRd.getMsg());
        }

        if (userCanDeleteRd.isSuccess()) {
            articleService.deleteArticle(id);
            imageService.deleteImage(id, article.getBoardId());
        }

        return Ut.jsReplace(userCanDeleteRd.getResultCode(), userCanDeleteRd.getMsg(), "../article/list?boardId=2&page=1");
    }

    @RequestMapping("/usr/article/write")
    public String showWrite(Model model) {

        int currentId = articleService.getCurrentArticleId();

        model.addAttribute("currentId", currentId);

        return "usr/article/write";
    }

    @RequestMapping("/usr/article/doWrite")
    @ResponseBody
    public String doWrite(
            String request,
            String boardId,
            String title,
            String body,
            @RequestParam(required = false) String replaceUri,
            @RequestParam(required = false) String homeTeam,
            @RequestParam(required = false) String awayTeam,
            @RequestParam("imageUrls") String imageUrls,
            MultipartRequest multipartRequest) {

        if (Ut.isEmptyOrNull(title)) {
            return Ut.jsHistoryBack("F-1", "제목을 입력해주세요.");
        }
        if (Ut.isEmptyOrNull(body)) {
            return Ut.jsHistoryBack("F-2", "내용을 입력해주세요.");
        }

        if (Ut.isEmptyOrNull(boardId)) {
            String alertMsg = "게시판을 선택해주세요.";
            return Ut.jsReplace(alertMsg, "../article/write?title=%s&body=%s",
                    Ut.getEncodedUriComponent(title), Ut.getEncodedUriComponent(body));
        }

        ResultData writeArticleRd = articleService.writeArticle(loginService.getLoginedMemberId(), title, body, boardId);
        int id = (int) writeArticleRd.getData1();  // 게시물 ID

        // 이미지 URL들을 처리 (쉼표로 구분된 URL들)
        String[] images = imageUrls.split(",");
        System.out.println("imageUrls : " + imageUrls);
        System.out.println("imageUrls : " + imageUrls.length());

        // images 배열을 사용하여 처리
        // 파일 업로드
        if (imageUrls.length() >= 2) {
            try {
                // images 배열을 순차적으로 처리
                for (String imageUrl : images) {
                    // 각 이미지 URL을 처리 (예: DB에 저장하거나, 다른 서비스에 저장)
                    imageService.saveImage(imageUrl, id, boardId);  // 이미지 업로드
                }
            } catch (IOException e) {
                return Ut.jsHistoryBack("F-4", "이미지 업로드 중 오류 발생.");
            }
        }

        // 파일 처리
        ResultData genFileSaveRs = null;
        Map<String, List<MultipartFile>> fileMap = multipartRequest.getMultiFileMap();
        for (String fileInputName : fileMap.keySet()) {
            List<MultipartFile> multipartFiles = fileMap.get(fileInputName);

            for (MultipartFile multipartFile : multipartFiles) {
                if (!multipartFile.isEmpty()) {
                    genFileSaveRs = genFileService.save(multipartFile, id); // 기존 파일 저장
                }
            }
        }

        System.out.println("genFileSaveRs : " + genFileSaveRs.toString());

        // REQUEST 처리
        if ("true".equals(request)) {

            System.out.println("request processing...");

            String videoFilePath = (String) genFileSaveRs.getBody().get("fileRealPath");

            System.out.println("homeTeam : " + homeTeam);
            System.out.println("awayTeam : " + awayTeam);

            if (Ut.isEmptyOrNull(homeTeam) || Ut.isEmptyOrNull(awayTeam)) {
                return Ut.jsHistoryBack("F-5", "홈팀과 어웨이 팀 정보를 입력해주세요.");
            }

            if (videoFilePath == null) {
                return Ut.jsHistoryBack("F-4", "동영상 파일이 업로드되지 않았습니다.");
            }

            try {
                // Flask 서버로 파일과 팀 정보 전송
                ResultData flaskResponse = flaskProcessingController.videoProcess(videoFilePath, homeTeam, awayTeam);

                if (flaskResponse.isSuccess()) {
                    return Ut.jsReplace("S-1", "동영상 처리가 시작되었습니다.", "../article/detail?id=" + id);
                } else {
                    return Ut.jsHistoryBack(flaskResponse.getResultCode(), flaskResponse.getMsg());
                }
            } catch (Exception e) {
                e.printStackTrace();
                return Ut.jsHistoryBack("F-Flask", "Flask 서버와의 통신 중 오류가 발생했습니다.");
            }
        }

        return Ut.jsReplace(writeArticleRd.getResultCode(), writeArticleRd.getMsg(), "../article/detail?id=" + id);
    }

    @RequestMapping("/usr/article/request")
    public String showRequest(Model model) {

        int currentId = articleService.getCurrentArticleId();

        model.addAttribute("currentId", currentId);

        return "usr/article/request";
    }

//    public String doRequest() {
//
//    }

    // 파일 확장자 추출 메서드
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex != -1) {
            return filename.substring(lastDotIndex + 1).toLowerCase();
        }
        return "";
    }

    // 동영상 확장자 유효성 검사
    private boolean isValidVideoExtension(String extension) {
        List<String> validVideoExtensions = Arrays.asList("mp4", "avi", "mov", "mkv", "flv");
        return validVideoExtensions.contains(extension);
    }

    // 이미지 확장자 유효성 검사
    private boolean isValidImageExtension(String extension) {
        List<String> validImageExtensions = Arrays.asList("jpg", "jpeg", "png", "gif", "bmp");
        return validImageExtensions.contains(extension);
    }

    @RequestMapping("/usr/article/list")
    public String showList(HttpServletRequest req, Model model,
                           @RequestParam(defaultValue = "1") Integer boardId,
                           @RequestParam(defaultValue = "1") int page,
                           @RequestParam(defaultValue = "title,body") String searchKeywordTypeCode,
                           @RequestParam(defaultValue = "") String searchKeyword) throws IOException {

        RqUtil rq = (RqUtil) req.getAttribute("rq");

        // boardId 유효성 검사
        if (boardId == null || boardId <= 0) {
            return rq.historyBackOnView("잘못된 게시판 ID입니다.");
        }

        Board board = boardService.getBoardById(boardId);

        if (board == null) {
            return rq.historyBackOnView("없는 게시판입니다.");
        }

        // 페이지네이션
        int articlesCount = articleService.getArticlesCount(boardId, searchKeywordTypeCode, searchKeyword);

        int itemsInAPage = 10;

        int pagesCount = (int) Math.ceil(articlesCount / (double) itemsInAPage);

        List<Article> articles = articleService.getForPrintArticles(boardId, itemsInAPage, page, searchKeywordTypeCode,
                searchKeyword);

        model.addAttribute("articles", articles);
        model.addAttribute("articlesCount", articlesCount);
        model.addAttribute("pagesCount", pagesCount);
        model.addAttribute("board", board);
        model.addAttribute("page", page);
        model.addAttribute("searchKeywordTypeCode", searchKeywordTypeCode);
        model.addAttribute("searchKeyword", searchKeyword);
        model.addAttribute("boardId", boardId);

        return "usr/article/list";
    }

    @RequestMapping("/usr/article/myList")
    public String showMyList(HttpServletRequest req, Model model,
                             @RequestParam(defaultValue = "2,3") List<Integer> boardIds, // 여러 boardId를 받아오도록 수정
                             @RequestParam(defaultValue = "1") int page,
                             @RequestParam(defaultValue = "title,body") String searchKeywordTypeCode,
                             @RequestParam(defaultValue = "") String searchKeyword) throws IOException {

        // 현재 로그인한 회원 ID 가져오기
        int loginedMemberId = loginService.getLoginedMemberId();

        // boardIds 유효성 검사
        if (boardIds == null || boardIds.isEmpty()) {
            return rq.historyBackOnView("잘못된 게시판 ID입니다.");
        }

        // 페이지네이션
        int articlesCount = articleService.getMyArticlesCount(boardIds, loginedMemberId, searchKeywordTypeCode, searchKeyword);

        int itemsInAPage = 10;

        int pagesCount = (int) Math.ceil(articlesCount / (double) itemsInAPage);

        List<Article> articles = articleService.getForPrintMyArticles(boardIds, loginedMemberId, itemsInAPage, page, searchKeywordTypeCode, searchKeyword);

        model.addAttribute("articles", articles);
        model.addAttribute("articlesCount", articlesCount);
        model.addAttribute("pagesCount", pagesCount);
        model.addAttribute("page", page);
        model.addAttribute("searchKeywordTypeCode", searchKeywordTypeCode);
        model.addAttribute("searchKeyword", searchKeyword);
        model.addAttribute("boardIds", boardIds);

        return "usr/article/myList";
    }


}