alter table cata.player add tid varchar(20) default null;
alter table cata.player add admin boolean default false;

update cata.player
    set player.tid='530809403',player.admin = 1
    where player.surname = 'маюн';