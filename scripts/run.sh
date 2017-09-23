#!/bin/bash
sudo systemctl start docker
sudo systemctl enable docker
sudo docker build -t micro_rsvp .
sudo docker run -d --restart always --log-opt max-size=100m -p 80:80 micro_rsvp
sudo docker ps
