# integration-protocol-task
Integration solution between Gaming provider and Gaming platform

# POC of Integration Between Gaming Provider and Gaming Platform
This project is POC for the integration between a gaming provider and gaming platforms.
It is split into modules because, in a real production environment, each module should run in separate Kubernetes pods, handling its own 
logic and scaling independently.

## Login Flow Overview  
According to the task, the user first logs in at the gaming platform and is then redirected to our system.  
My approach:  
1. The gaming provider generates a JWT token after a successful login.  
2. The user is redirected to Auth module endpoint, where it:  
    - Validate the JWT token  
    - Check if the requested game is allowed for the platform  
    - Perform other authentication/authorization logic  
3. If successful, the Auth module generates a session storing all necessary information.  
4. The user is then redirected to the Game module endpoint with the session ID as a query parameter.  
5. The Game module validates the session via the Auth module to prevent direct/fraudulent calls.  
6. If the session is valid, the response contains all information needed to launch the requested game.  

## Modules Description
### Auth Module
1. Responsible for authentication, JWT token validation, session creation, and session validation.
2. Subscribes to the Platform-Policies Kafka queue to receive and cache platform policy information, such as allowed games per platform.
3. POC simplification: Kafka is simulated locally, and the cache is in-memory.
4. Real production: Should run in separate Kubernetes pods to support multiple instances and scale independently.
###  Game Module
1. Focused on game interaction.
2. Validates the session ID received from the Auth module and returns game information if valid.
3. Ensures only authenticated and authorized players can play games.
###  Platform-Policy Module
1. Interacts with gaming platforms and pushes updates to a Kafka topic.
2. Keeps platform policy data up-to-date (e.g., if a game is removed or restricted).
3. Auth module consumes updates to maintain an accurate in-memory cache.


To run the application:  
<pre>
cd main  
mvn quarkus:dev  
</pre>

## Simplifications  
1. Kafka queue is mocked for simplicity. In production should be changed to real Kafka cluster.  
2. Platform policies are cached in-memory. In production should be used reddis or other cache specific solution to serve to multiple    microservise pods and also to be able to scale independently. 
3. JWT validation is also simplified. In production should be replaced with a proper JWT library with key rotation and full validation.  
4. All configs should be move to vault  
5. And much more test should be added on all levels  
