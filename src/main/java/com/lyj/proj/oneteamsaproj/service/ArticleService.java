package com.lyj.proj.oneteamsaproj.service;


import com.lyj.proj.oneteamsaproj.repository.ArticleRepository;
import com.lyj.proj.oneteamsaproj.utils.Ut;
import com.lyj.proj.oneteamsaproj.vo.Article;
import com.lyj.proj.oneteamsaproj.vo.ResultData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ArticleService {

    @Autowired
    private ArticleRepository articleRepository;

    // 생성자
    public ArticleService(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;

    }

    // 서비스 메서드

    public ResultData writeArticle(int memberId, String title, String body, String boardId) {
        articleRepository.writeArticle(memberId, title, body, boardId);

        int id = articleRepository.getLastInsertId();
        String updatedBody = updateImagePaths(body,Integer.valueOf(boardId) , id);
        articleRepository.bodyUpdate(updatedBody,id);
        return ResultData.from("S-1", Ut.f("%d번 글이 등록되었습니다.", id), "등록 된 게시글의 id", id);

    }

    public String updateImagePaths(String body, int boardId, int articleId) {
        // 정규식으로 이미지 경로 추출
        Pattern pattern = Pattern.compile("!\\[\\]\\((/images/[^)]+)\\)");
        Matcher matcher = pattern.matcher(body);

        StringBuffer updatedBody = new StringBuffer();
        int index = 1;

        while (matcher.find()) {
            String oldPath = matcher.group(1);
            String extension = oldPath.substring(oldPath.lastIndexOf(".")); // 확장자 추출
            String newPath = String.format("/images/article/%d/%d/%d-%d%s", boardId,articleId, articleId, index, extension);

            matcher.appendReplacement(updatedBody, "![](" + newPath + ")");
            index++;
        }
        matcher.appendTail(updatedBody);

        return updatedBody.toString();
    }

    public void deleteArticle(int id) {

        articleRepository.deleteArticle(id);

    }

    public void modifyArticle(int id, String title, String body) {

        articleRepository.modifyArticle(id, title, body);
    }

    public Article getForPrintArticle(int loginedMemberId, int id) {

        Article article = articleRepository.getForPrintArticle(id);

        controlForPrintData(loginedMemberId, article);

        return article;
    }

    public Article getArticleById(int id) {

        return articleRepository.getArticleById(id);
    }

    public List<Article> getForPrintArticles(int boardId, int itemsInAPage, int page, String searchKeywordTypeCode, String searchKeyword) {

        int limitFrom = (page - 1) * itemsInAPage;
        int limitTake = itemsInAPage;


    //  ---- 날짜/시간 표현 구간 시작 ----
    //  게시글 목록의 날짜 표현부분을 현재 날짜를 기준으로 시간(HH:mm)으로 표현, 과거의 게시글은 YYYY-MM-DD 형식으로.

        List<Article> articles = articleRepository.getForPrintArticles(boardId, limitFrom, limitTake, searchKeywordTypeCode, searchKeyword);

        // 게시글 리스트의 날짜를 가공하여 추가
        for (Article article : articles) {
            formatArticleDate(article);
        }

        return articles;
    }

    private void formatArticleDate(Article article) {
        String regDateStr = article.getRegDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); // 실제 날짜 포맷에 맞춰주세요

        // String을 LocalDateTime으로 변환
        LocalDateTime regDate = LocalDateTime.parse(regDateStr, formatter);
        LocalDateTime now = LocalDateTime.now();

        // 오늘 날짜인지 비교하고 포맷 설정
        if (regDate.toLocalDate().isEqual(now.toLocalDate())) {
            article.setFormattedDate(regDate.format(DateTimeFormatter.ofPattern("HH:mm")));
        } else {
            article.setFormattedDate(regDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }
    }
    //  ---- 날짜/시간 표현 구간 끝 ----


    public List<Article> getArticles() {
        return articleRepository.getArticles();
    }

    public int getCurrentArticleId() {
        return articleRepository.getCurrentArticleId();

    }

    private void controlForPrintData(int loginedMemberId, Article article) {
        if (article == null) {
            return;
        }
        ResultData userCanModifyRd = userCanModify(loginedMemberId, article);

        article.setUserCanModify(userCanModifyRd.isSuccess());

        ResultData userCanDeleteRd = userCanDelete(loginedMemberId, article);

        article.setUserCanDelete(userCanModifyRd.isSuccess());

    }

    public ResultData userCanDelete(int loginedMemberId, Article article) {
        if (article.getMemberId() != loginedMemberId) {
            return ResultData.from("F-2", Ut.f("%d번 게시글에 대한 삭제 권한이 없습니다", article.getId()));
        }

        return ResultData.from("S-1", Ut.f("%d번 게시글을 삭제했습니다", article.getId()));
    }

    public ResultData userCanModify(int loginedMemberId, Article article) {
        if (article.getMemberId() != loginedMemberId) {
            return ResultData.from("F-2", Ut.f("%d번 게시글에 대한 수정 권한이 없습니다", article.getId()));
        }
        return ResultData.from("S-1", Ut.f("%d번 게시글을 수정했습니다", article.getId()), "수정된 게시글", article);
    }

    public int getArticlesCount(int boardId, String searchKeywordTypeCode, String searchKeyword) {
        return articleRepository.getArticleCount(boardId, searchKeywordTypeCode, searchKeyword);
    }

    public ResultData increaseHitCount(int id) {
        int affectedRow = articleRepository.increaseHitCount(id);

        if (affectedRow == 0) {
            return ResultData.from("F-1", "해당 게시글 없음", "id", id);
        }

        return ResultData.from("S-1", "해당 게시글 조회수 증가", "id", id);

    }

    public ResultData increaseGoodReactionPoint(int relId) {
        int affectedRow = articleRepository.increaseGoodReactionPoint(relId);

        if (affectedRow == 0) {
            return ResultData.from("F-1", "없는 게시물");
        }

        return ResultData.from("S-1", "좋아요 증가", "affectedRow", affectedRow);
    }

    public ResultData increaseBadReactionPoint(int relId) {
        int affectedRow = articleRepository.increaseBadReactionPoint(relId);

        if (affectedRow == 0) {

            return ResultData.from("F-1", "없는 게시물");
        }

        return ResultData.from("S-1", "싫어요 증가", "affectedRow", affectedRow);
    }

    public ResultData decreaseGoodReactionPoint(int relId) {

        int affectedRow = articleRepository.decreaseGoodReactionPoint(relId);

        if (affectedRow == 0) {

            return ResultData.from("F-1", "없는 게시물");
        }

        return ResultData.from("S-1", "좋아요 감소", "affectedRow", affectedRow);
    }

    public ResultData decreaseBadReactionPoint(int relId) {

        int affectedRow = articleRepository.decreaseBadReactionPoint(relId);

        if (affectedRow == 0) {

            return ResultData.from("F-1", "없는 게시물");
        }

        return ResultData.from("S-1", "싫어요 감소", "affectedRow", affectedRow);

    }

    public Object getArticleHitCount(int id) {
        return articleRepository.getArticleHitCount(id);
    }

    public int getGoodRP(int relId) {

        return articleRepository.getGoodRP(relId);
    }

    public int getBadRP(int relId) {

        return articleRepository.getBadRP(relId);
    }


}
