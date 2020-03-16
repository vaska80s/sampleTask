FROM rabbitmq:3-management

RUN apt-get update \ 
    && apt-get -y install openjdk-8-jdk \
    && apt-get -y install git \
    maven \
    wget \
    && apt-get -y remove openjdk-7-jre \
    openjdk-7-jre-headless \
    && apt-get clean

RUN git clone https://github.com/vaska80s/sampleTask.git

WORKDIR /sampleTask/uploaderBot

RUN mvn package && cp target/ubot-bin.tar.gz ../../ 

WORKDIR /

RUN tar -zxvf ubot-bin.tar.gz

WORKDIR /ubot

RUN mkdir img \
    && mkdir tmp \
    && wget https://www.free-pictures-photos.com/stock-photos-2/pigs-4yo2.jpg -O img/schwein.jpg \
    && wget https://www.free-pictures-photos.com/animals/Cat-pictures/cat_pictures-38.jpg -O img/cat1.jpg \
    && wget https://www.free-pictures-photos.com/animals/Cat-pictures/cat_pictures-2.jpg -O img/cat2.jpg \
    && wget https://www.free-pictures-photos.com/zoo/penguins-zoo-3xh.jpg -O img/pinguine.jpg 