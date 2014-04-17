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
INSERT INTO dstates VALUES(5, 'aborted');

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
  part_joined timestamp NOT NULL DEFAULT NOW(),
  part_paid boolean NOT NULL DEFAULT false,
  part_seat int,
  PRIMARY KEY(draft_hash, user_id)
);;

# --- !Downs
DROP TABLE IF EXISTS participants;;

DROP TABLE IF EXISTS matches;;

DROP TABLE IF EXISTS invitations;;

DROP TABLE IF EXISTS drafts;;

DROP TABLE IF EXISTS dstates;;

DROP TABLE IF EXISTS mtgsets;;

DROP TABLE IF EXISTS users;;

DROP TABLE IF EXISTS roles;;

DROP EXTENSION IF EXISTS pgcrypto;;