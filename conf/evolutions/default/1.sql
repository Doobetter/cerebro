# --- !Ups
CREATE TABLE rest_requests (
    id INTEGER PRIMARY KEY,
    username TEXT,
    path TEXT NOT NULL,
    method TEXT NOT NULL,
    body TEXT NOT NULL,
    md5 TEXT NOT NULL,
    created_at INTEGER
);

CREATE INDEX username_idx ON rest_requests(username);

CREATE UNIQUE INDEX md5_idx ON rest_requests(md5);

CREATE TABLE sql_requests (
      id INTEGER PRIMARY KEY,
      username TEXT,
      hostname TEXT,
      body TEXT NOT NULL,
      md5 TEXT NOT NULL,
      created_at INTEGER
);

CREATE INDEX username_sql_idx ON sql_requests(username);
CREATE INDEX hostname_sql_idx ON sql_requests(hostname);
CREATE UNIQUE INDEX md5_sql_idx ON sql_requests(md5);


# --- !Downs
DROP TABLE rest_requests;
DROP TABLE sql_requests;



