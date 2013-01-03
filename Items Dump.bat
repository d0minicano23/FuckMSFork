@echo off
@title Items Dumper
set CLASSPATH=.;dist\*
java -server -Dnet.sf.odinms.wzpath=wz tools.wztosql.DumpItems
pause