#!/bin/bash

declare -a nodes=(20 40 70 100)
declare -a speeds=(5 10 15 20)
declare -a packet_rates=(100 200 300 400)

for node in "${nodes[@]}"
do
	speed=20
	packet_rate=4	
	echo "Running simulation with $node nodes, $speed m/s speed, $packet_rate packets/s"
	./ns3 run "2005104_task1 --CSVfileName=scratch/demo/2005104_aodv_nodes.csv --nWifis=$node --nodeSpeed=$speed --packetsPerSecond=$packet_rate"
done

for speed in "${speeds[@]}"
do
	node=50
	packet_rate=4
	echo "Running simulation with $node nodes, $speed m/s speed, $packet_rate packets/s"
    ./ns3 run "2005104_task1 --CSVfileName=scratch/demo/2005104_aodv_speed.csv --nWifis=$node --nodeSpeed=$speed --packetsPerSecond=$packet_rate"
done


for packet_rate in "${packet_rates[@]}"
do
	node=50
	speed=20
	echo "Running simulation with 40 nodes, 10 m/s speed, $packet_rate packets/s"
    ./ns3 run "2005104_task1 --CSVfileName=scratch/demo/2005104_aodv_packetRate.csv --nWifis=$node --nodeSpeed=$speed --packetsPerSecond=$packet_rate"
done
