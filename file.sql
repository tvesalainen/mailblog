delete from Settings where x is null;

insert into Settings (key, ShowCount, ConfirmEmail, PicMaxHeight, PicMaxWidth, FixPic, Language, Template) values (
key(Settings('base')),
5,
Boolean('true'),
500,
500,
Boolean('true'),
'fi',
'<div class="subject">%s</div><div class="subject">%s</div><div class="subject">%s</div>'
)
;

insert into Settings (key, Email, Nickname, ConfirmEmail, Template) values (
key(Settings('base')/Settings('Jonna')),
Email('johanna.vesalainen@gmail.com'),
'Jonna',
Boolean('true'),
Text('
<div class="subject">%1$s</div>
<div class="date">%2$te.%2$tm.%2$tY</div>
<div class="sender">%3$s</div>
<div class="body">%4$s</div>
')
)
;
insert into Settings (key, Email, Nickname, ConfirmEmail, Template) values (
key(Settings('base')/Settings('timo')),
Email('timo.vesalainen@gmail.com'),
'timo',
Boolean('true'),
Text('
<div class="subject">%1$s</div>
<div class="date">%2$te.%2$tm.%2$tY</div>
<div class="sender">%3$s</div>
<div class="body">%4$s</div>
')
)
;