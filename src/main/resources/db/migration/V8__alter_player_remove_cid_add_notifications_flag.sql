alter table cata.player
    drop column cid;

alter table cata.player
    add enable_notifications boolean default false;

update cata.player
set player.enable_notifications= 1
where player.surname = 'маюн';