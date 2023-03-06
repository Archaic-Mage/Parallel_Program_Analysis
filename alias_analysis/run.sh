#!/bin/bash

till=$(ls -1f ./inputs/*.java | wc -l)
RED="\e[31m"
GREEN="\e[32m"
YELLOW="\e[33m"
END_COLOR="\e[0m"
correct=0
ncorrect=0

for (( f=1; f<=$till; f++ ))
do
  echo "Test case: $f"
  /home/mage/.jdks/corretto-1.8.0_362/bin/java -cp /home/mage/Documents/Semester_6/cs6235/practise/soot4.3.0/soot4.3.0/soot-4.3.0-jar-with-dependencies.jar:out/production/alias_analysis -Dtest.file="queries/Q$f.txt" submit_a1.A1 -cp inputs P$f > temp.txt

  grep -v "Soot" temp.txt > temp1.txt
  a=1
  diff <(sed -e '$a\' temp1.txt) <(sed -e '$a\' ./results/A$f.txt) && a=0 || a=1

  if [ $a == 0 ]; then
    echo -e "${GREEN} OK: correct ${END_COLOR}"
    correct=$(($correct+1))  
  else 
    echo -e "${RED} NOT_OK: not correct ${END_COLOR}"
    ncorrect=$(($ncorrect+1))
  fi
done

if [ $till == $correct ]; then
  echo -e "$YELLOW ALL OK $END_COLOR"
else 
  echo -e "$RED CHECK $END_COLOR"
fi

rm -rf temp.txt temp1.txt
