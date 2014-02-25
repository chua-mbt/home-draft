# --- !Ups
CREATE EXTENSION IF NOT EXISTS pgcrypto;;

CREATE TABLE roles (
  role_id serial PRIMARY KEY,
  role_name text NOT NULL UNIQUE
);;

INSERT INTO roles VALUES(DEFAULT, 'admin');
INSERT INTO roles VALUES(DEFAULT, 'joe');

CREATE TABLE users (
  user_id serial PRIMARY KEY,
  user_handle text NOT NULL UNIQUE,
  user_email text NOT NULL UNIQUE,
  user_password text NOT NULL,
  user_role int NOT NULL REFERENCES roles
);;

INSERT INTO users VALUES(
  DEFAULT, 'admin', 'admin@admin.blah',
  crypt('yesthisistheadminspassword!', gen_salt('bf')),
  (SELECT role_id FROM roles WHERE role_name = 'admin' LIMIT 1)
);

CREATE TABLE mtgsets (
  mtgset_id text PRIMARY KEY,
  mtgset_name text NOT NULL
);;

CREATE TABLE drafts (
  draft_id serial PRIMARY KEY,
  draft_hash text NOT NULL UNIQUE,
  draft_start timestamp NOT NULL,
  draft_venue text NOT NULL,
  draft_food text NOT NULL,
  draft_state int NOT NULL,
  draft_set1 text REFERENCES mtgsets,
  draft_set2 text REFERENCES mtgsets,
  draft_set3 text REFERENCES mtgsets
);;

CREATE TABLE participants (
  draft_id int REFERENCES drafts,
  user_id int REFERENCES users,
  joined_on timestamp NOT NULL,
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

DROP TABLE IF EXISTS mtgsets;;

DROP TABLE IF EXISTS users;;

DROP TABLE IF EXISTS roles;;

DROP EXTENSION IF EXISTS pgcrypto;;