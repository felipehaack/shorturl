## Short URL API

### How to Setup

1. Download [Docker For Mac](https://docs.docker.com/docker-for-mac/) on your machine.
2. Install the Docker Application and make sure you have the latest version installed.
3. For others OS look at [Docker Install](https://docs.docker.com/compose/install/).


### How to Run

1. Open the terminal
2. Clone the project: ```git clone https://github.com/felipehaack/shorturl.git```
3. And go to the application folder ```cd shorturl```
4. Run the following command: ```docker-compose up api``` (**The first launch of the API takes considerably more time than the database** due to the fact that it has to download dependencies, even database was finished the API will continue downloading)
5. Once the docker application has been launched successfully, use the following curls:

- POST: ```curl -H "Content-Type: application/json" -X POST -d '{"url": "http://www.payu.com"}' http://localhost:9000/v1/shorturl```
- GET: ```curl -H "Content-Type: application/json" -X GET  http://localhost:9000/v1/shorturl/if```

### How to Test

1. Stop the short url instance if you have one running
2. Run the follow command: ```docker-compose up api-test```