package com.lyj.proj.oneteamsaproj.controller;


import com.lyj.proj.oneteamsaproj.service.*;
import com.lyj.proj.oneteamsaproj.utils.Ut;
import com.lyj.proj.oneteamsaproj.vo.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartRequest;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Controller
public class UsrArticleController {

    @Autowired
    private Rq rq;

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

    // 액션 메서드, 컨트롤 메서드
    @RequestMapping("/usr/article/detail")
    public String showDetail(HttpServletRequest req, Model model, int id) {

        Rq rq = (Rq) req.getAttribute("rq");

        Article article = articleService.getForPrintArticle(rq.getLoginedMemberId(), id);

//        // -1 싫어요, 0 표현 x, 1 좋아요
//        model.addAttribute("usersReaction", usersReaction);

        ResultData usersReactionRd = reactionPointService.usersReaction(rq.getLoginedMemberId(), "article", id);

        if (usersReactionRd.isSuccess()) {

            model.addAttribute("userCanMakeReaction", usersReactionRd.isSuccess());
        }

        List<Reply> replies = replyService.getForPrintReplies(rq.getLoginedMemberId(), "article", id);

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

                reactionPointService.isAlreadyAddGoodRp(rq.getLoginedMemberId(), id, "article"));

        model.addAttribute("isAlreadyAddBadRp",

                reactionPointService.isAlreadyAddBadRp(rq.getLoginedMemberId(), id, "article"));

        model.addAttribute("loginedMemberId", rq.getLoginedMemberId());

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

        Rq rq = (Rq) req.getAttribute("rq");

        Article article = articleService.getForPrintArticle(rq.getLoginedMemberId(), id);

        if (article == null) {
            return Ut.jsHistoryBack("F-1", Ut.f("%d번 게시글은 없습니다", id));
        }

        GenFile existingFile = genFileService.getGenFileByRelId("article", id);
        if(existingFile != null) {
            model.addAttribute("file", existingFile);
        }

        model.addAttribute("article", article);

        return "usr/article/modify";
    }

    // 로그인 체크 -> 유무 체크 -> 권한 체크 -> 수정
    @RequestMapping("/usr/article/doModify")
    @ResponseBody
    public String doModify(HttpServletRequest req, int id, String title, String body, MultipartRequest multipartRequest) {

        Rq rq = (Rq) req.getAttribute("rq");

        Article article = articleService.getArticleById(id);

        if (article == null) {
            return Ut.jsHistoryBack("F-1", Ut.f("%d번 게시글은 없습니다.", id));
        }

        // -3- 권한 체크
        ResultData userCanModifyRd = articleService.userCanModify(rq.getLoginedMemberId(), article);

        if (userCanModifyRd.isFail()) {
            return Ut.jsHistoryBack(userCanModifyRd.getResultCode(), userCanModifyRd.getMsg());
        }
        if (userCanModifyRd.isSuccess()) {
            articleService.modifyArticle(id, title, body);
        }

        article = articleService.getArticleById(id);

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
    public String doDelete(HttpServletRequest req, int id) {

        Rq rq = (Rq) req.getAttribute("rq");

        // id가 있는지부터 알아야 함.
        Article article = articleService.getArticleById(id);

        if (article == null) {
            return Ut.jsHistoryBack("F-1", Ut.f("%d번 게시글은 없습니다", id));
        }

        ResultData userCanDeleteRd = articleService.userCanDelete(rq.getLoginedMemberId(), article);

        if (userCanDeleteRd.isFail()) {
            return Ut.jsHistoryBack(userCanDeleteRd.getResultCode(), userCanDeleteRd.getMsg());
        }

        if (userCanDeleteRd.isSuccess()) {
            articleService.deleteArticle(id);
        }

        return Ut.jsReplace(userCanDeleteRd.getResultCode(), userCanDeleteRd.getMsg(), "../article/list");
    }

    @RequestMapping("/usr/article/write")
    public String showWrite(Model model) {

        int currentId = articleService.getCurrentArticleId();

        model.addAttribute("currentId", currentId);

        return "usr/article/write";
    }

    @RequestMapping("/usr/article/doWrite")
    @ResponseBody
    public String doWrite(HttpServletRequest req, String boardId, String title, String body, String replaceUri,
                          MultipartRequest multipartRequest) {

        Rq rq = (Rq) req.getAttribute("rq");

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

        ResultData writeArticleRd = articleService.writeArticle(rq.getLoginedMemberId(), title, body, boardId);
        int id = (int) writeArticleRd.getData1();  // 게시물 ID

        Article article = articleService.getArticleById(id);

        // 파일 처리
        Map<String, List<MultipartFile>> fileMap = multipartRequest.getMultiFileMap();

        for (String fileInputName : fileMap.keySet()) {
            List<MultipartFile> multipartFiles = fileMap.get(fileInputName);

            for (MultipartFile multipartFile : multipartFiles) {
                if (!multipartFile.isEmpty()) {
                    // 파일 확장자 확인
                    String fileExtension = getFileExtension(multipartFile.getOriginalFilename());
                    if (isValidVideoExtension(fileExtension) || isValidImageExtension(fileExtension)) {
                        ResultData fileResult = genFileService.save(multipartFile, id);
                        if (!fileResult.getResultCode().startsWith("S-")) {
                            return Ut.jsHistoryBack("F-3", "파일 저장에 실패하였습니다.");
                        }
                    } else {
                        return Ut.jsHistoryBack("F-4", "허용되지 않은 파일 형식입니다.");
                    }
                }
            }
        }

        return Ut.jsReplace(writeArticleRd.getResultCode(), writeArticleRd.getMsg(), "../article/detail?id=" + id);
    }

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
    public String showList(HttpServletRequest req, Model model, @RequestParam(defaultValue = "1") int boardId,
                           @RequestParam(defaultValue = "1") int page,
                           @RequestParam(defaultValue = "title,body") String searchKeywordTypeCode,
                           @RequestParam(defaultValue = "") String searchKeyword) throws IOException {

        Rq rq = (Rq) req.getAttribute("rq");

        Board board = boardService.getBoardById(boardId);

        // 페이지네이션
        int articlesCount = articleService.getArticlesCount(boardId, searchKeywordTypeCode, searchKeyword);

        // 한 페이지에 글 10개
        // 글 20개 -> 2page
        // 글 25개 -> 3page
        int itemsInAPage = 10;

        int pagesCount = (int) Math.ceil(articlesCount / (double) itemsInAPage);

        List<Article> articles = articleService.getForPrintArticles(boardId, itemsInAPage, page, searchKeywordTypeCode,
                searchKeyword);

        if (board == null) {
            return rq.historyBackOnView("없는 게시판입니다.");
        }

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

}