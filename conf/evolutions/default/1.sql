# --- !Ups
CREATE EXTENSION IF NOT EXISTS pgcrypto;;

CREATE TABLE roles (
  role_id serial PRIMARY KEY,
  role_name text NOT NULL UNIQUE
);;

INSERT INTO roles VALUES(DEFAULT, 'admin');
INSERT INTO roles VALUES(DEFAULT, 'joe');

CREATE TABLE users (
  user_id bigserial PRIMARY KEY,
  user_handle text NOT NULL UNIQUE,
  user_email text NOT NULL UNIQUE,
  user_password text NOT NULL,
  user_role int NOT NULL REFERENCES roles
);;

INSERT INTO users VALUES(
  DEFAULT, 'admin', 'admin@admin.blah',
  crypt('yesthisistheadminspassword!', gen_salt('bf')),
  (SELECT role_id FROM roles WHERE role_name = 'admin' LIMIT 1)
);;

CREATE TABLE mtgsets (
  mtgset_id text PRIMARY KEY,
  mtgset_name text NOT NULL
);;

CREATE TABLE dstates (
  dstate_number int PRIMARY KEY,
  dstate_name text NOT NULL UNIQUE
);;

INSERT INTO dstates VALUES(1, 'preparation');
INSERT INTO dstates VALUES(2, 'drafting');
INSERT INTO dstates VALUES(3, 'matches');
INSERT INTO dstates VALUES(4, 'finished');

CREATE TABLE drafts (
  draft_id bigserial PRIMARY KEY,
  draft_hash text NOT NULL UNIQUE,
  draft_start timestamp NOT NULL,
  draft_venue text NOT NULL,
  draft_food text NOT NULL,
  draft_state int NOT NULL REFERENCES dstates,
  draft_fee numeric(2) NOT NULL,
  draft_set1 text REFERENCES mtgsets,
  draft_set2 text REFERENCES mtgsets,
  draft_set3 text REFERENCES mtgsets
);;

CREATE TABLE participants (
  draft_id int REFERENCES drafts,
  user_id int REFERENCES users,
  joined_on timestamp NOT NULL,
  paid boolean NOT NULL DEFAULT false,
  PRIMARY KEY(draft_id, user_id)
);;

CREATE VIEW participation AS
  SELECT
    drafts.draft_id,
    drafts.draft_hash,
    drafts.draft_start,
    COALESCE(count(participants.user_id), 0) AS participants
  FROM drafts LEFT JOIN participants
    ON (drafts.draft_id = participants.draft_id)
  GROUP BY
    drafts.draft_id,
    drafts.draft_hash,
    drafts.draft_start;;

# --- !Downs
DROP VIEW IF EXISTS participation;;

DROP TABLE IF EXISTS participants;;

DROP TABLE IF EXISTS drafts;;

DROP TABLE IF EXISTS dstates;;

DROP TABLE IF EXISTS mtgsets;;

DROP TABLE IF EXISTS users;;

DROP TABLE IF EXISTS roles;;

DROP EXTENSION IF EXISTS pgcrypto;;