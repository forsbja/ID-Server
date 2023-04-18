## Project 2/Identity Service

* Authors: Everest K C, Jake Forsberg
* Class: CS555 [Distributed Systems]
* Semseter: Spring, Year: 2023

## Overview

The project implements a concurrent identity server and a client in Kotlin. The project uses gRPC as a middleware for the communication between the server and the client.

## Link to Demo Video
https://youtu.be/6EGFeebuFik

## Manifest

`certificates` directory containing the self signed certificate and the private key used to sign the certificate.<br/>
`certificates/certificate.cer` self signed certificate for the server.<br/>
`certificates/mykey.pem.pkcs8` private key used to sign the certificate.<br/>
`docker` directory containing the docker-compose configuration file and the volume for redis docker image.<br/>
`docker/docker-compose.yaml` docker-compose configuration file.<br/>
`identity-service` directory containing the kotlin source codes, protocol buffer files, kotlin and java libraries, and gradle build files required for the project.<br/>
`identity-service/src` directory containing the kotlin source codes, and protocol buffer files.<br/>
`identity-service/build` directory containing the libraries and binary files generated after the build process.<br/>
`identity-service/build.gradle.kts` gradle build file containing the steps required to fetch the necessary libraries and build the project.<br/>
`Makefile` file containing build steps to build the project.<br/>
`runredis.sh` shell script used to start and stop the redis server.<br/>
`runservice.sh` shell script used to run the identity server and identity client.<br/>

## Building the project

Below mentioned steps should be followed to build the project:
1. Clone the code repository from the github
```sh
git git@github.com:cs455-spring2023/project-2-identity-server-bestteam.git
```

2. Change directory to project-2-identity-server-bestteam
```sh
cd project-2-identity-server-bestteam
```

3. Build the Server and Client
```sh
make clean
make all
```
executables files are generated.

## Usage
1. Run the redis server
./runredis.sh 
```sh
./runredis.sh 
```

1. Run the server   
./runservice.sh server <enable_ssl> <port>
```sh
./run_server.sh server true 5001
```

2. Run the client   
./runservice.sh client <enable_ssl> <host> <port> <command> <arguments>
```sh
./run_client.sh client true localhost 5001 create jke Jake strongpassword
```

3. You get the chat prompt, where you can use the following commands:

| __Command__ | __Description__ |
|-------------|-----------------|
| create \<loginName> \<realName> \<password>| Creates a user entry with the given login name and real name.|
| modify \<oldLoginName> \<newLoginName> \<password>| Changes the user’s login name from the old to the new. |
| delete \<loginName> \<password> | Deletes the entry of the user with the given login name. |
| lookup \<loginName> | Retrieves the user entry with the specified login name. |
| reverseLookup <userID> | Retrieves the entry of user with the ID passed in. |
| listLogins | Returns a list of login names for all user entries. |
| listIds | Returns a list of user IDs for all user entries. |
| listAllInfo | Returns the complete entry for every user. |

## Assumptions
For ease of work in our project we have made following assumptions:<br/>
1. The real name to be supplied to the create command cannot be multiple string literals seperated by space.<br/>
2. The redis-server's port and hostname has been hardcoded for ease in this project.<br/>
3. We have assumed that the redis server always runs in the same machine as the server with its port exposed only to the localhost and hence We haven't encrypted the traffic between the backedn server and the redis server for this project.<br/>
4. We believe that the Redis Server should not be killed when the Identity Server is shutdown.<br/>

## Implementation
Our project utilized the powerful jBCrypt library, a Java implementation of OpenBSD's Blowfish password hashing code. This enabled us to accurately manage the hash and salt aspects of password storage.<br/>
To store our data, we relied on Redis, an efficient in-memory Key Value store. We also leveraged Redis' AOF (Append Only File) functionality to ensure that data persistence was achieved. The Redisson library was used to interact with the Redis Server, providing a reliable and user-friendly interface.<br/>
To achieve consistency in our data, we relied on Redisson's Read Write Locks mechanism, which allowed us to ensure that data integrity was maintained even in complex situations.

## Testing
Our team has taken a thorough approach to testing our Kotlin code by writing several unit tests using Kotlin tests. For each method, we have created a dedicated test that not only verifies correct input, but also tests for potential issues that could potentially break the code. For instance, when testing the create method, which is responsible for user creation, we have designed tests that successfully create a user, as well as tests that cover user creation failures due to duplicate login names.<br/>
In addition to our unit tests, we have taken the extra step of running manual tests to verify the encryption of our application's traffic. For this purpose, we have utilized the tcpdump tool to capture network traffic, ensuring that sensitive information is properly protected. To make this process more accessible to others, we have included a detailed demonstration of our encryption test in a video.

## Known Bugs
N/A

## Reflection and Self Assessment
Our project was a fascinating endeavor that allowed us to explore various concepts such as encryption, password storage schemes, and the use of locks to ensure consistency. Throughout the project, we delved deeper into Redis and gained a thorough understanding of its different data types. In particular, we utilized Strings, Hashes, and Sets to achieve our goals.<br/>
One of the most exciting parts of the project was learning about Kotlin tests. We were able to create and run tests for our code, which helped us identify and fix bugs early on in the development process.<br/>
As we progressed, we encountered a specific challenge that required the use of Redisson ReadWriteLock. While this lock mechanism was effective, we realized that using RedLock would have made our work easier for future projects.
Overall, this project was an incredible learning experience that allowed us to apply our knowledge to a practical scenario. We look forward to utilizing our newfound skills and knowledge in future projects.

**Development Process / Role**   
Throughout this project, we leveraged the power of peer-programming to achieve our goals. Our team worked collaboratively on both the server-side and client-side code, as well as the various tests required for the project.<br/>
Code review was a critical aspect of our peer-programming approach. We took the time to review each other's work thoroughly, providing feedback and suggestions where necessary. This allowed us to identify and address any issues early on in the development process, ensuring that our code was of the highest quality.
