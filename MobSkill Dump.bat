@echo off
@title MobSkill Dumper
set CLASSPATH=.;dist\*
java -server -Dnet.sf.odinms.wzpath=wz tools.wztosql.DumpMobSkills
pause