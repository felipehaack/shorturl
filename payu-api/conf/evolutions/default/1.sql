# -- Url schema
# --- !Ups
CREATE SEQUENCE urls_id_seq START 1000;

CREATE TABLE urls (
  id            INT8 DEFAULT nextval('urls_id_seq')     NOT NULL,
  url           VARCHAR(1024)                           NOT NULL,
  url_shortened VARCHAR(64),
  url_hashed    VARCHAR(255)                            NOT NULL,
  created       TIMESTAMP DEFAULT now()                 NOT NULL,
  deleted       TIMESTAMP
);

CREATE INDEX urls_url_hashed_idx
  ON urls (url_shortened);

# -- !Downs
DROP INDEX IF EXISTS urls_url_hashed_idx;
DROP TABLE IF EXISTS urls CASCADE;
DROP SEQUENCE urls_id_seq;