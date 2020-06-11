use cata;
create table if not exists player
(
    id      bigint primary key auto_increment,
    surname varchar(20) not null
);
ALTER TABLE player CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;


create index player_id_idx on player (id);
create index player_surname_idx on player (surname);

create table if not exists season
(
    id   bigint primary key auto_increment,
    name varchar(20) not null
);
ALTER TABLE season CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

create index season_id_idx on season (id);


create table if not exists round
(
    id         bigint primary key auto_increment,
    winner1_id bigint    not null,
    winner2_id bigint    not null,
    loser1_id  bigint    not null,
    loser2_id  bigint    not null,
    isShutout  boolean default false,
    season_id  bigint    not null,
    created    timestamp not null,
    foreign key fk_win1 (winner1_id) references player (id),
    foreign key fk_win2 (winner2_id) references player (id),
    foreign key fk_l1 (loser1_id) references player (id),
    foreign key fk_l2 (loser2_id) references player (id),
    foreign key fk_season (season_id) references season (id)
);
ALTER TABLE round CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

create index round_id_idx on round (id);

