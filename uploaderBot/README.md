# Uploader Bot

This command line application resizes and uploads given images to remote cloud storage

## Required software

To compile and run this script the following software is required: 
[JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html), 
[Maven](http://maven.apache.org/download.cgi) and [RabbitMQ](https://www.rabbitmq.com/download.html)
All software does not required any special configuration steps. 

## Compilation

To build script, change directory to sampleTask\uploaderBot and run ```mvn package``` command: 
```sh
$ git clone https://github.com/vaska80s/sampleTask.git
$ cd sampleTask\uploaderBot
$ mvn package
```
The result of compilation is three different types of archive - ubot-bin.tar.bz2, ubot-bin.zip and ubot-bin.tar.gz. These files can be found in ```target``` directory:

```sh
$ ls target
archive-tmp/  generated-sources/       maven-archiver/  surefire-reports/  ubot-bin.tar.bz2  ubot-bin.zip
classes/      generated-test-sources/  maven-status/    test-classes/      ubot-bin.tar.gz   ubot.jar
```

## Configuration file

Before you can run the script you have to specify at least Dropbox authorization token and resizer temp dir, where resized photos will be stored. To do it, copy appropriate type of archive, uncompress it and change uploader.dropboxtoken and resizer.tmpdir values in ```uploadbot.properties``` file:

```sh
$ cp target/ubot-bin.tar.bz2 ../../ 
$ cd ../../
$ tar -jxvf ubot-bin.tar.bz2
$ cd ubot
$ mcedit uploadbot.properties
```

Also, in configuration file, you can specify RabbitMQ host (queue.rabbitmq.host), username (rabbit.user) and password (rabbit.password).

## Running script

To run the script just execute shell-file:

```sh
$ ./bot
Uploader Bot
Usage:
        command [arguments]
Available commands:
        schedule        Add filenames to resize queue
        resize          Resize next images from the queue
        status          Output current status in format %queue%:%number_of_images%
        upload          Upload next images to remote storage
        retry           Reschedule failed images

```

## Docker

You can create and run a Docker container from supplied Dockerfile, this option requires Internet connection. To do this, run following commands in ```sampleTask\uploaderBot``` directory:

```sh
$ cd sampleTask\uploaderBot
$ docker build -t ubot .
$ docker run -d -h ubot --name ubot -p 8090:15672 ubot
$ docker exec -ti ubot /bin/bash
```

To connect with rabbitmq managment page go to url http://your-docker-ip:8090/ and login as "guest" with password "guest". Inside container you will find /ubot directory with ready to run script and sample images in /ubot/img.
