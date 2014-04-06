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
  user_role int NOT NULL REFERENCES roles,
  user_last_login timestamp DEFAULT NOW()
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

INSERT INTO dstates VALUES(1, 'upcoming');
INSERT INTO dstates VALUES(2, 'drafting');
INSERT INTO dstates VALUES(3, 'tournament');
INSERT INTO dstates VALUES(4, 'finished');

CREATE TABLE drafts (
  draft_hash text PRIMARY KEY,
  draft_start timestamp NOT NULL,
  draft_set1 text NOT NULL REFERENCES mtgsets,
  draft_set2 text NOT NULL REFERENCES mtgsets,
  draft_set3 text NOT NULL REFERENCES mtgsets,
  draft_state int NOT NULL REFERENCES dstates,
  draft_venue text,
  draft_food text,
  draft_fee numeric(10, 2),
  draft_details text
);;

CREATE TABLE invitations (
  draft_hash text REFERENCES drafts ON DELETE CASCADE,
  user_id int REFERENCES users,
  PRIMARY KEY(draft_hash, user_id)
);;

CREATE TABLE matches (
  draft_hash text REFERENCES drafts ON DELETE CASCADE,
  user1 int NOT NULL REFERENCES users,
  user2 int NOT NULL REFERENCES users,
  match_round int NOT NULL,
  PRIMARY KEY(draft_hash, user1, user2, match_round)
);;

CREATE TABLE participants (
  draft_hash text REFERENCES drafts,
  user_id int REFERENCES users,
  part_seat int,
  part_joined timestamp NOT NULL,
  part_paid boolean NOT NULL DEFAULT false,
  PRIMARY KEY(draft_hash, user_id)
);;

CREATE VIEW participation AS
  SELECT
    drafts.draft_hash,
    drafts.draft_start,
    COALESCE(count(participants.user_id), 0) AS participants
  FROM drafts LEFT JOIN participants
    ON (drafts.draft_hash = participants.draft_hash)
  GROUP BY
    drafts.draft_hash,
    drafts.draft_start;;

# --- !Downs
DROP VIEW IF EXISTS participation;;

DROP TABLE IF EXISTS participants;;

DROP TABLE IF EXISTS matches;;

DROP TABLE IF EXISTS invitations;;

DROP TABLE IF EXISTS drafts;;

DROP TABLE IF EXISTS dstates;;

DROP TABLE IF EXISTS mtgsets;;

DROP TABLE IF EXISTS users;;

DROP TABLE IF EXISTS roles;;

DROP EXTENSION IF EXISTS pgcrypto;;