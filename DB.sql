# DB 세팅
DROP DATABASE IF EXISTS `one-team-SA-proj`;
CREATE DATABASE `one-team-SA-proj`;
USE `one-team-SA-proj`;

# 경기일정 테이블 생성
CREATE TABLE gameSchedule (
                              id INT AUTO_INCREMENT PRIMARY KEY,
                              startDate CHAR(100) NOT NULL,
                              matchTime CHAR(100) NOT NULL,
                              leagueName CHAR(100) NOT NULL DEFAULT '기본값',
                              homeTeam CHAR(100) NOT NULL,
                              awayTeam CHAR(100) NOT NULL,
                              homeTeamScore CHAR(5) NOT NULL,
                              awayTeamScore CHAR(5) NOT NULL
);

# 기프티콘 테이블 생성
CREATE TABLE gifticons (
                           id INT AUTO_INCREMENT PRIMARY KEY,
                           `name` VARCHAR(255) NOT NULL,
                           points INT NOT NULL,
                           image_url VARCHAR(255),
                           stock INT NOT NULL,
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE gifticon_Stock(
                               id INT AUTO_INCREMENT PRIMARY KEY,
                               `gifticon_id` INT NOT NULL,
                               image_url TEXT NOT NULL,
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               `use` INT NOT NULL
);

# 회원 테이블 생성
CREATE TABLE `member`(
                         id INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
                         regDate DATETIME NOT NULL,
                         updateDate DATETIME NOT NULL,
                         loginId CHAR(30) NOT NULL,
                         loginPw CHAR(100) NOT NULL,
                         `authLevel` SMALLINT(2) UNSIGNED DEFAULT 3 COMMENT '권한 레벨 (3=일반,7=관리자)',
                         `name` CHAR(20) NOT NULL,
                         nickname CHAR(20) NOT NULL,
                         cellphoneNum CHAR(20) NOT NULL,
                         email CHAR(50) NOT NULL,
                         points INT NOT NULL,
                         delStatus TINYINT(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '탈퇴 여부 (0=탈퇴 전, 1=탈퇴 후)',
                         delDate DATETIME COMMENT '탈퇴 날짜'
);

#신청내역 테이블 생성
CREATE TABLE exchange_history (
                                  id INT AUTO_INCREMENT PRIMARY KEY,
                                  member_id INT NOT NULL,
                                  gifticon_id INT NOT NULL,
                                  points INT NOT NULL,
                                  exchange_status CHAR(100) NOT NULL,
                                  exchange_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);



#포인트 사용 내역 테이블 생성
CREATE TABLE point_transactions (
                                    id INT AUTO_INCREMENT PRIMARY KEY,
                                    member_id INT NOT NULL,
                                    points INT NOT NULL,
                                    transaction_type CHAR(150) NOT NULL, -- 포인트 추가 또는 차감
                                    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

#교환내역 로그 테이블 생성
CREATE TABLE exchange_logs (
                               id INT AUTO_INCREMENT PRIMARY KEY,
                               exchange_id INT NOT NULL,
                               member_id INT NOT NULL,
                               gifticon_id INT NOT NULL,
                               log_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               FOREIGN KEY (exchange_id) REFERENCES exchange_history(id)
);


INSERT INTO `member` (regDate,updateDate, loginId, loginPw, `name`, nickname,cellphoneNum, email, points) VALUES
                                                                                                              (NOW(), NOW(), 'test1', 'test1','test1Name', 'test1Nickname','010-0000-0000', 'user1@example.com', 100),
                                                                                                              (NOW(), NOW(), 'test2', 'test2','test2Name', 'test2Nickname','010-1234-0000', 'user1@example.com', 100),
                                                                                                              (NOW(), NOW(), 'test3', 'test3','test3Name', 'test3Nickname','010-5678-0000', 'user1@example.com', 100);

INSERT INTO gifticons (`name`, points, image_url) VALUES
                                                      ('신세계상품권 모바일교환권  1만원(이마트 교환전용)',13000,'https://img1.kakaocdn.net/thumb/C320x320@2x.fwebp.q82/?fname=https%3A%2F%2Fst.kakaocdn.net%2Fproduct%2Fgift%2Fproduct%2F20200404051421_eadcebac5cf64cd19407128aff11fe2a'),
                                                      ('신세계상품권 모바일교환권  3만원(이마트 교환전용)', 33000, 'https://img1.kakaocdn.net/thumb/C320x320@2x.fwebp.q82/?fname=https%3A%2F%2Fst.kakaocdn.net%2Fproduct%2Fgift%2Fproduct%2F20200404051421_d916c722336e43f58275842678eb0227'),
                                                      ('신세계상품권 모바일교환권  5만원(이마트 교환전용)', 55000, 'https://img1.kakaocdn.net/thumb/C320x320@2x.fwebp.q82/?fname=https%3A%2F%2Fst.kakaocdn.net%2Fproduct%2Fgift%2Fproduct%2F20200404051421_24453196e4f94f83af5d6c5621b5f72e'),
                                                      ('신세계상품권 모바일교환권  10만원(이마트 교환전용)', 115000, 'https://img1.kakaocdn.net/thumb/C320x320@2x.fwebp.q82/?fname=https%3A%2F%2Fst.kakaocdn.net%2Fproduct%2Fgift%2Fproduct%2F20200404051422_cb787dca9f744ef1b297da9696df3e13');

INSERT INTO gifticons (`name`, points, image_url) VALUES
                                                      ('스타벅스 e카드 2만원 교환권',23000,'https://img1.kakaocdn.net/thumb/C320x320@2x.fwebp.q82/?fname=https%3A%2F%2Fst.kakaocdn.net%2Fproduct%2Fgift%2Fproduct%2F20231010105154_d7cf22243fb647a6a27279e090ed63a3.jpg'),
                                                      ('스타벅스 e카드 3만원 교환권',33000,'https://img1.kakaocdn.net/thumb/C320x320@2x.fwebp.q82/?fname=https%3A%2F%2Fst.kakaocdn.net%2Fproduct%2Fgift%2Fproduct%2F20231010105204_8632b94c327549c686f3f090415c5969.jpg'),
                                                      ('스타벅스 e카드 5만원 교환권',55000,'https://img1.kakaocdn.net/thumb/C320x320@2x.fwebp.q82/?fname=https%3A%2F%2Fst.kakaocdn.net%2Fproduct%2Fgift%2Fproduct%2F20231010105213_c0932c6874fb4f26a023f16e81b9bd69.jpg'),
                                                      ('스타벅스 e카드 10만원 교환권',115000,'https://img1.kakaocdn.net/thumb/C320x320@2x.fwebp.q82/?fname=https%3A%2F%2Fst.kakaocdn.net%2Fproduct%2Fgift%2Fproduct%2F20231010105220_ab76aac923e94dd2a81a2f9580542ce9.jpg');

INSERT INTO gifticons (`name`, points, image_url) VALUES
                                                      ('CU 1만원 모바일상품권',13000,'https://img1.kakaocdn.net/thumb/C320x320@2x.fwebp.q82/?fname=https%3A%2F%2Fst.kakaocdn.net%2Fproduct%2Fgift%2Fproduct%2F20230221100420_c5dc56b15a764045b9572f16db5ebcd3.jpg'),
                                                      ('CU 2만원 모바일상품권',23000,'https://img1.kakaocdn.net/thumb/C320x320@2x.fwebp.q82/?fname=https%3A%2F%2Fst.kakaocdn.net%2Fproduct%2Fgift%2Fproduct%2F20230221100445_2006e73b89a742b5ab947f3d6ce5e484.jpg'),
                                                      ('CU 3만원 모바일상품권',33000,'https://img1.kakaocdn.net/thumb/C320x320@2x.fwebp.q82/?fname=https%3A%2F%2Fst.kakaocdn.net%2Fproduct%2Fgift%2Fproduct%2F20230221100507_8b076dadc2f64f118a8aaa638dc5ea84.jpg'),
                                                      ('CU 5만원 모바일상품권',55000,'https://img1.kakaocdn.net/thumb/C320x320@2x.fwebp.q82/?fname=https%3A%2F%2Fst.kakaocdn.net%2Fproduct%2Fgift%2Fproduct%2F20230221100350_0616d4030f324d40bfe4bb39376bc870.jpg');

INSERT INTO gifticons (`name`, points, image_url) VALUES
                                                      ('GS25 1만원 모바일상품권',13000,'https://img1.kakaocdn.net/thumb/C320x320@2x.fwebp.q82/?fname=https%3A%2F%2Fst.kakaocdn.net%2Fproduct%2Fgift%2Fproduct%2F20240913145411_631be4f616b74075ac66185e015dd4c4.png'),
                                                      ('GS25 2만원 모바일상품권',23000,'https://img1.kakaocdn.net/thumb/C320x320@2x.fwebp.q82/?fname=https%3A%2F%2Fst.kakaocdn.net%2Fproduct%2Fgift%2Fproduct%2F20240913145430_97ad95b8ac6b484f917209321fbdadaf.png'),
                                                      ('GS25 3만원 모바일상품권',33000,'https://img1.kakaocdn.net/thumb/C320x320@2x.fwebp.q82/?fname=https%3A%2F%2Fst.kakaocdn.net%2Fproduct%2Fgift%2Fproduct%2F20240913145445_c5e5f4e466174179ad4a38efb43b99c6.png'),
                                                      ('GS25 5만원 모바일상품권',55000,'https://img1.kakaocdn.net/thumb/C320x320@2x.fwebp.q82/?fname=https%3A%2F%2Fst.kakaocdn.net%2Fproduct%2Fgift%2Fproduct%2F20240913145504_72d61554ca3d43d182172b017c307f06.png');

INSERT INTO gifticons (`name`, points, image_url) VALUES
                                                      ('세븐일레븐 5천원 모바일상품권', 6000,'https://img1.kakaocdn.net/thumb/C320x320@2x.fwebp.q82/?fname=https%3A%2F%2Fst.kakaocdn.net%2Fproduct%2Fgift%2Fproduct%2F20240912153854_60ef083b889e4bd39fd4185efef96765.jpg'),
                                                      ('세븐일레븐 1만원 모바일상품권', 13000,'https://img1.kakaocdn.net/thumb/C320x320@2x.fwebp.q82/?fname=https%3A%2F%2Fst.kakaocdn.net%2Fproduct%2Fgift%2Fproduct%2F20231004172647_df4e1b8a1c2a4539a382f87a791ccc21.jpg'),
                                                      ('세븐일레븐 3만원 모바일상품권', 33000,'https://img1.kakaocdn.net/thumb/C320x320@2x.fwebp.q82/?fname=https%3A%2F%2Fst.kakaocdn.net%2Fproduct%2Fgift%2Fproduct%2F20240912154104_1a61f09e37124547a3ff340a8df7c3ee.jpg'),
                                                      ('세븐일레븐 5만원 모바일상품권', 55000,'https://img1.kakaocdn.net/thumb/C320x320@2x.fwebp.q82/?fname=https%3A%2F%2Fst.kakaocdn.net%2Fproduct%2Fgift%2Fproduct%2F20240912153604_f76082cc041142628e89c389ebd20cd4.jpg');

INSERT INTO exchange_history
SET member_id = 2,
gifticon_id = 3,
exchange_status = 'REQUESTED',
exchange_date = NOW();

INSERT INTO exchange_history
SET member_id = 1,
gifticon_id = 10,
exchange_status = 'REQUESTED',
exchange_date = NOW();

SELECT * FROM gifticons;

SELECT * FROM `member`;

SELECT * FROM exchange_history;

# jhb part init 끝
#############

  # 게시글 테이블 생성
CREATE TABLE article(
                        id INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
                        regDate DATETIME NOT NULL,
                        updateDate DATETIME NOT NULL,
                        title CHAR(100) NOT NULL,
                        `body` TEXT NOT NULL
);

## 게시글 테스트 데이터 생성
INSERT INTO article
SET regDate = NOW(),
updateDate = NOW(),
title = '제목1',
`body` = '내용1';

INSERT INTO article
SET regDate = NOW(),
updateDate = NOW(),
title = '제목2',
`body` = '내용2';

INSERT INTO article
SET regDate = NOW(),
updateDate = NOW(),
title = '제목3',
`body` = '내용3';

INSERT INTO article
SET regDate = NOW(),
updateDate = NOW(),
title = '제목4',
`body` = '내용4';

ALTER TABLE article ADD COLUMN memberId INT(10) UNSIGNED NOT NULL AFTER updateDate;

UPDATE article
SET memberId = 2
WHERE id IN (1,2);

UPDATE article
SET memberId = 3
WHERE id IN (3,4);


# 게시판(board) 테이블 생성
CREATE TABLE board (
                       id INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
                       regDate DATETIME NOT NULL,
                       updateDate DATETIME NOT NULL,
                       `code` CHAR(50) NOT NULL UNIQUE COMMENT 'notice(공지사항) free(자유) QnA(질의응답) ...',
                       `name` CHAR(20) NOT NULL UNIQUE COMMENT '게시판 이름',
                       delStatus TINYINT(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '삭제 여부 (0=삭제 전, 1=삭제 후)',
                       delDate DATETIME COMMENT '삭제 날짜'
);

## 게시판(board) 테스트 데이터 생성
INSERT INTO board
SET regDate = NOW(),
updateDate = NOW(),
`code` = 'NOTICE',
`name` = '공지사항';

INSERT INTO board
SET regDate = NOW(),
updateDate = NOW(),
`code` = 'FREE',
`name` = '자유';

INSERT INTO board
SET regDate = NOW(),
updateDate = NOW(),
`code` = 'QnA',
`name` = '질의응답';

ALTER TABLE article ADD COLUMN boardId INT(10) UNSIGNED NOT NULL AFTER `memberId`;

UPDATE article
SET boardId = 1
WHERE id IN (1,2);

UPDATE article
SET boardId = 2
WHERE id = 3;

UPDATE article
SET boardId = 3
WHERE id = 4;

ALTER TABLE article ADD COLUMN hitCount INT(10) UNSIGNED NOT NULL DEFAULT 0 AFTER `body`;

# reactionPoint 테이블 생성
CREATE TABLE reactionPoint(
                              id INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
                              regDate DATETIME NOT NULL,
                              updateDate DATETIME NOT NULL,
                              memberId INT(10) UNSIGNED NOT NULL,
                              relTypeCode CHAR(50) NOT NULL COMMENT '관련 데이터 타입 코드',
                              relId INT(10) NOT NULL COMMENT '관련 데이터 번호',
                              `point` INT(10) NOT NULL
);

# reactionPoint 테스트 데이터 생성
# 1번 회원이 1번 글에 싫어요
INSERT INTO reactionPoint
SET regDate = NOW(),
updateDate = NOW(),
memberId = 1,
relTypeCode = 'article',
relId = 1,
`point` = -1;

# 1번 회원이 2번 글에 좋아요
INSERT INTO reactionPoint
SET regDate = NOW(),
updateDate = NOW(),
memberId = 1,
relTypeCode = 'article',
relId = 2,
`point` = 1;

# 2번 회원이 1번 글에 싫어요
INSERT INTO reactionPoint
SET regDate = NOW(),
updateDate = NOW(),
memberId = 2,
relTypeCode = 'article',
relId = 1,
`point` = -1;

# 2번 회원이 2번 글에 싫어요
INSERT INTO reactionPoint
SET regDate = NOW(),
updateDate = NOW(),
memberId = 2,
relTypeCode = 'article',
relId = 2,
`point` = -1;

# 3번 회원이 1번 글에 좋아요
INSERT INTO reactionPoint
SET regDate = NOW(),
updateDate = NOW(),
memberId = 3,
relTypeCode = 'article',
relId = 1,
`point` = 1;

# article 테이블에 reactionPoint(좋아요) 관련 컬럼 추가
ALTER TABLE article ADD COLUMN goodReactionPoint INT(10) UNSIGNED NOT NULL DEFAULT 0;
ALTER TABLE article ADD COLUMN badReactionPoint INT(10) UNSIGNED NOT NULL DEFAULT 0;

# update join -> 기존 게시글의 good bad RP 값을 RP 테이블에서 추출해서 article table에 채운다
UPDATE article AS A
    INNER JOIN (
    SELECT RP.relTypeCode, Rp.relId,
    SUM(IF(RP.point > 0,RP.point,0)) AS goodReactionPoint,
    SUM(IF(RP.point < 0,RP.point * -1,0)) AS badReactionPoint
    FROM reactionPoint AS RP
    GROUP BY RP.relTypeCode,Rp.relId
    ) AS RP_SUM
ON A.id = RP_SUM.relId
    SET A.goodReactionPoint = RP_SUM.goodReactionPoint,
        A.badReactionPoint = RP_SUM.badReactionPoint;

# reply 테이블 생성
CREATE TABLE reply (
                       id INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
                       regDate DATETIME NOT NULL,
                       updateDate DATETIME NOT NULL,
                       memberId INT(10) UNSIGNED NOT NULL,
                       relTypeCode CHAR(50) NOT NULL COMMENT '관련 데이터 타입 코드',
                       relId INT(10) NOT NULL COMMENT '관련 데이터 번호',
                       `body`TEXT NOT NULL
);

# 2번 회원이 1번 글에 댓글 작성
INSERT INTO reply
SET regDate = NOW(),
updateDate = NOW(),
memberId = 2,
relTypeCode = 'article',
relId = 1,
`body` = '댓글 1';

# 2번 회원이 1번 글에 댓글 작성
INSERT INTO reply
SET regDate = NOW(),
updateDate = NOW(),
memberId = 2,
relTypeCode = 'article',
relId = 1,
`body` = '댓글 2';

# 3번 회원이 1번 글에 댓글 작성
INSERT INTO reply
SET regDate = NOW(),
updateDate = NOW(),
memberId = 3,
relTypeCode = 'article',
relId = 1,
`body` = '댓글 3';

# 3번 회원이 1번 글에 댓글 작성
INSERT INTO reply
SET regDate = NOW(),
updateDate = NOW(),
memberId = 2,
relTypeCode = 'article',
relId = 2,
`body` = '댓글 4';

# reply 테이블에 좋아요 관련 컬럼 추가
ALTER TABLE reply ADD COLUMN goodReactionPoint INT(10) UNSIGNED NOT NULL DEFAULT 0;
ALTER TABLE reply ADD COLUMN badReactionPoint INT(10) UNSIGNED NOT NULL DEFAULT 0;

# reactionPoint 테스트 데이터 생성
# 1번 회원이 1번 댓글에 싫어요
INSERT INTO reactionPoint
SET regDate = NOW(),
updateDate = NOW(),
memberId = 1,
relTypeCode = 'reply',
relId = 1,
`point` = -1;

# 1번 회원이 2번 댓글에 좋아요
INSERT INTO reactionPoint
SET regDate = NOW(),
updateDate = NOW(),
memberId = 1,
relTypeCode = 'reply',
relId = 2,
`point` = 1;

# 2번 회원이 1번 댓글에 싫어요
INSERT INTO reactionPoint
SET regDate = NOW(),
updateDate = NOW(),
memberId = 2,
relTypeCode = 'reply',
relId = 1,
`point` = -1;

# 2번 회원이 2번 댓글에 싫어요
INSERT INTO reactionPoint
SET regDate = NOW(),
updateDate = NOW(),
memberId = 2,
relTypeCode = 'reply',
relId = 2,
`point` = -1;

# 3번 회원이 1번 댓글에 좋아요
INSERT INTO reactionPoint
SET regDate = NOW(),
updateDate = NOW(),
memberId = 3,
relTypeCode = 'reply',
relId = 1,
`point` = 1;

# update join -> 기존 게시물의 good,bad RP 값을 RP 테이블에서 가져온 데이터로 채운다
UPDATE reply AS R
    INNER JOIN (
    SELECT RP.relTypeCode,RP.relId,
    SUM(IF(RP.point > 0, RP.point, 0)) AS goodReactionPoint,
    SUM(IF(RP.point < 0, RP.point * -1, 0)) AS badReactionPoint
    FROM reactionPoint AS RP
    GROUP BY RP.relTypeCode, RP.relId
    ) AS RP_SUM
ON R.id = RP_SUM.relId
    SET R.goodReactionPoint = RP_SUM.goodReactionPoint,
        R.badReactionPoint = RP_SUM.badReactionPoint;

# 파일 테이블 추가
CREATE TABLE genFile (
                         id INT(10) UNSIGNED NOT NULL AUTO_INCREMENT, # 번호
        regDate DATETIME DEFAULT NULL, # 작성날짜
                             updateDate DATETIME DEFAULT NULL, # 갱신날짜
                             delDate DATETIME DEFAULT NULL, # 삭제날짜
                             delStatus TINYINT(1) UNSIGNED NOT NULL DEFAULT 0, # 삭제상태(0:미삭제,1:삭제)
  relTypeCode CHAR(50) NOT NULL, # 관련 데이터 타입(article, member)
  relId INT(10) UNSIGNED NOT NULL, # 관련 데이터 번호
  originFileName VARCHAR(100) NOT NULL, # 업로드 당시의 파일이름
  fileExt CHAR(10) NOT NULL, # 확장자
                             typeCode CHAR(20) NOT NULL, # 종류코드 (common)
  type2Code CHAR(20) NOT NULL, # 종류2코드 (attatchment)
  fileSize INT(10) UNSIGNED NOT NULL, # 파일의 사이즈
  fileExtTypeCode CHAR(10) NOT NULL, # 파일규격코드(img, video)
  fileExtType2Code CHAR(10) NOT NULL, # 파일규격2코드(jpg, mp4)
  fileNo SMALLINT(2) UNSIGNED NOT NULL, # 파일번호 (1)
  fileDir CHAR(20) NOT NULL, # 파일이 저장되는 폴더명
  PRIMARY KEY (id),
                         KEY relId (relTypeCode,relId,typeCode,type2Code,fileNo)
);

# 기존의 회원 비번을 암호화
UPDATE `member`
SET loginPw = SHA2(loginPw,256);

#######(INIT 끝)

SELECT * FROM gameSchedule;