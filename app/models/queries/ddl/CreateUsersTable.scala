package models.queries.ddl

import jdub.async.Statement

case object CreateUsersTable extends Statement {
  override val sql = """
    create table users (
      id uuid primary key,
      username character varying(256),
      profiles text[] not null,
      roles character varying(64)[] not null,
      full_name character varying(512),
      email character varying(256),
      phone character varying(256),
      street character varying(256),
      city character varying(256),
      state character varying(256),
      zip character varying(256),
      hasstripe character varying(256), 
      preferences character varying(256),
      image character varying(256),
      created timestamp not null
    ) with (oids=false);

    create index users_profiles_idx on users using gin (profiles);
    create unique index users_username_idx on users using btree (username collate pg_catalog."default");
    create unique index users_full_name_idx on users using btree (username collate pg_catalog."default");
    create unique index users_email_idx on users using btree (username collate pg_catalog."default");
    create index users_roles_idx on users using gin (roles);
  """
}
