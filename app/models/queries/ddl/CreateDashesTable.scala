package models.queries.ddl

import jdub.async.Statement

case object CreateDashesTable extends Statement {
  override val sql = """
    create table dashes (
      id uuid primary key,
      clientname character varying(256),
      clientemail character varying(256),
      clientphone character varying(256),
      clientzip character varying(256),
      clientcity character varying(256),
      clientstate character varying(256),
      driverphone character varying(256),
      drivername character varying(256),
      drivercompany character varying(256),
      pickuplocation character varying(256),
      attendantnamecomment character varying(256), 
      chargedamount character varying(256),
      chargecomment character varying(256),
      other character varying(256),
      created timestamp not null
    ) with (oids=false);

    create index dashes_clientname_idx on dashes using btree (clientname collate pg_catalog."default");
    create index dashes_clientstate_idx on dashes using btree (clientstate collate pg_catalog."default");
    create index dashes_clientzip_idx on dashes using btree (clientzip collate pg_catalog."default");
    create index dashes_clientphone_idx on dashes using btree (clientphone collate pg_catalog."default");
    create index dashes_drivername_idx on dashes using btree (drivername collate pg_catalog."default");
    create index dashes_driverphone_idx on dashes using btree (driverphone collate pg_catalog."default");
    create index dashes_drivercompany_idx on dashes using btree (drivercompany collate pg_catalog."default");
    create index dashes_pickuplocation_idx on dashes using btree (pickuplocation collate pg_catalog."default");
    create index dashes_chargecomment_idx on dashes using btree (chargecomment collate pg_catalog."default");
    create index dashes_chargedamount_idx on dashes using btree (chargedamount collate pg_catalog."default");
    create index dashes_created_idx on dashes using btree (created);
  """
}
