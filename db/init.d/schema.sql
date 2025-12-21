create table devtalk.user_account
(
    user_id            bigint auto_increment
        primary key,
    created_at         datetime(6)  not null,
    deleted_at         datetime(6)  null,
    updated_at         datetime(6)  not null,
    email              varchar(255) not null,
    is_deleted         bit          not null,
    nickname           varchar(10)  not null,
    password           varchar(100) not null,
    profile_image_name varchar(255) null,
    constraint UK43ya4hrd194pfo2k5a40bfali
        unique (profile_image_name),
    constraint UKd5s80v1ic05f862oo5hs0rw8q
        unique (nickname),
    constraint UKhl02wv5hym99ys465woijmfib
        unique (email)
);

create table devtalk.post
(
    post_id           bigint auto_increment
        primary key,
    created_at        datetime(6)  not null,
    deleted_at        datetime(6)  null,
    updated_at        datetime(6)  not null,
    comment_count     int          not null,
    content           longtext     not null,
    image_name        varchar(255) null,
    is_deleted        bit          not null,
    like_count        int          not null,
    origin_image_name varchar(255) null,
    title             varchar(26)  not null,
    view_count        int          not null,
    user_id           bigint       null,
    constraint UK2lr23iqksqs5xnjm3mcklweor
        unique (image_name),
    constraint FK7ossp45hgowuwqeytd1c92v1s
        foreign key (user_id) references devtalk.user_account (user_id)
);

create table devtalk.comment
(
    comment_id bigint auto_increment
        primary key,
    created_at datetime(6) not null,
    deleted_at datetime(6) null,
    updated_at datetime(6) not null,
    content    text        not null,
    is_deleted bit         not null,
    post_id    bigint      null,
    user_id    bigint      null,
    constraint FK3y3uou7na66pfn512byon549s
        foreign key (user_id) references devtalk.user_account (user_id),
    constraint FKs1slvnkuemjsq2kj4h3vhx7i1
        foreign key (post_id) references devtalk.post (post_id)
);

create index idx_post_is_deleted_created_at
    on devtalk.post (is_deleted, created_at);

create table devtalk.post_like
(
    post_like_id bigint auto_increment
        primary key,
    is_liked     bit         not null,
    updated_at   datetime(6) not null,
    post_id      bigint      null,
    user_id      bigint      null,
    constraint uk_user_post
        unique (user_id, post_id),
    constraint FKj7iy0k7n3d0vkh8o7ibjna884
        foreign key (post_id) references devtalk.post (post_id),
    constraint FKm07ldu4156avl1luxs0gbhjh0
        foreign key (user_id) references devtalk.user_account (user_id)
);

