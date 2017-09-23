# Base OS
FROM ubuntu:16.04

# Set our working directory
WORKDIR /

# Update the repositories and then install java
RUN apt-get update
RUN apt-get install default-jre -y

# Copy the application from its folder to our image
# Assumes docker build is run from the same folder as the JAR
ADD ./micro_rsvp.jar /micro_rsvp.jar
ADD ./resources /resources

# Run the app when the container is executed.
CMD ["java","-jar","./micro_rsvp.jar","--port","80","--nrepl-port","3001"]
