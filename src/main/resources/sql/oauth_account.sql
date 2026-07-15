create table oauth_account (
    provider         varchar(20) not null,
    provider_subject varchar(64) character set ascii not null,
    user_id          bigint      not null,
    created_at       datetime(6) not null,
    updated_at       datetime(6) not null,
    last_login_at    datetime(6) not null,
    primary key (provider, provider_subject)
);
