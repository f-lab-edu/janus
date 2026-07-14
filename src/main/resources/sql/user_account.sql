create table user_account (
    id           bigint       not null auto_increment,
    nickname     varchar(40),
    status       varchar(20)  not null,
    blocked_at   datetime(6),
    created_at   datetime(6)  not null,
    updated_at   datetime(6)  not null,
    withdrawn_at datetime(6),
    primary key (id)
);
