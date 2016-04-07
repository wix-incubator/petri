---
title: Build Petri Server From Scratch (Uses MySql)
keywords: questions, quickstart, installation, server, integration, database
last_updated: April 6, 2016
sidebar: configure_petri_server
permalink: /configure_petri_server/
---

![Setting Up Petri Server](https://raw.githubusercontent.com/wix/petri/gh-pages/images/quickstart_architecture1.png)

## Setup Petri's MySql Database 

Run a script creating the needed schema

```
CREATE TABLE experiments (id INT AUTO_INCREMENT, experiment MEDIUMTEXT, last_update_date BIGINT, orig_id INT, start_date BIGINT DEFAULT 0, end_date BIGINT DEFAULT 4102444800000, PRIMARY KEY(id, last_update_date))
CREATE TABLE specs (id INT PRIMARY KEY AUTO_INCREMENT, fqn VARCHAR (255) NOT NULL, spec MEDIUMTEXT, UNIQUE KEY (fqn))
CREATE TABLE metricsReport (server_name VARCHAR (255) NOT NULL, experiment_id INT NOT NULL, experiment_value VARCHAR (255) NOT NULL, total_count BIGINT,  five_minutes_count BIGINT , last_update_date BIGINT,  PRIMARY KEY (server_name, experiment_id, experiment_value))
CREATE TABLE userState (user_id VARCHAR (50) NOT NULL, state VARCHAR (4096) , date_updated BIGINT NOT NULL, PRIMARY KEY(user_id))
```
        
## Configuration and Deployment

- Compile the project  

- Copy the resulting jar and lib folder from the target folder to your folder of choice.

```
cp petri-server/target/petri-server-1.19.0-SNAPSHOT.jar
cp -r petri-server/target/lib .
```
  
- Create a 'petri.properties' file. The values below should match your database info and your server's port for receiving RPC requests.


```
db.username : <username_string>
db.password : <password_string>
db.url : <url_string>
server.port : <int> // Your server's port, on which it will receive RPC requests
```

- Run the server: `java -jar petri-server-1.19.0-SNAPSHOT.jar` 
