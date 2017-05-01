# --Seed
# --- !Ups
INSERT INTO urls (id, url, url_shortened, url_hashed, created, deleted) VALUES
  (1, 'http://www.payu.com/', '1', 'c4ca4238a0b923820dcc509a6f75849b', now(), NULL),
  (2, 'http://www.payu.com/', '2', 'c81e728d9d4c2f636f067f89cc14862c', now(), now());

# --- !Downs
TRUNCATE TABLE urls RESTART IDENTITY CASCADE;