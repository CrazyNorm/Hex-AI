#!/bin/bash

usage(){
	echo "Usage: bash testAgents.sh [-g <no. of games>] <agent1> [<agent2>]"
	exit 1
}

if [ $# -lt 1 ]; then
  usage
fi

rm logs/log*
rm logs/out/*

# associative array of agents
declare -rA agents=(
 ["MCTS"]="java -cp agents mcts.MCTSAgent"
 ["RootMCTS"]="java -cp agents mcts.RootMCTSAgent"
 ["JavaNaive"]="java -cp agents/DefaultAgents NaiveAgent"
 ["PythonNaive"]="python3 agents/DefaultAgents/NaiveAgent.py"
 ["alice"]=$(cat agents/TestAgents/alice/cmd.txt)
 ["bob"]="./agents/TestAgents/bob/bobagent"
 ["jimmie"]=$(cat agents/TestAgents/jimmie/cmd.txt)
 ["joni"]=$(cat agents/TestAgents/joni/cmd.txt)
 ["rita"]=$(cat agents/TestAgents/rita/cmd.txt)
 # Add new agents here ([name]=command)
)


# get no. of games
declare games=10

while getopts ':g:' OPTION; do
  case "$OPTION" in
    g)
      if ! [ "${OPTARG}" -eq "${OPTARG}" ] 2>/dev/null; then
          echo "no. of game must be a number"
          usage
      fi
      games="${OPTARG}"
      ;;
    ?)
      usage
  esac
done

shift $((OPTIND-1))

# get agents
declare agent1 agent2

if [ -n "${agents[$1]}" ]; then
    agent1="$1;${agents[$1]}"
else
    echo "$1 is not a valid agent"
    exit
fi

if [ -z "$2" ]; then
  agent2="JavaNaive;${agents["JavaNaive"]}"
elif [ -n "${agents[$2]}" ]; then
    agent2="$2;${agents[$2]}"
else
  echo "$2 is not a valid agent"
  exit
fi


# play games (switch sides halfway)
declare agent1Wins=0
declare agent2Wins=0

for i in $(seq 0 $((games / 2 - 1))); do
  echo "Game $((i+1)) starting"
  if [ "$i" -eq 0 ]; then
    out="logs/out/out"
  else
    out="logs/out/out$i"
  fi
  python3 Hex.py "a=$agent1" "a=$agent2" -l -k 1>"$out" 2>&1

  # count wins
  result1=$(sed '2!d' "$out")
  result2=$(sed '3!d' "$out")
  if [[ $result1 == True* ]]; then
    ((agent1Wins+=1))
  elif [[ $result2 == True* ]]; then
    ((agent2Wins+=1))
  fi
done

for i in $(seq $((games / 2)) $((games - 1))); do
  echo "Game $((i+1)) starting"
  out="logs/out/out$i"
  python3 Hex.py "a=$agent1" "a=$agent2" -l -s -k 1>"$out" 2>&1

  # count wins
  result1=$(sed '2!d' "$out")
  result2=$(sed '3!d' "$out")
  if [[ $result1 == True* ]]; then
    ((agent2Wins+=1))
  elif [[ $result2 == True* ]]; then
    ((agent1Wins+=1))
  fi
done

# output win statistics
echo
echo "Agent 1 wins: $agent1Wins"
echo "Agent 2 wins: $agent2Wins"