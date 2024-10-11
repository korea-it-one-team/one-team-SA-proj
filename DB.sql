# DB 세팅
DROP DATABASE IF EXISTS `one-team-SA-proj`;
CREATE DATABASE `one-team-SA-proj`;
USE `one-team-SA-proj`;

# 게시글 테이블 생성
CREATE TABLE gifticons (
                           id INT AUTO_INCREMENT PRIMARY KEY,
                           `name` VARCHAR(255) NOT NULL,
                           points INT NOT NULL,
                           image_url VARCHAR(255),
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE users (
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       `password` VARCHAR(255) NOT NULL,
                       email VARCHAR(100) NOT NULL UNIQUE,
                       points INT DEFAULT 0,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE exchange_history (
                                  id INT AUTO_INCREMENT PRIMARY KEY,
                                  user_id INT NOT NULL,
                                  gifticon_id INT NOT NULL,
                                  exchange_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                  FOREIGN KEY (user_id) REFERENCES users(id),
                                  FOREIGN KEY (gifticon_id) REFERENCES gifticons(id)
);

CREATE TABLE point_transactions (
                                    id INT AUTO_INCREMENT PRIMARY KEY,
                                    user_id INT NOT NULL,
                                    points INT NOT NULL,
                                    transaction_type ENUM('ADD', 'SUBTRACT') NOT NULL, -- 포인트 추가 또는 차감
                                    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                    FOREIGN KEY (user_id) REFERENCES users(id)
);

INSERT INTO users (username, PASSWORD, email, points) VALUES
                                                          ('test1', 'test1', 'user1@example.com', 100),
                                                          ('test2', 'test2', 'user2@example.com', 150),
                                                          ('test3', 'test3', 'user3@example.com', 200);

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

SELECT * FROM gifticons;
