# Introduction

This repo enables configurable rabbit queues. With adding spesifying new queues in `application.yml` will be enough.

# How it works?

If you want to add new queue add following properties to `application.yml`: 

    rabbit:
      brokers:
        - exchange: event.log
          routingKey: route.audit.log
          queue: queue.audit.log
          deadLetter: true

`brokers` - defines new queues;

`exchange` - name of exchange;

`routingKey` - routing key between queue and exchange;

 `queue` - name of actual queue;
 
 `deadLetter` - this enables dead letter queues with exact name of queue but addition with `.dlq`
 
 Use and give feedback ;)
